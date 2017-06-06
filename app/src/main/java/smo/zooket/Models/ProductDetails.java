package smo.zooket.Models;

/**
 * Created by Smo on 13/05/2017.
 */
public class ProductDetails {
    private int ID ;

    private String Name ;

    private String Company ;

    private String Category ;

    private String Price ;

    private String OffPrice ;

    private String MainImage ;

    private String Description ;

    private String Attributes ;

    private String Features ;

    private int LikeCount ;

    private int CommentCount ;

    private Boolean IsLiked ;

    private Boolean IsAvailable ;

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

    public String getCompany() {
        return Company;
    }

    public void setCompany(String company) {
        Company = company;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getOffPrice() {
        return OffPrice;
    }

    public void setOffPrice(String offPrice) {
        OffPrice = offPrice;
    }

    public String getMainImage() {
        return MainImage;
    }

    public void setMainImage(String mainImage) {
        MainImage = mainImage;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getAttributes() {
        return Attributes;
    }

    public void setAttributes(String attributes) {
        Attributes = attributes;
    }

    public String getFeatures() {
        return Features;
    }

    public void setFeatures(String features) {
        Features = features;
    }

    public int getLikeCount() {
        return LikeCount;
    }

    public void setLikeCount(int likeCount) {
        LikeCount = likeCount;
    }

    public int getCommentCount() {
        return CommentCount;
    }

    public void setCommentCount(int commentCount) {
        CommentCount = commentCount;
    }

    public Boolean getLiked() {
        return IsLiked;
    }

    public void setLiked(Boolean liked) {
        IsLiked = liked;
    }

    public Boolean getAvailable() {
        return IsAvailable;
    }

    public void setAvailable(Boolean available) {
        IsAvailable = available;
    }
}
