package org.watchman.main.service;

/**
 * Created by waderwu on 18-4-9.
 */


import android.util.Log;
import android.widget.Toast;

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
        Map email_fetch = new HashMap();
        Store store = null;
        Folder folder = null;
        Message message = null;

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
            Log.d("from",from);
            Log.d("subject",subject);
            email_fetch.put("from",from);
            email_fetch.put("subject",subject);
            email_fetch.put("date",date);


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

        return email_fetch;
    }

    private void execute (Runnable runnable)
    {
        new Thread (runnable).start();
    }
}