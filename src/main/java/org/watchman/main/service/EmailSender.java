package org.watchman.main.service;

/**
 * Created by waderwu on 18-4-8.
 */

import android.app.Activity;
import android.content.Context;

import net.sourceforge.argparse4j.inf.Namespace;

import org.asamk.signal.Main;

import java.util.ArrayList;
import java.util.HashMap;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.watchman.main.PreferenceManager;
import org.watchman.main.service.JSSEProvider;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.Properties;




public class EmailSender extends javax.mail.Authenticator {

    private String mailhost = "smtp.163.com";
    private String username;
    private String password;
    private Session session;

    static {
        Security.addProvider(new JSSEProvider());
    }


    public EmailSender(String username, String password) {
        this.username = username;
        this.password = password;
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", mailhost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        session = Session.getDefaultInstance(props, this);
    }


    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
    }


    /**
     * @param subject    标题
     * @param body       内容
     * @param sender     发件人
     * @param recipients 收件人
     * @throws Exception
     */
    public synchronized void sendMail(String subject, String body, String sender, String recipients) throws Exception {
        try {
            MimeMessage message = new MimeMessage(session);
            DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));
            message.setSender(new InternetAddress(sender));
            message.setSubject(subject);
            message.setDataHandler(handler);
            if (recipients.indexOf(',') > 0)
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            else
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public synchronized void sendMailwithAttachment(String subject, String body, String sender, String recipients,String attachment) throws Exception {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setSender(new InternetAddress(sender));
            message.setSubject(subject);
            MimeBodyPart body0 = new MimeBodyPart();
            body0.setContent(body,"text/html;charset=utf-8");
            MimeBodyPart body1 = new MimeBodyPart();
            body1.setDataHandler( new DataHandler( new FileDataSource(attachment)));
            body1.setFileName( MimeUtility.encodeText(attachment));
            MimeMultipart mm = new MimeMultipart();
            mm.addBodyPart(body0);
            mm.addBodyPart(body1);
            message.setContent(mm);
            if (recipients.indexOf(',') > 0)
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            else
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public class ByteArrayDataSource implements DataSource {
        private byte[] data;
        private String type;

        public ByteArrayDataSource(byte[] data, String type) {
            super();
            this.data = data;
            this.type = type;
        }

        public ByteArrayDataSource(byte[] data) {
            super();
            this.data = data;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContentType() {
            if (type == null)
                return "application/octet-stream";
            else
                return type;
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public String getName() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }
    }

}


//    private Context mContext;
//    private static SignalSender mInstance;
//    private String mUsername; //aka your signal phone number
//
//    private SignalSender(Context context, String username)
//    {
//        mContext = context;
//        mUsername = username;
//    }
//
//    public static synchronized SignalSender getInstance (Context context, String username)
//    {
//        if (mInstance == null)
//        {
//            mInstance = new SignalSender(context, username);
//        }
//
//        return mInstance;
//    }
//
//    public void setUsername (String username)
//    {
//        mUsername = username;
//    }
//
//    public void reset ()
//    {
//        Main mainSignal = new Main(mContext);
//        mainSignal.resetUser();
//        mInstance = null;
//    }
//
//    public void register ()
//    {
//        execute (new Runnable() {
//            public void run() {
//                Main mainSignal = new Main(mContext);
//                HashMap<String, Object> map = new HashMap<>();
//
//                map.put("username", mUsername);
//                map.put("command", "register");
//                map.put("voice", false);
//
//                Namespace ns = new Namespace(map);
//                mainSignal.handleCommands(ns);
//            }
//        });
//    }
//
//    public void verify (final String verificationCode)
//    {
//        execute (new Runnable() {
//            public void run() {
//                Main mainSignal = new Main(mContext);
//                HashMap<String, Object> map = new HashMap<>();
//
//                map.put("username", mUsername);
//                map.put("command", "verify");
//                map.put("verificationCode", verificationCode);
//
//                Namespace ns = new Namespace(map);
//                mainSignal.handleCommands(ns);
//            }
//        });
//    }
//
//    public void sendMessage (final ArrayList<String> recipients, final String message, final String attachment)
//    {
//        execute (new Runnable() {
//            public void run() {
//                Main mainSignal = new Main(mContext);
//                HashMap<String, Object> map = new HashMap<>();
//
//                map.put("username", mUsername);
//                map.put("endsession",false);
//                map.put("recipient", recipients);
//                map.put("command", "send");
//                map.put("message", message);
//
//                if (attachment != null)
//                {
//                    ArrayList<String> attachments = new ArrayList<>();
//                    attachments.add(attachment);
//                    map.put("attachment",attachments);
//                }
//
//                Namespace ns = new Namespace(map);
//                mainSignal.handleCommands(ns);
//            }
//        });
//    }
//
//    private void execute (Runnable runnable)
//    {
//        new Thread (runnable).start();
//    }
//}

