package ca.staugustinechs.staugustineapp;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ca.staugustinechs.staugustineapp.Activities.Main;

public class MessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "WOWTHISISACOOLCHANNELIDTHATMAKESSENSEMAN21";
    private static final String GROUP_KEY = "GROUPIDFORALLNOTIFICATIONSTODOWITHTHISAPP21";
    private static String msgToken;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MessagingService.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(remoteMessage.getNotification().getTitle())
               // .setContentText(remoteMessage.getNotification().getBody())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(remoteMessage.getNotification().getBody()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setColor(getColor(R.color.colorAccent))
                .setVibrate(new long[]{1000})
                .setLights(getColor(R.color.colorAccent), 1000, 5000)
                .setGroup(MessagingService.GROUP_KEY);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify((int) Math.floor(Math.random() * 1000), builder.build());
    }

    public static void createNotificationChannel(Activity activity) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "St. Augustine CHS Notifications", importance);
            channel.setDescription("All notifications regarding the St. Augustine CHS App");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = activity.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void sendMessage(String topic, String clubName, String title, String body, OnCompleteListener listener){
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("clubID", topic);
        data.put("clubName", clubName);
        data.put("title", title);
        data.put("body", body);

        FirebaseFunctions.getInstance().getHttpsCallable("sendToTopic").call(data)
                .addOnCompleteListener(listener);
    }

    public static void sendMessageToUser(String msgToken, String title, String body){
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("token", msgToken);
        data.put("title", title);
        data.put("body", body);

        FirebaseFunctions.getInstance().getHttpsCallable("sendToUser").call(data);
    }

    public static void subscribe(String topic, OnCompleteListener listener){
        FirebaseMessaging.getInstance().subscribeToTopic(topic);
        FirebaseFirestore.getInstance().collection("users")
                .document(Main.PROFILE.getUid()).update("notifications", FieldValue.arrayUnion(topic))
                .addOnCompleteListener(listener);
    }

    public static void subscribe(String topic, String userId, String messageId, OnCompleteListener listener){
        manageSubscription(topic, userId, messageId, true, listener);
    }

    public static void unsubscribe(String topic, OnCompleteListener listener){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
        FirebaseFirestore.getInstance().collection("users")
                .document(Main.PROFILE.getUid()).update("notifications", FieldValue.arrayRemove(topic))
                .addOnCompleteListener(listener);
    }

    public static void unsubscribe(String topic, String userId, String messageId, OnCompleteListener listener){
        manageSubscription(topic, userId, messageId, false, listener);
    }

    private static void manageSubscription(final String topic, final String userId, String messageId,
                                           final boolean subscribing, final OnCompleteListener listener){
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("registrationTokens", Arrays.asList(messageId));
        data.put("isSubscribing", subscribing);
        data.put("clubID", topic);

        FirebaseFunctions.getInstance().getHttpsCallable("manageSubscriptions")
                .call(data).addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
            @Override
            public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                if(task.isSuccessful()){
                    if(subscribing){
                        FirebaseFirestore.getInstance().collection("users")
                                .document(userId).update("notifications", FieldValue.arrayUnion(topic))
                                .addOnCompleteListener(listener);
                    }else{
                        FirebaseFirestore.getInstance().collection("users")
                                .document(userId).update("notifications", FieldValue.arrayRemove(topic))
                                .addOnCompleteListener(listener);
                    }
                }
            }
        });
    }

    public static void registerToken(){
        registerToken(msgToken);
    }

    public static void registerToken(final String token){
        FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("info").document("vital")
                .update("msgToken", token)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(Main.PROFILE.getNotifications() != null){
                            for(String topic : Main.PROFILE.getNotifications()){
                                MessagingService.subscribe(topic, null);
                            }
                        }
                        FirebaseMessaging.getInstance().subscribeToTopic("alerts");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        registerToken(token);
                    }
                });
    }

    @Override
    public void onNewToken(String s) {
        /*msgToken = s;
        Main.REGISTER_TOKEN = true;*/
    }
}
