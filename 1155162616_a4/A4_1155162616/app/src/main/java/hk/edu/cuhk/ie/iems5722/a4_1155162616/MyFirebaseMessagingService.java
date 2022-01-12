package hk.edu.cuhk.ie.iems5722.a4_1155162616;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    private static int count =0;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG,"From:"+remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getTag(),
                    remoteMessage.getNotification().getBody());
            count = count+1;
        }
    }

    //Called when a new token for the default Firebase project is generated.
    @Override
    public void onNewToken(String token) {
        Log.d(TAG,"Refreshed token:"+token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // send token to your app server.
        String line = null;
        String result = new String();

        try{
            URL url = new URL(getString(R.string.get_tokensAPI));
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            OutputStream outputStream = connection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
            Uri.Builder builder = new Uri.Builder();

            //Build the parameters using ArrayList objects para_names and para_values
            builder.appendQueryParameter("user_id",getString(R.string.myID));
            builder.appendQueryParameter("token",token);
            String query = builder.build().getEncodedQuery();

            bufferedWriter.write(query);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();

            int response = connection.getResponseCode();
            if (response!=200){
                return;
            }

            InputStream inputStream = connection.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while((line = bufferedReader.readLine())!=null){
                result += line;
            }
            Log.d(TAG,"TokenToServerResult:"+result);
        }catch (Exception e){
            System.out.println((e.toString()));
        }
    }

    private void sendNotification(String chatroom_name,String chatroom_id,String messageBody) {
        Log.d(TAG,"sendNotification");
        // control what happens when the user taps on the notification.
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("id",chatroom_id);
        intent.putExtra("name",chatroom_name);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, count /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        //used to create a notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(chatroom_name)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(count /* ID of notification */, notificationBuilder.build());
    }


}