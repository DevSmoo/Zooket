package smo.zooket.Models;

/**
 * Created by Smo on 22/05/2017.
 */
public class Order {
    private int ID;
    private String Date;
    private String ClientName;
    private String SupermarketName;
    private String Orders;
    private String CountsOrders;
    private String ClientPrice;
    private String SuperPrice;
    private String NewAddress;
    private String Status;
    private String RahgiriCode;
    private String PayWay;
    private String Factor;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getClientName() {
        return ClientName;
    }

    public void setClientName(String clientName) {
        ClientName = clientName;
    }

    public String getSupermarketName() {
        return SupermarketName;
    }

    public void setSupermarketName(String supermarketName) {
        SupermarketName = supermarketName;
    }

    public String getOrders() {
        return Orders;
    }

    public void setOrders(String orders) {
        Orders = orders;
    }

    public String getCountsOrders() {
        return CountsOrders;
    }

    public void setCountsOrders(String countsOrders) {
        CountsOrders = countsOrders;
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

    public String getNewAddress() {
        return NewAddress;
    }

    public void setNewAddress(String newAddress) {
        NewAddress = newAddress;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getRahgiriCode() {
        return RahgiriCode;
    }

    public void setRahgiriCode(String rahgiriCode) {
        RahgiriCode = rahgiriCode;
    }

    public String getPayWay() {
        return PayWay;
    }

    public void setPayWay(String payWay) {
        PayWay = payWay;
    }

    public String getFactor() {
        return Factor;
    }

    public void setFactor(String factor) {
        Factor = factor;
    }
}
