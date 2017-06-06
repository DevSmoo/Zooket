package smo.zooket.Models;

/**
 * Created by Smo on 17/05/2017.
 */
public class OrderTemp {
    private int ProductID;
    private int UserID;
    private int SupermarketID;
    private String SupermarketName;
    private String Order;
    private String CountsOrder;
    private String ClientPrice;
    private String SuperPrice;
    private String ProMainImg;

    public int getUserID() {
        return UserID;
    }

    public void setUserID(int userID) {
        UserID = userID;
    }

    public int getSupermarketID() {
        return SupermarketID;
    }

    public void setSupermarketID(int supermarketID) {
        SupermarketID = supermarketID;
    }

    public String getClientPrice() {
        return ClientPrice;
    }

    public void setClientPrice(String clientPrice) {
        ClientPrice = clientPrice;
    }

    public String getSuperPrice() {
        return SuperPrice;
    }

    public void setSuperPrice(String superPrice) {
        SuperPrice = superPrice;
    }

    public String getSupermarketName() {
        return SupermarketName;
    }

    public void setSupermarketName(String supermarketName) {
        SupermarketName = supermarketName;
    }

    public int getProductID() {
        return ProductID;
    }

    public void setProductID(int productID) {
        ProductID = productID;
    }

    public String getCountsOrder() {
        return CountsOrder;
    }

    public void setCountsOrder(String countsOrder) {
        CountsOrder = countsOrder;
    }

    public String getOrder() {
        return Order;
    }

    public void setOrder(String order) {
        Order = order;
    }

    public String getProMainImg() {
        return ProMainImg;
    }

    public void setProMainImg(String proMainImg) {
        ProMainImg = proMainImg;
    }
}
