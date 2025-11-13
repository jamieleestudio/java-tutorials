package management;

public class UserManager implements UserManagerMBean{


    private final User user;

    public UserManager(User user){
        this.user = user;
    }

    @Override
    public Long getId() {
        return user.getId();
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public void setId(Long id) {
        user.setId(id);
    }

    @Override
    public void setName(String name) {
        user.setName(name);
    }

    @Override
    public void setPassword(String password) {
        user.setPassword(password);
    }

    @Override
    public String toString(){
        return user.toString();
    }

}
