package com.william.androidsdk.ftp;

public class FTPUserInfo {
    private String host;
    private String port;

    public FTPUserInfo(String host, String port, String userName, String pwd) {
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.pwd = pwd;
    }

    @Override
    public String toString() {
        return "FTPUserInfo{" +
                "host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", userName='" + userName + '\'' +
                ", pwd='" + pwd + '\'' +
                '}';
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    private String userName;
    private String pwd;

}
