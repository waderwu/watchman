package org.watchman.main.service;

/**
 * Created by waderwu on 18-5-2.
 */


import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPHTTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.apache.commons.net.util.TrustManagerUtils;


public class Ftp
{
    private String ftp_url;
    private String ftp_account;
    private String ftp_password;
    private int ftp_port;
    public boolean login_staus=false;
    public boolean upload_status=false;

    public FTPClient ftp_clinet;

    public Ftp(String ftp_url,int ftp_port, String ftp_account, String ftp_password)
    {
        this.ftp_url = ftp_url;
        this.ftp_port = ftp_port;
        this.ftp_account = ftp_account;
        this.ftp_password = ftp_password;

        this.ftp_clinet = connectFTP(ftp_url,ftp_port,ftp_account,ftp_password);

    }




    public FTPClient connectFTP(String url, int port, String username, String password) {
        FTPClient ftp = null;
        try {
            ftp = new FTPClient();
            ftp.connect(url,port);
            if (FTPReply.isPositiveCompletion(ftp.getReplyCode())){
                Log.d("ftp","login to the ftp server");
                this.login_staus = ftp.login(username,password);
                ftp.enterLocalPassiveMode();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ftp;
    }


    public Boolean upload_file(String src_file_path,String des_file_name, String des_file_dir)
    {

        try{
            FileInputStream srcFileStream = new FileInputStream(src_file_path);
            this.upload_status = this.ftp_clinet.storeFile(des_file_name,srcFileStream);
            srcFileStream.close();
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        return this.upload_status;
    }

}
