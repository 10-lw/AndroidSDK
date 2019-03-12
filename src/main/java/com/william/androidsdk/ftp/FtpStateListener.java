package com.william.androidsdk.ftp;

public abstract class FtpStateListener {

    public abstract void onProgress(int progress);

    // 成功后目的地的path, 即上传成功后服务器上的地址, 下载成功后本地的地址，全路径
    public abstract void onSuccessful(String path);

    public abstract void onFail(String msg);

    // 成功后目的地的path, 即上传成功后服务器上的地址, 下载成功后本地的地址，全路径
    public abstract void onAbort(int progress, String remotePath, long remoteFileSize);
}
