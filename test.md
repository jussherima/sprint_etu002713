```java
public class Controller{
    @Get(url="/hello")
    public String getsession(Session sess){
        return sess.get("nom");
    }

    @Get(url="/hello2")
    public String decl(Session s){
        s.save("olona");
        s.delete("olona");
    }

    
}





