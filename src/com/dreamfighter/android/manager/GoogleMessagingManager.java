package com.dreamfighter.android.manager;


import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.dreamfighter.android.log.Logger;
import com.dreamfighter.android.utils.CommonUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;


public class GoogleMessagingManager {
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private GoogleCloudMessaging gcm;
    private Context context;
    private String regid;
    private String senderId;
    private SharedPreferences prefs;
    private String appVersion;
    private GCMListener gcmListener;
    
    public static interface GCMListener{
        void onRegistrationSuccess(String registrationId);
        void onRegistered(String registrationId);
    }
    
    public GoogleMessagingManager(Context context, String senderId, String appVersion, SharedPreferences prefs){
        this.context = context;
        this.senderId = senderId;
        this.prefs = prefs;
        this.appVersion = appVersion;
    }
    
    public void requestRegistrationId(){
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(context);
            regid = getRegistrationId(context, prefs);

            if ("".equals(regid)) {
                registerInBackground();
            }else{
                if(!"".equals(regid) && gcmListener!=null){
                    gcmListener.onRegistered(regid);
                }
            }
            //mDisplay.setText(regid);
            Logger.log("RegistrationId="+regid);
        } else {
            Logger.log("No valid Google Play Services APK found.");
        }
    }
    
    public boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, (Activity)context, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Logger.log("This device is not supported.");
            }
            return false;
        }
        return true;
    }
    
    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    public String getRegistrationId(Context context, SharedPreferences prefs) {
        //final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if ("".equals(registrationId)) {
            Logger.log("Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(appVersion, Integer.MIN_VALUE);
        
        int currentVersion = CommonUtils.getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Logger.log( "App version changed.");
            return "";
        }
        return registrationId;
    }
    
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    Logger.log("Register with sender id="+senderId);
                    regid = gcm.register(senderId);
                    Logger.log("Register success");
                    msg = "Device registered, registration ID=" + regid;
                    
                    if(!"".equals(regid) && gcmListener!=null){
                        gcmListener.onRegistrationSuccess(regid);
                    }

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid, prefs);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Logger.log(msg);
                //mDisplay.append(msg + "\n");
            }
        }.execute();
        
    }
    
    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId, SharedPreferences prefs) {
        //final SharedPreferences prefs = getGCMPreferences(context);
        Logger.log("context " + context);
        int appVersion = CommonUtils.getAppVersion(context);
        Logger.log("Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(this.appVersion, appVersion);
        editor.commit();
    }
    
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    }

    public GCMListener getGcmListener() {
        return gcmListener;
    }

    public void setGcmListener(GCMListener gcmListener) {
        this.gcmListener = gcmListener;
    }
}
