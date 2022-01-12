package hk.edu.cuhk.ie.iems5722.a4_1155162616;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private TextView chatroomName;
    private EditText getMessageField;
    private ListView messageListView;

    private int next_page;
    private int total_pages;
    //数据
    private List<MessageBean> messageBeanList = new ArrayList<>();
    //适配器
    ChatMessageAdapter chatMessageAdapter;
    //所选择chatroom信息
    private String chatroom_id;
    private String chatroom_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //引入布局
        setContentView(R.layout.activity_chat);

        //获取View对象
        chatroomName = (TextView)findViewById(R.id.chatroomName);
        messageListView = (ListView)findViewById(R.id.messageList);

        //获得跳转intent传递的数据
        Bundle extras = getIntent().getExtras();
        chatroom_id = extras.getString("id");
        chatroom_name = extras.getString("name");

        //设置ChatroomName
        chatroomName.setText(chatroom_name);

        //配置适配器 关联ListView
        chatMessageAdapter = new ChatMessageAdapter(this,messageBeanList);
        messageListView.setAdapter(chatMessageAdapter);

        //用ShowMessageAsyncTask接入API获取PageOneMessage
        String urlStr = getString(R.string.get_messagesAPI)+'?'+"chatroom_id="+chatroom_id+"&"+"page=1";
        ShowMessageAsyncTask showMessageAsyncTask = new ShowMessageAsyncTask();
        showMessageAsyncTask.execute(urlStr);

        //给ListView加入OnScrollListener
        messageListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private boolean isFirstItem = false;

            //Callback method to be invoked when the list or grid has been scrolled
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(messageListView.getChildAt(firstVisibleItem)!=null){
                    isFirstItem = false;

                    int top = messageListView.getChildAt(firstVisibleItem).getTop();
                    if(firstVisibleItem == 0 && top == 0){
                        isFirstItem = true;
                    }
                }
            }

            //Callback method to be invoked while the list view or grid view is being scrolled
            //This will be called after the scroll has completed
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (isFirstItem && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
                    if(next_page <= total_pages){
                        String urlStr = getString(R.string.get_messagesAPI)+'?'+"chatroom_id="+chatroom_id+"&"+"page="+next_page;
                        ShowMessageAsyncTask showMessageAsyncTask = new ShowMessageAsyncTask();
                        showMessageAsyncTask.execute(urlStr);
                    }
                }
            }

        });

    }

    //返回按钮监听事件 回到MainActivity
    public void backMainActivity(View view) {

        startActivity(new Intent(this, MainActivity.class));
    }

    //更新按钮监听事件 回到信息列表首页
    public void refreshChatMessage(View view){
        //回到第一页
        String urlStr = getString(R.string.get_messagesAPI)+'?'+"chatroom_id="+chatroom_id+"&"+"page=1";
        ShowMessageAsyncTask showMessageAsyncTask = new ShowMessageAsyncTask();
        showMessageAsyncTask.execute(urlStr);
    }

    //发送信息按钮的监听事件
    public void sendMessage(View view) {
        //获取文本框及内容
        getMessageField = findViewById(R.id.getMessage);
        String text = getMessageField.getText().toString();
        //输入内容判空
        if (text.length()==0)
        {
            Toast.makeText(this, getString(R.string.sendInvalidHint), Toast.LENGTH_SHORT).show();
        }
        else{
            //MessageBean message user_name message_time user_id isSelf
            MessageBean messageBean = new MessageBean();
            messageBean.setMessage(text);
            //在此处设置时间
            messageBean.setMessage_time(getCurrentTime());

            //设置用户名及ID及isSelf
            messageBean.setUser_name(getString(R.string.myName));
            messageBean.setUser_id(getString(R.string.myID));
            messageBean.setIsSelf(true);

            //清空文本框
            getMessageField.setText("");

            //上传数据
            String urlStr = getString(R.string.send_messageAPI);
            SendMessageAsyncTask sendMessageAsyncTask = new SendMessageAsyncTask();
            sendMessageAsyncTask.execute(urlStr,chatroom_id+"",getString(R.string.myID),getString(R.string.myName),text);


            messageBeanList.add(messageBean);

            messageListView.setAdapter(new ChatMessageAdapter(ChatActivity.this,messageBeanList));
        }
    }

    //获取当前时间 返回规定格式字符串
    public String getCurrentTime(){
        SimpleDateFormat formatter = new SimpleDateFormat(getString(R.string.timeFormatWithoutYear));
        Date curDate = new Date(System.currentTimeMillis());
        //System.out.println(curDate);
        String curTime = formatter.format(curDate);
        //System.out.println(curTime);
        return curTime;
    }

    //将从/get_messages API获取的字符串转化为ListOfJSON结构数据并存储在ChatMessageListView的数据源MessageBeanList上
    public void get_messagesJSON(String json_string){
        try{
            JSONObject jsonObject = new JSONObject(json_string);

            MessageBean messageBean;

            //API状态检查
            String status = jsonObject.getString("status");
            if (!status.equals(getString(R.string.rightStatus))){
                System.out.println(getString(R.string.APIStatusHint));
                return;
            }

            //清除messageBeanList原有数据 否则会越来越多
            messageBeanList.clear();

            JSONObject data = jsonObject.getJSONObject("data");
            //获取存储在data里的信息 messages
            JSONArray array = data.getJSONArray("messages");
            //System.out.println(array.length());
            //获取存储Message信息 message name message_time user_id isSelf
            for (int i=array.length()-1;i>=0;i--){
                messageBean = new MessageBean();

                //message
                String message = array.getJSONObject(i).getString("message");
                messageBean.setMessage(message);

                //user_name
                String user_name = array.getJSONObject(i).getString("name");
                messageBean.setUser_name(user_name);

                //message_time
                String message_time = array.getJSONObject(i).getString("message_time");
                //统一时间格式为"HH:mm"
                SimpleDateFormat oldSdf = new SimpleDateFormat(getString(R.string.timeFormatWithYear));
                Date date = oldSdf.parse(message_time);
                SimpleDateFormat newSdf = new SimpleDateFormat(getString(R.string.timeFormatWithoutYear));
                String newTime = newSdf.format(date);
                messageBean.setMessage_time(newTime);

                //user_id
                String user_id = array.getJSONObject(i).getString("user_id");
                Log.d(TAG,"user_id:"+user_id);
                messageBean.setUser_id(user_id);
                messageBeanList.add(messageBean);

                //isSelf
                if (user_id.equals(getString(R.string.myID))){
                    messageBean.setIsSelf(true);
                }else {
                    messageBean.setIsSelf(false);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return;
    }

    //从/get_Message API中获取数据并显示的异步线程
    public class ShowMessageAsyncTask extends AsyncTask<String,Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            MyHttp myHttp = new MyHttp();
            String urlStr = strings[0];
            String result = myHttp.getHttp(urlStr);

            System.out.println(result);

            //获得该chatroomPage信息
            List<Integer> pageInfo = myHttp.get_pages(result);
            next_page = pageInfo.get(0)+1;
            total_pages = pageInfo.get(1);

            //显示当前页面
            System.out.println("current_page:"+pageInfo.get(0));

            return result;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            get_messagesJSON(result);

            messageListView.setAdapter(new ChatMessageAdapter(ChatActivity.this,messageBeanList));
        }
    }

    //向/send_message API上传数据
    public class SendMessageAsyncTask extends AsyncTask<String,Void,Boolean>{

        @Override
        protected Boolean doInBackground(String... strings) {
            MyHttp myHttp = new MyHttp();
            String urlStr = strings[0];
            List<String> params = new ArrayList<>();
            for (int i=1;i<=4;i++){
                params.add(strings[i]);
                System.out.println(strings[i]);
            }
            String result = myHttp.postHttp(urlStr,params);
            return myHttp.send_messagesStatus(result);
        }

        @Override
        protected void onPostExecute(Boolean isOK) {
            super.onPostExecute(isOK);
            if(isOK){
                Toast.makeText(ChatActivity.this,getString(R.string.sendSuccessedHint),Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(ChatActivity.this,getString(R.string.sendFailedHint),Toast.LENGTH_LONG).show();
            }
        }
    }
}


