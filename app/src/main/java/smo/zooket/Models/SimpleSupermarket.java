package smo.zooket.Models;

/**
 * Created by Smo on 5/7/2017.
 */
public class SimpleSupermarket {
    private int ID ;
    private String Name ;
    private String MainImg ;

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
}
