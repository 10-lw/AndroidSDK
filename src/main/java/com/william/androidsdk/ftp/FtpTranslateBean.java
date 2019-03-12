package com.william.androidsdk.ftp;

public class FtpTranslateBean {
    public String localPath;
    public String remotePath;
    public FtpStateListener listener;
    public String remoteFileName;
    public FTPTransferInfo ftpTransferInfo;

    public FtpTranslateBean(String localPath, String remotePath, String remoteFileName, FtpStateListener listener, FTPTransferInfo ftpTransferInfo) {
        this.localPath = localPath;
        this.remotePath = remotePath;
        this.listener = listener;
        this.remoteFileName = remoteFileName;
        this.ftpTransferInfo = ftpTransferInfo;
    }

    public FtpTranslateBean(String localPath, String remotePath, String remoteFileName, FtpStateListener listener) {
        this.localPath = localPath;
        this.remotePath = remotePath;
        this.listener = listener;
        this.remoteFileName = remoteFileName;
    }

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
