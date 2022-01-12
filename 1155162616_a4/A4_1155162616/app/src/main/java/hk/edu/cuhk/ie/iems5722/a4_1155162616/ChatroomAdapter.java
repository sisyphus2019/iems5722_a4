package hk.edu.cuhk.ie.iems5722.a4_1155162616;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ChatroomAdapter extends BaseAdapter{
    //布局资源
    private LayoutInflater inflater;
    //数据源
    private List<ChatroomBean> chatroomBeanList;

    public ChatroomAdapter(){}

    public ChatroomAdapter(Context context,List<ChatroomBean> chatroomBeans) {
        this.chatroomBeanList = chatroomBeans;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return chatroomBeanList.size();
    }

    @Override
    public Object getItem(int position) {
        return chatroomBeanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.chatroomlist_item,null);

            holder.chatroomTv = (TextView) convertView.findViewById(R.id.chatroomTv);
            //设置按钮透明度
            //holder.chatroomTv.getBackground().setAlpha(50);

            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)convertView.getTag();
        }
        //输出
        holder.chatroomTv.setText((chatroomBeanList.get(position).getName()));

        return convertView;
    }

    //缓冲池
    public final class ViewHolder{
        public TextView chatroomTv;
    }
}
