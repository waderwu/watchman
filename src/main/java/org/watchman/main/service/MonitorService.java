
/*
 * Copyright (c) 2017 Nathanial Freitas / Guardian Project
 *  * Licensed under the GPLv3 license.
 *
 * Copyright (c) 2013-2015 Marco Ziccardi, Luca Bonato
 * Licensed under the MIT license.
 */

package org.watchman.main.service;


import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;

import org.watchman.main.WatchMan;
import org.watchman.main.MonitorActivity;
import org.watchman.main.PreferenceManager;
import org.watchman.main.R;
import org.watchman.main.model.Event;
import org.watchman.main.model.EventTrigger;
import org.watchman.main.sensors.AccelerometerMonitor;
import org.watchman.main.sensors.AmbientLightMonitor;
import org.watchman.main.sensors.BarometerMonitor;
import org.watchman.main.sensors.BumpMonitor;
import org.watchman.main.sensors.MicrophoneMonitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

@SuppressLint("HandlerLeak")
public class MonitorService extends Service {

    /**
     * Monitor instance
     */
    private static MonitorService sInstance;

    /**
     * To show a notification on service start
     */
    private NotificationManager manager;
    private NotificationChannel mChannel;
    private final static String channelId = "monitor_id";
    private final static CharSequence channelName = "watchman notifications";
    private final static String channelDescription= "Important messages from watchman";
	
    /**
     * Object used to retrieve shared preferences
     */
     private PreferenceManager mPrefs = null;

    /**
     * Sensor Monitors
     */
    private AccelerometerMonitor mAccelManager = null;
    private BumpMonitor mBumpMonitor = null;
    private MicrophoneMonitor mMicMonitor = null;
    private BarometerMonitor mBaroMonitor = null;
    private AmbientLightMonitor mLightMonitor = null;

    private boolean mIsRunning = false;

    /**
     * Last Event instances
     */
    private Event mLastEvent;

    /**
     * Last sent notification time
     */
    private Date mLastNotification;

    private long uploadTimestamp;

