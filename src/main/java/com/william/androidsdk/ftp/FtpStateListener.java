package com.william.androidsdk.ftp;

public interface FtpStateListener {
    void onProgress(int progress);
    // 成功后目的地的path, 即上传成功后服务器上的地址, 下载成功后本地的地址
    void onSuccessful(String path);
    void onFail(String msg);
}
