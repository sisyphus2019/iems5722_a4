package hk.edu.cuhk.ie.iems5722.a4_1155162616;

public class ChatroomBean {
    int id;
    String name;

    public ChatroomBean(){}

    public ChatroomBean(int id,String name){
        setId(id);
        setName(name);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