        /**
	 * Handler for incoming messages
	 */
    private class MessageHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			alert(msg.what,msg.getData().getString("path"));
		}
	}
		
	/**
	 * Messenger interface used by clients to interact
	 */
	private final Messenger messenger = new Messenger(new MessageHandler());

    /**
     * Helps keep the service awake when screen is off
     */
    private PowerManager.WakeLock wakeLock;

    /**
     * Application
     */
    private WatchMan mApp = null;

	/**
	 * Called on service creation, sends a notification
	 */
    @Override
    public void onCreate() {

        sInstance = this;

        mApp = (WatchMan) getApplication();

        manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mPrefs = new PreferenceManager(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(channelId, channelName,
                    NotificationManager.IMPORTANCE_HIGH);
            mChannel.setDescription(channelDescription);
            mChannel.setLightColor(Color.RED);
            mChannel.setImportance(NotificationManager.IMPORTANCE_MIN);
            manager.createNotificationChannel(mChannel);
        }

        startSensors();

        showNotification();

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();

        uploadTimestamp = System.currentTimeMillis();
        Log.d("stamp",uploadTimestamp+"");
    }

    public static MonitorService getInstance ()
    {
        return sInstance;
    }
    
    /**
     * Called on service destroy, cancels persistent notification
     * and shows a toast
     */
    @Override
    public void onDestroy() {

        wakeLock.release();
        stopSensors();
		stopForeground(true);

    }
	
    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }
    
    /**
     * Show a notification while this service is running.
     */
    @SuppressWarnings("deprecation")
	private void showNotification() {

    	Intent toLaunch = new Intent(getApplicationContext(),
    	                                          MonitorActivity.class);

        toLaunch.setAction(Intent.ACTION_MAIN);
        toLaunch.addCategory(Intent.CATEGORY_LAUNCHER);
        toLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        toLaunch,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.secure_service_started);

		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this, channelId)
						.setSmallIcon(R.drawable.ic_stat_haven)
						.setContentTitle(getString(R.string.app_name))
						.setContentText(text);

		mBuilder.setPriority(NotificationCompat.PRIORITY_MIN);
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_SECRET);

		startForeground(1, mBuilder.build());

    }

    public boolean isRunning ()
    {
        return mIsRunning;

    }

    private void startSensors ()
    {
        mIsRunning = true;

        if (!mPrefs.getAccelerometerSensitivity().equals(PreferenceManager.OFF)) {
            mAccelManager = new AccelerometerMonitor(this);
            if(Build.VERSION.SDK_INT>=18) {
                mBumpMonitor = new BumpMonitor(this);
            }
        }

        //moving these out of the accelerometer pref, but need to enable off prefs for them too
        mBaroMonitor = new BarometerMonitor(this);
        mLightMonitor = new AmbientLightMonitor(this);

        // && !mPrefs.getVideoMonitoringActive()

        if (!mPrefs.getMicrophoneSensitivity().equals(PreferenceManager.OFF))
            mMicMonitor = new MicrophoneMonitor(this);


    }

    private void stopSensors ()
    {
        mIsRunning = false;
        //this will never be false:
        // -you can't use ==, != for string comparisons, use equals() instead
        // -Value is never set to OFF in the first place
        if (!mPrefs.getAccelerometerSensitivity().equals(PreferenceManager.OFF)) {
            mAccelManager.stop(this);
            if(Build.VERSION.SDK_INT>=18) {
                mBumpMonitor.stop(this);
            }
        }

        //moving these out of the accelerometer pref, but need to enable off prefs for them too
        mBaroMonitor.stop(this);
        mLightMonitor.stop(this);

        // && !mPrefs.getVideoMonitoringActive())

        if (!mPrefs.getMicrophoneSensitivity().equals(PreferenceManager.OFF))
            mMicMonitor.stop(this);
    }

    /**
    * Sends an alert according to type of connectivity
    */
    public synchronized void alert(int alertType, String path) {

        Date now = new Date();
        boolean doNotification = false;
        boolean doUpload = false;

        if (mLastEvent == null) {
            mLastEvent = new Event();
            mLastEvent.save();
            doNotification = true;
        }
        else if (mPrefs.getNotificationTimeMs() == 0)
        {
            doNotification = true;
        }
        else if (mPrefs.getNotificationTimeMs() > 0 && mLastNotification != null)
        {
            //check if time window is within configured notification time window
            doNotification = ((now.getTime()-mLastNotification.getTime())>mPrefs.getNotificationTimeMs());
        }
        else
        {
            doNotification = true;
        }

        EventTrigger eventTrigger = new EventTrigger();
        eventTrigger.setType(alertType);
        eventTrigger.setPath(path);

        mLastEvent.addEventTrigger(eventTrigger);

        //we don't need to resave the event, only the trigger
        eventTrigger.save();

        if (System.currentTimeMillis() > uploadTimestamp+mPrefs.getFtpUploadTimeMs())
        {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Ftp ftp = new Ftp(mPrefs.getFtpUrl().trim(),21,mPrefs.getFtpAccount().trim(),mPrefs.getFtpPassword().trim());
                        File dir = Environment.getExternalStorageDirectory();
                        Log.d("test", "run: "+dir);
                        File test = new File(dir,"/phoneypot/ftp_upload.txt");
                        if (!test.exists()){
                            PrintStream ps = new PrintStream(new FileOutputStream(test));
                            ps.println("test for ftp upload");
                        }
                        String file_dected = eventTrigger.getPath().toString().split("/")[5];
                        Log.d("ftp",file_dected);
                        Boolean status = ftp.upload_file(eventTrigger.getPath(),"upload/"+file_dected,"hahhahah");

                        Log.d("ftp upload","success");


                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            });

            thread.start();

            uploadTimestamp = System.currentTimeMillis();
        }



        if (doNotification) {

            mLastNotification = new Date();
            /*
             * If SMS mode is on we send an SMS or Signal alert to the specified
             * number
             */
            StringBuilder alertMessage = new StringBuilder();
            alertMessage.append(getString(R.string.intrusion_detected, eventTrigger.getStringType(this)));

//            if (mPrefs.getSignalUsername() != null) {
//                //since this is a secure channel, we can add the Onion address
//                if (mPrefs.getRemoteAccessActive() && (!TextUtils.isEmpty(mPrefs.getRemoteAccessOnion()))) {
//                    alertMessage.append(" http://").append(mPrefs.getRemoteAccessOnion())
//                            .append(':').append(WebServer.LOCAL_PORT);
//                }
//
//                SignalSender sender = SignalSender.getInstance(this, mPrefs.getSignalUsername());
//                ArrayList<String> recips = new ArrayList<>();
//                StringTokenizer st = new StringTokenizer(mPrefs.getSmsNumber(), ",");
//                while (st.hasMoreTokens())
//                    recips.add(st.nextToken());
//
//                String attachment = null;
//                if (eventTrigger.getType() == EventTrigger.CAMERA) {
//                    attachment = eventTrigger.getPath();
//                } else if (eventTrigger.getType() == EventTrigger.MICROPHONE) {
//                    attachment = eventTrigger.getPath();
//                }
//                else if (eventTrigger.getType() == EventTrigger.CAMERA_VIDEO) {
//                    attachment = eventTrigger.getPath();
//                }
//
//                sender.sendMessage(recips, alertMessage.toString(), attachment);
//            } else if (mPrefs.getSmsActivation()) {
//                SmsManager manager = SmsManager.getDefault();
//
//                StringTokenizer st = new StringTokenizer(mPrefs.getSmsNumber(), ",");
//                while (st.hasMoreTokens())
//                    manager.sendTextMessage(st.nextToken(), null, alertMessage.toString(), null, null);
//
//            }

            Log.d("emailActiovaition",mPrefs.getEmailActivation()+"");



            if (mPrefs.getEmailActivation()){

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        Log.d("test","hahahha");
                        Log.d("test",mPrefs.getEmailUsername());
                        Log.d("test",mPrefs.getEmailUsername().trim().equals("myautosender@163.com")+"");
                        Log.d("test",mPrefs.getREmailUsername());
                        Log.d("test",mPrefs.getEmailPassword());
                        Log.d("test",mPrefs.getEmailPassword().trim().equals("laozi9yongmw")+"");

                        EmailSender sender = new EmailSender(mPrefs.getEmailUsername().trim(),mPrefs.getEmailPassword().trim());
                        try{
                            sender.sendMailwithAttachment("预警通知",alertMessage.toString(),mPrefs.getEmailUsername(),mPrefs.getREmailUsername(),eventTrigger.getPath().toString());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                }).start();
            }

            Log.d("begin",eventTrigger.getPath().toString());

            Log.d("begin","notication");
            Log.d("begin",alertMessage.toString());

        }

    }


}
