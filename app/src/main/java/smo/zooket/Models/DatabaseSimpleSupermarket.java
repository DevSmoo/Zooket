package smo.zooket.Models;

import io.realm.RealmObject;

/**
 * Created by Smo on 5/10/2017.
 */
public class DatabaseSimpleSupermarket extends RealmObject {
    private int ID ;
    private String Name ;
    private String MainImg ;
    private String Province ;

    public String getMainImg() {
        return MainImg;
    }

    public void setMainImg(String mainImg) {
        MainImg = mainImg;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getProvince() {
        return Province;
    }

    public void setProvince(String province) {
        Province = province;
    }
}
