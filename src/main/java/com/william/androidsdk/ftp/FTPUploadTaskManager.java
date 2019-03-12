package com.william.androidsdk.ftp;

import android.annotation.SuppressLint;
import android.util.Log;

import com.william.androidsdk.utils.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 用来管理 同时上传, 最多同时上传的数量为 MAX_UPLOAD_COUNT;
 */
public class FTPUploadTaskManager {
    private static FTPUploadTaskManager ftpUploadTaskManager;
    private static final String TAG = "FTPUploadTaskManager";
    //一次最多同时下载的数量
    private static final int MAX_UPLOAD_COUNT = 3;
    private volatile int uploadCurrentCount = 0;
    private static Object count;

    //upload 等待上传的文件
    private HashMap<String, FtpTranslateBean> pendingUploadTasks;
    //正在上传的文件信息
    private volatile HashMap<String, FtpTranslateBean> uploadingTasks;
    private LinkedList<String> uploadPendingTaskSet;
    private LinkedList<String> uploadingSet;
    private HashMap<String, FTPManager> managerMap;


    public static FTPUploadTaskManager getInstance() {
        if (ftpUploadTaskManager == null) {
            ftpUploadTaskManager = new FTPUploadTaskManager();
            count = new Object();
        }
        return ftpUploadTaskManager;
    }

    private FTPUploadTaskManager() {
        pendingUploadTasks = new HashMap<>();
        uploadingTasks = new HashMap<>();
        uploadPendingTaskSet = new LinkedList<>();
        uploadingSet = new LinkedList<>();
        managerMap = new HashMap<>();
    }

    private void startNextUploadTask() {
        synchronized (uploadPendingTaskSet) {
            if (uploadCurrentCount < MAX_UPLOAD_COUNT && !uploadPendingTaskSet.isEmpty()) {
                String s = uploadPendingTaskSet.removeFirst();
                if (!StringUtils.isNullOrEmpty(s)) {
                    FtpTranslateBean ftpUploadBean = pendingUploadTasks.get(s);
                    loginAndUploadFile(ftpUploadBean.localPath, ftpUploadBean.remotePath,
                            ftpUploadBean.remoteFileName, ftpUploadBean.listener, ftpUploadBean.ftpTransferInfo);
                }
            }
        }
    }

    public void resetAll() {
        ftpUploadTaskManager = null;
        pendingUploadTasks = null;
        uploadPendingTaskSet.clear();
        uploadPendingTaskSet = null;
        uploadingTasks.clear();
        uploadingTasks = null;
        uploadCurrentCount = 0;
        uploadingSet.clear();
        uploadingSet = null;
    }

    private void storeUploadFtpTask(String localPath, FtpTranslateBean ftpUploadBean) {
        synchronized (uploadPendingTaskSet) {
            if (!uploadPendingTaskSet.contains(localPath)) {
                pendingUploadTasks.put(localPath, ftpUploadBean);
                uploadPendingTaskSet.add(localPath);
            }
        }
    }

    public synchronized void resumeUploadFile(String localPath) {
        if (uploadingTasks.containsKey(localPath)) {
            Log.d(TAG, "resumeUploadFile: ===" + localPath);
            FtpTranslateBean ftpUploadBean = uploadingTasks.get(localPath);
            loginAndUploadFile(ftpUploadBean.localPath, ftpUploadBean.remotePath,
                    ftpUploadBean.remoteFileName, ftpUploadBean.listener, ftpUploadBean.ftpTransferInfo);
        }
    }

    public void addDBUploadingData(String localPath, FtpTranslateBean ftpUploadBean) {
        if (!uploadingTasks.containsKey(localPath)) {
            uploadingTasks.put(localPath, ftpUploadBean);
        }
    }

