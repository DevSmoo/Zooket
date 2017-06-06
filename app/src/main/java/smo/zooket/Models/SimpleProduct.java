package smo.zooket.Models;

/**
 * Created by Smo on 12/05/2017.
 */
public class SimpleProduct {
    private int ID;
    private String Name;
    private String MainImg;
    private String MainPrice;
    private String SuperPrice;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getMainImg() {
        return MainImg;
    }

    public void setMainImg(String mainImg) {
        MainImg = mainImg;
    }

    public String getMainPrice() {
        return MainPrice;
    }

    public void setMainPrice(String mainPrice) {
        MainPrice = mainPrice;
    }

    public String getSuperPrice() {
        return SuperPrice;
    }

    public void setSuperPrice(String superPrice) {
        SuperPrice = superPrice;
    }
}
