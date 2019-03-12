package com.william.androidsdk.ftp;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;


import com.william.androidsdk.utils.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 用来管理 同时下载, 最多同时下载的数量为 MAX_DOWNLOAD_COUNT;
 */
public class FTPDownloadTaskManager {
    private static final String TAG = "FTPDownloadTaskManager";
    private static FTPDownloadTaskManager ftpDownloadTaskManager;
    //一次最多同时下载的数量
    public static final int MAX_DOWNLOAD_COUNT = 3;
    private volatile int downloadCurrentCount = 0;
    private static Object count;

    //downlaod
    private HashMap<String, FtpTranslateBean> pendingDownloadTasks;
    private LinkedList<String> downloadTaskSet;

    private Timer downloadCheckTimer;
    private Runnable downloadRunnable;
    private final Handler handler;
    private volatile boolean downloadTimerStarted = false;

    public static FTPDownloadTaskManager getInstance() {
        if (ftpDownloadTaskManager == null) {
            ftpDownloadTaskManager = new FTPDownloadTaskManager();
            count = new Object();
        }
        return ftpDownloadTaskManager;
    }

    private FTPDownloadTaskManager() {
        handler = new Handler();
        pendingDownloadTasks = new HashMap<>();
        downloadTaskSet = new LinkedList<>();

        downloadCheckTimer = new Timer();
        downloadRunnable = new Runnable() {
            @Override
            public void run() {
                cancelAll();
                Log.d(TAG, "FTPDownloadTaskManager: cancel downloadCheckTimer");
            }
        };

        TimerTask downloadTimerTask = new TimerTask() {
            @Override
            public void run() {
                synchronized (downloadTaskSet) {
                    downloadTimerStarted = true;
                    if (downloadCurrentCount < MAX_DOWNLOAD_COUNT && !downloadTaskSet.isEmpty()) {
                        String s = downloadTaskSet.removeFirst();
                        if (!StringUtils.isNullOrEmpty(s)) {
                            FtpTranslateBean ftpTranslateBean = pendingDownloadTasks.get(s);
                            loginFtpAndDownLoad(ftpTranslateBean.localPath, s, ftpTranslateBean.listener);
                        }
                    }
                }
            }
        };
        if (!downloadTimerStarted) {
            downloadCheckTimer.schedule(downloadTimerTask, 0, 3000);
        }
    }

    private void cancelAll() {
        downloadTimerStarted = false;
        downloadCheckTimer.cancel();
        downloadCheckTimer.purge();
        ftpDownloadTaskManager = null;
        pendingDownloadTasks = null;
        downloadTaskSet = null;
    }


    public void downloadFtpFile(String localPath, String remotePath, FtpStateListener listener) {
        synchronized (downloadTaskSet) {
            if (!downloadTaskSet.contains(remotePath)) {
                FtpTranslateBean ftpTranslateBean = new FtpTranslateBean(localPath, remotePath, listener);
                pendingDownloadTasks.put(remotePath, ftpTranslateBean);
                downloadTaskSet.add(remotePath);
            }
        }
    }

    @SuppressLint("CheckResult")
    private void loginFtpAndDownLoad(final String localPath, final String remotePath, final FtpStateListener listener) {
        synchronized (count) {
            downloadCurrentCount++;
        }
        Observable
                .create(new ObservableOnSubscribe<FtpStateResultBean>() {
                    @Override
                    public void subscribe(final ObservableEmitter<FtpStateResultBean> emitter) throws Exception {
                        //step 1 login to ftp server
                        final FTPManager ftpManager = new FTPManager();
                        final FtpStateResultBean result = new FtpStateResultBean();

                        ftpManager.connectByConfig(null, new FtpConnectStateListener() {
                            @Override
                            public void onLoginState(boolean success) {
                                //step 2  start download files
                                if (success) {
                                    try {
                                        Log.d(TAG, "start download ftp file");
                                        ftpManager.downloadFile(localPath, remotePath, new FtpStateListener() {
                                            @Override
                                            public void onProgress(int progress) {
                                                result.setProgress((int) progress);
                                                emitter.onNext(result);
                                            }

                                            @Override
                                            public void onSuccessful(String localPath) {
                                                //step 3 close current ftp connection when download fail or success
                                                closeCurrentFtpConnection(ftpManager);
                                                result.setSuccess(1);
//                                                result.setProgress(100);
                                                result.setLocalFilePath(localPath);
                                                result.setRemoteFilePath(remotePath);
                                                emitter.onNext(result);
                                                emitter.onComplete();
                                            }

                                            @Override
                                            public void onFail(String msg) {
                                                closeCurrentFtpConnection(ftpManager);
                                                result.setSuccess(0);
                                                result.setErrorMsg(msg);
                                                emitter.onNext(result);
                                                emitter.onComplete();
                                            }

                                            @Override
                                            public void onAbort(int progress, String remotePath, long remoteFileSize) {

                                            }
                                        });
                                    } catch (Exception e) {
                                        closeCurrentFtpConnection(ftpManager);
                                        e.printStackTrace();
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
                        listener.onProgress(result.getProgress());
                        if (result.getProgress() < 100) {
                            handler.removeCallbacks(downloadRunnable);
                        }
                        if (result.getProgress() == 100 && result.getSuccess() == 1) {
                            listener.onSuccessful(result.getLocalFilePath());
                            startCheckAndEndDownloadTimer();
                        }
                        if (result.getSuccess() == 0) {
                            listener.onFail(result.getErrorMsg());
                            startCheckAndEndDownloadTimer();
                        }
                    }
                });
    }

    private void closeCurrentFtpConnection(FTPManager ftpManager) {
        ftpManager.closeFTP();
    }

    private void startCheckAndEndDownloadTimer() {
        synchronized (count) {
            downloadCurrentCount--;
        }
        if (downloadCurrentCount <= 0) {
            handler.postDelayed(downloadRunnable, 30000);
        } else {
            handler.removeCallbacks(downloadRunnable);
        }
    }
}
