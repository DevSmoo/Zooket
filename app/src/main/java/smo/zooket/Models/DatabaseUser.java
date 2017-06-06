package smo.zooket.Models;

import io.realm.RealmObject;

/**
 * Created by Smo on 5/6/2017.
 */

public class DatabaseUser extends RealmObject {

    private String Username;

    private String Password;

    private String Name;

    private int SupermarketID;

    private String Mobile;

    private String Role;

    private String Address;

    public int getSupermarketID() {
        return SupermarketID;
    }

    public void setSupermarketID(int supermarketID) {
        SupermarketID = supermarketID;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getMobile() {
        return Mobile;
    }

    public void setMobile(String mobile) {
        Mobile = mobile;
    }

    public String getRole() {
        return Role;
    }

    public void setRole(String role) {
        Role = role;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }
}
