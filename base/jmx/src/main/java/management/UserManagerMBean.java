package management;

public interface UserManagerMBean {

    Long getId();

    String getName();

    String getPassword();


    void setId(Long id);

    void setName(String name);

    void setPassword(String password);

    String toString();
}
