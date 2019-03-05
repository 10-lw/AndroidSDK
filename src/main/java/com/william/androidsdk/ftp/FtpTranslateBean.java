package com.william.androidsdk.ftp;

public class FtpTranslateBean {
    public String localPath;
    public String remotePath;
    public FtpStateListener listener;

    public FtpTranslateBean(String localPath, String remotePath, FtpStateListener listener) {
        this.localPath = localPath;
        this.remotePath = remotePath;
        this.listener = listener;
    }

    @Override
    public String toString() {
        return "FtpTranslateBean{" +
                "localPath='" + localPath + '\'' +
                ", remotePath='" + remotePath + '\'' +
                '}';
    }
}