    /**
     * 每单个上传需要建立一个新的连接, 所以想同时上传多个文件,需要建立多个ftp连接, 并且在上传成功/失败时 自动关闭当前的连接,
     */
    @SuppressLint("CheckResult")
    public synchronized void loginAndUploadFile(final String localPath, final String serverPath,
                                                final String fileOnServerName, final FtpStateListener listener, final FTPTransferInfo ftpTransferInfo) {
        FtpTranslateBean ftpUploadBean = new FtpTranslateBean(localPath, serverPath, fileOnServerName, listener, ftpTransferInfo);
        if (uploadCurrentCount >= MAX_UPLOAD_COUNT) {
            //当上传的数量大于规定的数量时,暂时将任务保存起来.
            storeUploadFtpTask(localPath, ftpUploadBean);
            return;
        }
        synchronized (count) {
            uploadCurrentCount++;
        }
        if (!uploadingSet.contains(localPath)) {
            uploadingSet.add(localPath);
            uploadingTasks.put(localPath, ftpUploadBean);
        } else {
            Log.d(TAG, "loginAndUploadFile: is in uploading task");
            return;
        }
        Observable
                .create(new ObservableOnSubscribe<FtpStateResultBean>() {
                    @Override
                    public void subscribe(final ObservableEmitter<FtpStateResultBean> emitter) throws Exception {
                        final FTPManager ftpManager = new FTPManager();
                        managerMap.put(localPath, ftpManager);
                        final FtpStateResultBean result = new FtpStateResultBean();
                        //第一步 连接ftp服务器
                        ftpManager.connectByConfig(ftpTransferInfo, new FtpConnectStateListener() {
                            @Override
                            public void onLoginState(boolean success) {
                                if (success) {
                                    try {
                                        //第二步 连接成功后开始上传文件
                                        ftpManager.uploadFile(localPath, serverPath, fileOnServerName, new FtpStateListener() {
                                            @Override
                                            public void onProgress(int progress) {
                                                result.setProgress(progress);
                                                emitter.onNext(result);
                                            }

                                            @Override
                                            public void onSuccessful(String path) {
                                                result.setSuccess(1);
                                                result.setProgress(100);
                                                result.setRemoteFilePath(path);
                                                result.setLocalFilePath(localPath);
                                                emitter.onNext(result);
                                                emitter.onComplete();
                                                //第三步  上传完成或失败 关闭当前的ftp连接
                                                closeCurrentFtpConnection(ftpManager);
                                            }

                                            @Override
                                            public void onFail(String msg) {
                                                onFtpFail(msg, result, emitter, ftpManager);
                                            }

                                            @Override
                                            public void onAbort(int progress, String path, long remoteFileSize) {
                                                result.setSuccess(2);
                                                result.setProgress(progress);
                                                result.setRemoteFilePath(path);
                                                result.setRemoteFileSize(remoteFileSize);
                                                result.setLocalFilePath(localPath);
                                                emitter.onNext(result);
                                                emitter.onComplete();
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        onFtpFail(e.getMessage(), result, emitter, ftpManager);
                                    }
                                } else {
                                    onFtpFail("Login ftp error!", result, emitter, ftpManager);
                                }
                            }
                        });

                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<FtpStateResultBean>() {
                    @Override
                    public void accept(FtpStateResultBean result) throws Exception {
                        if (result.getProgress() > 0) {
                            listener.onProgress(result.getProgress());
                        }
                        if (result.getProgress() == 100 && result.getSuccess() == FtpStateResultBean.SUCCESS_TYPE_VALUE) {
                            listener.onSuccessful(result.getRemoteFilePath());
                            checkAndStartNewOne(localPath);
                        }
                        if (result.getSuccess() == FtpStateResultBean.FAIL_TYPE_VALUE) {
                            listener.onFail(result.getErrorMsg());
                            checkAndStartNewOne(localPath);
                        }

                        if (result.getSuccess() == FtpStateResultBean.ABORT_TYPE_VALUE) {
                            uploadingSet.remove(localPath);
                            listener.onAbort(result.getProgress(), result.getRemoteFilePath(), result.getRemoteFileSize());
                        }
                    }
                });
    }

    public synchronized void abortTransfer(final String localPath) {
        if (managerMap.containsKey(localPath)) {
            FTPManager ftpManager = managerMap.get(localPath);
            if (ftpManager != null) {
                boolean uploadOrDownload = ftpManager.pauseUploadOrDownload();
                if (uploadOrDownload) {
                    synchronized (count) {
                        uploadCurrentCount--;
                    }
                }
            } else {
                Log.d(TAG, "abortTransfer: ====111==null=====");
            }
        } else {
            Log.d(TAG, "abortTransfer: =====2222=null=====");
        }
    }

    public synchronized void abortAllTransfer() {
        Log.d(TAG, "abortAllTransfer: =====size:" + managerMap.size());
        if (managerMap != null && managerMap.size() > 0) {
            Set<Map.Entry<String, FTPManager>> entries = managerMap.entrySet();
            for (Map.Entry<String, FTPManager> entry : entries) {
                FTPManager ftpManager = entry.getValue();
                if (ftpManager != null) {
                    ftpManager.pauseUploadOrDownload();
                }
            }
            uploadingSet.clear();
            uploadingTasks.clear();
            managerMap.clear();
            uploadPendingTaskSet.clear();
            uploadCurrentCount = 0;
        }
    }

    private void onFtpFail(String msg, FtpStateResultBean result, ObservableEmitter<FtpStateResultBean> emitter, FTPManager ftpManager) {
        result.setSuccess(0);
        result.setErrorMsg(msg);
        emitter.onNext(result);
        emitter.onComplete();
        closeCurrentFtpConnection(ftpManager);
    }


    private void closeCurrentFtpConnection(FTPManager ftpManager) {
        ftpManager.closeFTP();
    }

    private synchronized void checkAndStartNewOne(String localPath) {
        uploadingSet.remove(localPath);
        uploadingTasks.remove(localPath);
        synchronized (count) {
            uploadCurrentCount--;
        }
        if (uploadCurrentCount < MAX_UPLOAD_COUNT) {
            startNextUploadTask();
        }
    }
}
