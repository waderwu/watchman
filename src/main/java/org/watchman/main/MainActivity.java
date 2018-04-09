package org.watchman.main;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import java.util.Date;
import java.util.Map;


import org.watchman.main.service.EmailSender;
import org.watchman.main.service.EmailFetch;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("an","hhhhhh");
                new Thread(new Runnable() {
//                    String receiver = "wu2015@sjtu.edu.cn"; can
                    String receiver = "waderwu@qq.com";//can sender but spam

                    @Override
                    public void run() {
                        EmailSender emailSender = new EmailSender("myautosender@163.com","laozi9yongmw");
                        EmailFetch emailFetch = new EmailFetch("myautosender@163.com","laozi9yongmw");

//                        EmailSender emailSender = new EmailSender("wu2015@sjtu.edu.cn","");
                        try{
//                            emailSender.sendMail("title","成绩","myautosender@163.com",receiver);
                            Map email_fetch = emailFetch.fetchEmail();
                            Log.d("from",email_fetch.get("from").toString());
                            Log.d("subject",email_fetch.get("subject").toString());
                            Log.d("date",email_fetch.get("date").toString());
                        } catch(Exception e) {
                            Log.e("error",e.getMessage(),e);
                        }
                    }

                }).start();

            }
        });
    }



}
