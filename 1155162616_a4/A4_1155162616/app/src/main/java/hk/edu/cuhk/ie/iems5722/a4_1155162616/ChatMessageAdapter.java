package hk.edu.cuhk.ie.iems5722.a4_1155162616;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class ChatMessageAdapter extends BaseAdapter {

    //布局资源
    private LayoutInflater inflater;
    //数据源
    private List<MessageBean> messageBeanList = new ArrayList<>();

    public ChatMessageAdapter(){}

    public ChatMessageAdapter(Context context, List<MessageBean> messageBeans){
        this.messageBeanList = messageBeans;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount(){
        return messageBeanList.size();
    }

    @Override
    public Object getItem(int position) {
        return messageBeanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (messageBeanList.get(position).getIsSelf()) {
            convertView = inflater.inflate(R.layout.message_right_item,null);
        }else{
            convertView = inflater.inflate(R.layout.message_left_item,null);
        }
        TextView name = (TextView)convertView.findViewById(R.id.username);
        TextView message = (TextView)convertView.findViewById(R.id.message);
        TextView message_time = (TextView)convertView.findViewById(R.id.message_time);
        name.setText("User:"+messageBeanList.get(position).getUser_name());
        message.setText(messageBeanList.get(position).getMessage());
        message_time.setText(messageBeanList.get(position).getMessage_time());

        return convertView;
    }

}
