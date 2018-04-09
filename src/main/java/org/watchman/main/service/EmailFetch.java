package org.watchman.main.service;

/**
 * Created by waderwu on 18-4-9.
 */


import android.util.Log;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

public class EmailFetch {
    private String mailhost;
    private String username;
    private String password;
    private Session session;
    private String protocol;
    private Boolean isSSL;
    private int port;

    public EmailFetch(String username, String password){
        this.username = username;
        this.password = password;
        this.protocol = "pop3";
        this.isSSL = true;
        this.mailhost = "pop.163.com";
        this.port = 995;

        Properties props = new Properties();
        props.put("mail.pop3.ssl.enable", isSSL);
        props.put("mail.pop3.host", mailhost);
        props.put("mail.pop3.port", port);

        session = Session.getInstance(props);
    }

    public synchronized Map fetchEmail() {

        Store store = null;
        Folder folder = null;
        Message message = null;
        Map email_fetch = new HashMap();

        try {
            store = session.getStore(protocol);
            store.connect(username,password);

            folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);

            int size = folder.getMessageCount();
            message = folder.getMessage(size);

            String from = message.getFrom()[0].toString();
            String subject = message.getSubject();
            Date date = message.getSentDate();
            email_fetch.put("from",from);
            email_fetch.put("subject",subject);
            email_fetch.put("date",date);

//            try{
//                String content;
//                content = message.getContent().toString();
//                Log.d("content",content);
//                message.writeTo(System.out);
//            }catch (Exception e){
//                Log.d("hahah","error");
//            }

//            Log.d("From: ",from.toString());
//            Log.d("Subject: " ,subject.toString());
//            Log.d("Date: ",date.toString());


        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } finally {
            try {
                if (folder != null) {
                    folder.close(false);
                }
                if (store != null) {
                    store.close();
                }
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }

//        Log.d("info","接收完毕！");
        return email_fetch;
    }
}