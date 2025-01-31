package authentification;

public interface UserInterface {
    public String getUsername();

    public String getPassword();

    public default String getRole() {
        return "";
    };
}
