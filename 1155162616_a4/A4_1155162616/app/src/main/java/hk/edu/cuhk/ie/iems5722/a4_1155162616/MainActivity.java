package hk.edu.cuhk.ie.iems5722.a4_1155162616;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GoogleApiAvailabilityLight;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

//默认的activity
public class MainActivity extends AppCompatActivity{
    private List<ChatroomBean> chatrooms = new ArrayList<>();

    private static final String TAG = "MainActivity";

    ListView chatroomListView;
    Button logTokenBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"MainActivity Start!");
        //获取app现有token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        // Get new FCM registration token
                        String token = task.getResult();
                        // Log and toast
                        Log.d(TAG, "FCM registration Token:"+token);
                    }
                });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check google play services
        isGooglePlayServicesAvailable(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }


        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }
        // [END handle_data_extras]


        //获取ListView对象
        chatroomListView = findViewById(R.id.chatroomListView);

        //用ShowChatroomAsyncTask执行更新聊天室列表
        ShowChatroomAsyncTask showChatrooms = new ShowChatroomAsyncTask(MainActivity.this,chatroomListView);
        showChatrooms.execute(getString(R.string.get_chatroomsAPI));

        //给每个ListView生成的TextViwe创建监听事件
        this.chatroomListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatroomBean chatroomBean = (ChatroomBean)chatroomListView.getItemAtPosition(position);
                //System.out.println(chatroomBean.getId());
                String chatroom_id = String.valueOf(chatroomBean.getId());
                String chatroom_name = chatroomBean.getName();
                //System.out.println(chatroom_id+chatroom_name);

                //传递所选择chatroom信息给chatActivity
                Intent intent = new Intent(MainActivity.this,ChatActivity.class);
                intent.putExtra("id",chatroom_id);
                intent.putExtra("name",chatroom_name);

                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //check google play services
        isGooglePlayServicesAvailable(this);
    }

    //Check for Google Play Services
    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 9000).show();
            }
            return false;
        }
        return true;
    }

}
