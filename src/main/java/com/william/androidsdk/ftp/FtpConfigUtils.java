package com.william.androidsdk.ftp;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class FtpConfigUtils {
    private static final String TAG = "FtpConfigUtils";
    public static final String IP = "ip";
    public static final String PORT = "port";
    public static final String USER_NAME = "userName";
    public static final String PWD = "pwd";
    public static final String PROXY = "FtpProxy";
    public static final String USER = "ftpUsr";

    public static HashMap<String, String> readContentOfFile(File fileForRead) {
        HashMap<String, String> map = new HashMap<>();
        FileInputStream in = null;
        InputStreamReader reader = null;
        BufferedReader breader = null;
        try {
            in = new FileInputStream(fileForRead);
            reader = new InputStreamReader(in, "utf-8");
            breader = new BufferedReader(reader);

            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = breader.readLine()) != null) {
                int endIndex = line.indexOf("=");
                String cf = line.substring(0, endIndex);
                String configs = line.substring(endIndex + 1);
                int index = configs.indexOf(":");
                if (cf.equals(PROXY)) {
                    map.put(IP, configs.substring(0, index));
                    map.put(PORT, configs.substring(index + 1));
                } else if (cf.equals(USER)) {
                    map.put(USER_NAME, configs.substring(0, index));
                    map.put(PWD, configs.substring(index + 1));
                }
                sb.append(line);
            }
//            Log.i(TAG, "Config file's content is: " + sb.toString());
            return map;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(breader);
            closeQuietly(reader);
            closeQuietly(in);
        }
        return null;
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null)
                closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
