package smo.zooket.Models;

import java.util.List;

/**
 * Created by Smo on 22/05/2017.
 */
public class Attributes {
    private int ID;
    private String AttrName;
    private List<SubAttributes> ListSubAttr;

    public List<SubAttributes> getListSubAttr() {
        return ListSubAttr;
    }

    public void setListSubAttr(List<SubAttributes> listSubAttr) {
        ListSubAttr = listSubAttr;
    }

    public String getAttrName() {
        return AttrName;
    }

    public void setAttrName(String attrName) {
        AttrName = attrName;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }
}
