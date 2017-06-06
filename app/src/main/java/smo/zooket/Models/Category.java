package smo.zooket.Models;

/**
 * Created by Smo on 15/05/2017.
 */
public class Category {
    private int ID;
    private String Name;
    private int ParentID;

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

    public int getParentID() {
        return ParentID;
    }

    public void setParentID(int parentID) {
        ParentID = parentID;
    }
}
