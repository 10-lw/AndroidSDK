package com.william.androidsdk.ftp;

public class FTPTransferInfo {

    /**
     * upload : true
     * message :
     * host : 183.6.117.87
     * port : 20003
     * path : /ysj/1310/so.ofo.labofo_V18001.apk
     * user : 5
     * pass : 1552292597
     */

    private boolean upload;
    private String message;
    private String host;
    private int port;
    private String path;
    private int user;
    private int pass;

    public boolean isUpload() {
        return upload;
    }

    public void setUpload(boolean upload) {
        this.upload = upload;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public int getPass() {
        return pass;
    }

    public void setPass(int pass) {
        this.pass = pass;
    }
}
