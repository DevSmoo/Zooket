package smo.zooket.Models;

/**
 * Created by Smo on 22/05/2017.
 */
public class SimpleOrder {
    private int ID;
    private String Date;
    private String SupermarketName;
    private String Status;
    private String RahgiriCode;

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

    public String getSupermarketName() {
        return SupermarketName;
    }

    public void setSupermarketName(String supermarketName) {
        SupermarketName = supermarketName;
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
}
