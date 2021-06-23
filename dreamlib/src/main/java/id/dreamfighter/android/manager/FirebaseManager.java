package id.dreamfighter.android.manager;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import id.dreamfighter.android.log.Logger;
import id.dreamfighter.android.utils.CommonUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtilLight;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;


public class FirebaseManager {
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    //private GoogleCloudMessaging gcm;
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

    public FirebaseManager(Context context, String senderId, String appVersion, SharedPreferences prefs){
        this.context = context;
        this.senderId = senderId;
        this.prefs = prefs;
        this.appVersion = appVersion;
    }
    
    public static void removeRegistrationId(SharedPreferences prefs){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, "");
        editor.commit();
    }
    
    public void requestRegistrationId(){
        registerInBackground();
        
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

        FirebaseInstallations.getInstance().getId().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w("FCM", "getInstanceId failed", task.getException());
            }else {

                // Get new Instance ID token
                String token = task.getResult();

                // Log and toast
                //String msg = getString (R.string.msg_token_fmt, token);
                Log.d("FCM", token);
                //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();

                //FirebaseMessaging.getInstance().subscribeToTopic(user?.uid.toString())
                if (gcmListener != null){
                    gcmListener.onRegistrationSuccess(token);
                }

                // You should send the registration ID to your server over HTTP,
                // so it can use GCM/HTTP or CCS to send messages to your app.
                // The request to your server should be authenticated if your app
                // is using accounts.
                sendRegistrationIdToBackend();

                subscribeTopics("global");

            }
        });
        
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
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param topic push notification topic
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    public void subscribeTopics(String topic) {
        //GcmPubSub pubSub = GcmPubSub.getInstance(context);
        
        //pubSub.subscribe(token, "/topics/" + topic, null);

        FirebaseMessaging.getInstance().subscribeToTopic(topic);
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
