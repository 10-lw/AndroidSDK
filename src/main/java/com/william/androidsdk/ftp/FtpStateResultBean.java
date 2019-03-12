package com.william.androidsdk.ftp;

public class FtpStateResultBean {
    //-1 -- default value
    public static final int DEFAULT_VALUE = -1;
    //0 -- fail
    public static final int FAIL_TYPE_VALUE = 0;
    //1 -- success
    public static final int SUCCESS_TYPE_VALUE = 1;
    //2 -- abort
    public static final int ABORT_TYPE_VALUE = 2;


    private String localFilePath;
    private String remoteFilePath;
    private int progress;
    private int success = DEFAULT_VALUE; // -1 -- default value;  0 -- fail;  1 -- success；  2 -- abort
    private String errorMsg;
    private long remoteFileSize;


    @Override
    public String toString() {
        return "FtpStateResultBean{" +
                "localFilePath='" + localFilePath + '\'' +
                ", remoteFilePath='" + remoteFilePath + '\'' +
                ", progress=" + progress +
                ", success=" + success +
                ", errorMsg='" + errorMsg + '\'' +
                ", remoteFileSize=" + remoteFileSize +
                '}';
    }

    public long getRemoteFileSize() {
        return remoteFileSize;
    }

    public void setRemoteFileSize(long remoteFileSize) {
        this.remoteFileSize = remoteFileSize;
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
