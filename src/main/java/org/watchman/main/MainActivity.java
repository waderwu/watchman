package org.watchman.main;

import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Date;
import java.util.Locale;
import java.util.Map;


import org.watchman.main.service.EmailSender;
import org.watchman.main.service.EmailFetch;

public class MainActivity extends AppCompatActivity {
    TextToSpeech textToSpeech;
    String res = null;

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
                            res = email_fetch.get("subject").toString();
                            Log.d("subject",res);
                            Log.d("date",email_fetch.get("date").toString());
                        } catch(Exception e) {
                            Log.e("error",e.getMessage(),e);
                        }
                    }

                }).start();

            }
        });

        EditText text = (EditText) findViewById(R.id.editText);

        Button voice = (Button) findViewById(R.id.button2);

        textToSpeech = new TextToSpeech(MainActivity.this,
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int i) {
                        //如果装载TTS引擎成功
                        if (i == TextToSpeech.SUCCESS) {

                            /*美式英语按钮监听*/
                            voice.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //设置使用美式英语朗读
                                    int result = textToSpeech.setLanguage(Locale.US);
//                                    int result = textToSpeech.setLanguage(Locale.CHINA);
                                    //如果不支持所设置的语言
                                    if ((result != textToSpeech.LANG_COUNTRY_AVAILABLE)
                                            && (result != TextToSpeech.LANG_AVAILABLE)) {
                                        Toast.makeText(MainActivity.this, "暂时不支持这种语言的朗读", Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                    //执行朗读
                                    textToSpeech.speak(res.substring(1),
                                            TextToSpeech.QUEUE_ADD, null);
                                }
                            });
                        }
                    }
                });



    }



}
