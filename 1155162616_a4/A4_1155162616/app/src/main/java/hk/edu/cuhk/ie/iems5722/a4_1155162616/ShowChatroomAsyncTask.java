package hk.edu.cuhk.ie.iems5722.a4_1155162616;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ListView;

import java.util.List;

//实现网络的异步访问
//启动任务执行的输入参数 后台任务执行的进度 后台计算结果的类型
//execute(Params… params)，执行一个异步任务，需要我们在代码中调用此方法，触发异步任务的执行
public class ShowChatroomAsyncTask extends AsyncTask<String,Void,List<ChatroomBean>> {
    //传入一个context 为了adapter的执行
    private Context context;
    private ListView listView;
    public ShowChatroomAsyncTask(Context context,ListView listView){
        super();
        this.context = context;
        this.listView = listView;
    }

    @Override
    //返回结果给onPostExecute
    protected List<ChatroomBean> doInBackground(String... urlStr) {
        MyHttp myHttp = new MyHttp();
        String url = urlStr[0];
        String result = myHttp.getHttp(url);

        //验证接收数据是否正确
        //System.out.println(result);
        //字符串转换成ListOfJSON数据
        return myHttp.get_chatroomsJSON(result);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(List<ChatroomBean> chatroomBeans) {
        super.onPostExecute(chatroomBeans);
        //设置Adapter 关联ListView
        ChatroomAdapter adapter = new ChatroomAdapter(context,chatroomBeans);
        listView.setAdapter(adapter);
    }
}
