package hk.edu.cuhk.ie.iems5722.a4_1155162616;

public class MessageBean {
    private String message;
    private String user_name;
    private String message_time;
    private String user_id;
    private boolean isSelf;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getMessage_time() {
        return message_time;
    }

    public void setMessage_time(String message_time) {
        this.message_time = message_time;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public boolean getIsSelf(){
        return isSelf;
    }

    public void setIsSelf(boolean isSelf){
        this.isSelf = isSelf;
    }
}
