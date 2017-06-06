package smo.zooket.Models;

/**
 * Created by Smo on 06/06/2017.
 */
public class SearchProduct {
    private int ID;
    private String Name;
    private String MainImg;
    private String MainPrice;
    private String SuperPrice;
    private int SupermarketID;
    private String SupermarketName;

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

    public int getSupermarketID() {
        return SupermarketID;
    }

    public void setSupermarketID(int supermarketID) {
        SupermarketID = supermarketID;
    }

    public String getSupermarketName() {
        return SupermarketName;
    }

    public void setSupermarketName(String supermarketName) {
        SupermarketName = supermarketName;
    }
}
