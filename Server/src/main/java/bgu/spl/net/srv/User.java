package bgu.spl.net.srv;

public abstract class User {
    private String username;
    private String password;
    private boolean isOnline;

    public User(String username, String password){
        this.username = username;
        this.password = password;
        this.isOnline = false;
    }

    //getter
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    //setter
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public boolean logUserIn(){
        //if user is already logged in return false
        if (this.isOnline){
            return false;
        }
        //else, log user in
        this.isOnline =true;
        return true;
    }
    public boolean logUserOut(){
        //if user is already logged out return false
        if (!this.isOnline){
            return false;
        }
        // else, log ues out
        this.isOnline = false;
        return true;
    }
    public boolean getIsOnline(){
        return isOnline;
    }
    public abstract String getType();

}
