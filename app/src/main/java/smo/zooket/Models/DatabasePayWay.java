package smo.zooket.Models;

import io.realm.RealmObject;

/**
 * Created by Smo on 21/05/2017.
 */
public class DatabasePayWay extends RealmObject {
    private int ID;
    private int SuperID;
    private Boolean PayCash;
    private Boolean PayPos;
    private Boolean PayElec;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getSuperID() {
        return SuperID;
    }

    public void setSuperID(int superID) {
        SuperID = superID;
    }

    public Boolean getPayCash() {
        return PayCash;
    }

    public void setPayCash(Boolean payCash) {
        PayCash = payCash;
    }

    public Boolean getPayPos() {
        return PayPos;
    }

    public void setPayPos(Boolean payPos) {
        PayPos = payPos;
    }

    public Boolean getPayElec() {
        return PayElec;
    }

    public void setPayElec(Boolean payElec) {
        PayElec = payElec;
    }
}
