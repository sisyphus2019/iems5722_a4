package hk.edu.cuhk.ie.iems5722.a4_1155162616;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyHttp {
    //gethttp请求 返回字符串
    public  String getHttp(String urlStr){
        try{
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            //start the query

            int response = connection.getResponseCode();
            if (response!=200){

                return null;
            }

            InputStream inputStream = connection.getInputStream();

            //convert the inputStream into a string
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            StringBuffer stringBuffer = new StringBuffer();
            while((line = in.readLine())!=null){
                stringBuffer.append(line);
            }
            return stringBuffer.toString();
        }catch(Exception e){
            System.out.println((e.toString()));
            return null;
        }
    }

    //posthttp请求
    public String postHttp(String urlStr,List<String> params){
        String line = null;
        String result = new String();

        try{
            URL url = new URL(urlStr);
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
            builder.appendQueryParameter("chatroom_id",params.get(0));
            builder.appendQueryParameter("user_id",params.get(1));
            builder.appendQueryParameter("name",params.get(2));
            builder.appendQueryParameter("message",params.get(3));

            String query = builder.build().getEncodedQuery();

            bufferedWriter.write(query);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();

            int response = connection.getResponseCode();
            if (response!=200){

                return null;
            }

            InputStream inputStream = connection.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while((line = bufferedReader.readLine())!=null){
                result += line;
            }

        }catch (Exception e){
            System.out.println((e.toString()));
            return null;
        }

        return result;
    }

    //从/get_chatrooms API获取字符串后转化为ListOfJSON结构数据
    public List<ChatroomBean> get_chatroomsJSON(String json_string){
        List<ChatroomBean> chatroomBeans = new ArrayList<>();
        try{
            JSONObject jsonObject = new JSONObject(json_string);

            ChatroomBean chatroomBean;

            //API状态检查
            String status = jsonObject.getString("status");
            if (!status.equals("OK")){
                System.out.println("API ERROR!");
                return null;
            }

            JSONArray array = jsonObject.getJSONArray("data");
            for (int i=0;i<array.length();i++){
                chatroomBean = new ChatroomBean();
                int id = array.getJSONObject(i).getInt("id");
                chatroomBean.setId(id);
                String name = array.getJSONObject(i).getString("name");
                chatroomBean.setName(name);
                chatroomBeans.add(chatroomBean);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return chatroomBeans;
    }

    //从/get_messages API获取字符串后获得page数据
    public List<Integer> get_pages(String json_string){
        List<Integer> pageInfo = new ArrayList<>();
        try{
            JSONObject jsonObject = new JSONObject(json_string);
            //API状态检查
            String status = jsonObject.getString("status");
            if (!status.equals("OK")){
                System.out.println("API ERROR!");
                return null;
            }

            JSONObject data = jsonObject.getJSONObject("data");
            //获取存储在data里的信息 current_page messages total_page
            int currentPage = data.getInt("current_page");
            int totalPages = data.getInt("total_pages");
            pageInfo.add(currentPage);
            pageInfo.add(totalPages);
        }catch (Exception e){
            e.printStackTrace();
        }
        return pageInfo;
    }

    //从/get_messages API获取字符串后转化为ListOfJSON结构数据
    public List<MessageBean> get_messagesJSON(String json_string){
        List<MessageBean> messageBeans = new ArrayList<>();
        try{
            JSONObject jsonObject = new JSONObject(json_string);

            MessageBean messageBean;

            //API状态检查
            String status = jsonObject.getString("status");
            if (!status.equals("OK")){
                System.out.println("API ERROR!");
                return null;
            }

            JSONObject data = jsonObject.getJSONObject("data");
            //获取存储在data里的信息 messages
            JSONArray array = data.getJSONArray("messages");
            //System.out.println(array.length());
            //获取存储Message信息 message name message_time user_id
            for (int i=0;i<array.length();i++){
                messageBean = new MessageBean();
                //message
                String message = array.getJSONObject(i).getString("message");
                System.out.println(message);
                messageBean.setMessage(message);

                //user_name
                String user_name = array.getJSONObject(i).getString("name");
                if (user_name.equals(R.string.myName)){
                    messageBean.setIsSelf(true);
                }else {
                    messageBean.setIsSelf(false);
                }
                messageBean.setUser_name(user_name);

                //message_time
                String message_time = array.getJSONObject(i).getString("message_time");
                //统一时间格式为"HH:mm"
                SimpleDateFormat oldSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date date = oldSdf.parse(message_time);
                SimpleDateFormat newSdf = new SimpleDateFormat("HH:mm");
                String newTime = newSdf.format(date);
                messageBean.setMessage_time(newTime);

                //user_id
                String user_id = array.getJSONObject(i).getString("user_id");
                messageBean.setUser_id(user_id);
                messageBeans.add(messageBean);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return messageBeans;
    }

    //上传数据给/send_messages API后获取的状态信息 字符串
    public boolean send_messagesStatus(String json_string){
        boolean isOK = true;
        if(json_string!=null){
            try{
                JSONObject jsonObject = new JSONObject(json_string);
                String status = jsonObject.getString("status");
                if (!status.equals("OK")){
                    isOK = false;
                    System.out.println("API ERROR!");
                    return isOK;
                }

            }catch (Exception e){
                System.out.println(e.toString());
            }
        }
        return isOK;
    }
}
