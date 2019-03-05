package com.william.androidsdk.ftp;

public class FtpStateResultBean {
    private String localFilePath;
    private String remoteFilePath;
    private int progress;
    private int success = -1; // -1 -- default value;  0 -- fail;  1 -- success
    private String errorMsg;

    @Override
    public String toString() {
        return "FtpStateResultBean{" +
                "remoteFilePath='" + remoteFilePath + '\'' +
                ", progress=" + progress +
                ", success=" + success +
                ", errorMsg='" + errorMsg + '\'' +
                ", localFilePath='" + localFilePath + '\'' +
                '}';
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }

    public void setLocalFilePath(String localFilePath) {
        this.localFilePath = localFilePath;
    }

    public String getRemoteFilePath() {
        return remoteFilePath;
    }

    public void setRemoteFilePath(String remoteFilePath) {
        this.remoteFilePath = remoteFilePath;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

}
