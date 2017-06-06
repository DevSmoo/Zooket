package smo.zooket.Models;

/**
 * Created by Smo on 5/8/2017.
 */
public class User {
    private String Username;

    private String Password;
    private String NewPassword;
    private String Name;

    private int SupermarketID;

    private String Mobile;

    private String Role;

    private String Address;

    public String getRole() {
        return Role;
    }

    public void setRole(String role) {
        Role = role;
    }

    public String getMobile() {
        return Mobile;
    }

    public void setMobile(String mobile) {
        Mobile = mobile;
    }

    public int getSupermarketID() {
        return SupermarketID;
    }

    public void setSupermarketID(int supermarketID) {
        SupermarketID = supermarketID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getNewPassword() {
        return NewPassword;
    }

    public void setNewPassword(String newPassword) {
        NewPassword = newPassword;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }
}
