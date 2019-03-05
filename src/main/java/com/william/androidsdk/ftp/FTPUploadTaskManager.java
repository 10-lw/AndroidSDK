package com.william.androidsdk.ftp;

import android.annotation.SuppressLint;
import android.util.Log;


import com.william.androidsdk.utils.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;

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

    //upload
    private HashMap<String, FtpTranslateBean> pendingUploadTasks;
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
        uploadPendingTaskSet = new LinkedList<>();
        uploadingSet = new LinkedList<>();
        managerMap = new HashMap<>();
    }

    private void startNextUploadTask() {
        synchronized (uploadPendingTaskSet) {
            Log.d(TAG, "==========startNextUploadTask  .." + uploadCurrentCount + " size:" + uploadPendingTaskSet.size());
            if (uploadCurrentCount < MAX_UPLOAD_COUNT && !uploadPendingTaskSet.isEmpty()) {
                String s = uploadPendingTaskSet.removeFirst();
                if (!StringUtils.isNullOrEmpty(s)) {
                    FtpTranslateBean ftpUploadBean = pendingUploadTasks.get(s);
                    loginAndUploadFile(ftpUploadBean.localPath, ftpUploadBean.remotePath, ftpUploadBean.listener);
                }
            }
        }
    }

    private void resetAll() {
        ftpUploadTaskManager = null;
        pendingUploadTasks = null;
        uploadPendingTaskSet = null;
        uploadCurrentCount = 0;
    }

    private void storeUploadFtpTask(String localPath, String remotePath, FtpStateListener listener) {
        synchronized (uploadPendingTaskSet) {
            if (!uploadPendingTaskSet.contains(localPath)) {
                FtpTranslateBean ftpUploadBean = new FtpTranslateBean(localPath, remotePath, listener);
                pendingUploadTasks.put(localPath, ftpUploadBean);
                uploadPendingTaskSet.add(localPath);
            }
        }
    }

    /**
     * 每单个上传需要建立一个新的连接, 所以想同时上传多个文件,需要建立多个ftp连接, 并且在上传成功/失败时 自动关闭当前的连接,
     *
     * @param localPath
     * @param serverPath
     * @param listener
     */
    @SuppressLint("CheckResult")
    public synchronized void loginAndUploadFile(final String localPath, final String serverPath, final FtpStateListener listener) {
        if (uploadCurrentCount >= MAX_UPLOAD_COUNT) {
            //当上传的数量大于规定的数量时,暂时将任务保存起来.
            storeUploadFtpTask(localPath, serverPath, listener);
            return;
        }
        synchronized (count) {
            uploadCurrentCount++;
        }
        if (!uploadingSet.contains(localPath)) {
            uploadingSet.add(localPath);
        } else {
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
                        ftpManager.connectByConfig(new FtpConnectStateListener() {
                            @Override
                            public void onLoginState(boolean success) {
                                if (success) {
                                    try {
                                        //第二步 连接成功后开始上传文件
                                        ftpManager.uploadFile(localPath, serverPath, new FtpStateListener() {
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
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        onFtpFail(e.getMessage(), result, emitter, ftpManager);
                                    }
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
                        if (result.getProgress() == 100 && result.getSuccess() == 1) {
                            listener.onSuccessful(result.getRemoteFilePath());
                            checkAndStartNewOne();
                        }
                        if (result.getSuccess() == 0) {
                            listener.onFail(result.getErrorMsg());
                            checkAndStartNewOne();
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
                        Log.d(TAG, "checkAndStartNewOne: =====count2:::" + uploadCurrentCount);
                    }
                }
            } else {
                Log.d(TAG, "abortTransfer: ====111==null=====");
            }
        } else {
            Log.d(TAG, "abortTransfer: =====2222=null=====");
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

    private synchronized void checkAndStartNewOne() {
        synchronized (count) {
            uploadCurrentCount--;
            Log.d(TAG, "checkAndStartNewOne: =====count1:::" + uploadCurrentCount);
        }
        if (uploadCurrentCount < MAX_UPLOAD_COUNT) {
            startNextUploadTask();
        }
    }
}
