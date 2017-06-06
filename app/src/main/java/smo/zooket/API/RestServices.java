package smo.zooket.API;

import org.w3c.dom.Attr;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Query;
import smo.zooket.Models.Attributes;
import smo.zooket.Models.Category;
import smo.zooket.Models.Comment;
import smo.zooket.Models.GpsSupermarket;
import smo.zooket.Models.Order;
import smo.zooket.Models.ProductDetails;
import smo.zooket.Models.SearchProduct;
import smo.zooket.Models.SimpleOrder;
import smo.zooket.Models.SimpleProduct;
import smo.zooket.Models.SimpleSupermarket;
import smo.zooket.Models.SupermarketDetails;
import smo.zooket.Models.User;

/**
 * Created by Smo on 5/7/2017.
 */
public interface RestServices {

    @Headers("User-Agent: SupermarketApp")
    @GET("/Supermarket")
    void getFeaturedItem(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key, @Query("currentpage") int currentpage, @Query("typenull") int typenull, Callback<List<SimpleSupermarket>> callback);

    //user
    @Headers("User-Agent: SupermarketApp")
    @FormUrlEncoded
    @POST("/UserLogin")
    void UserLogin(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key, @Field("Username") String username, @Field("Password") String password, Callback<User> callback);

    @Headers("User-Agent: SupermarketApp")
    @FormUrlEncoded
    @POST("/UserRegister")
    void UserRegister(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key, @Field("Name") String name, @Field("Username") String username, @Field("Password") String password, @Field("Mobile") String mobile, Callback<Integer> callback);

    @Headers("User-Agent: SupermarketApp")
    @FormUrlEncoded
    @POST("/UserForgetPassword")
    void UserForgetPass(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key, @Field("Username") String username, Callback<Integer> callback);

    @Headers("User-Agent: SupermarketApp")
    @FormUrlEncoded
    @POST("/UserProfile")
    void UserProfile(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key, @Field("Username") String username, @Field("Password") String password, @Field("NewPassword") String newpassword, @Field("Name") String name, @Field("Mobile") String mobile, @Field("Address") String address, Callback<Boolean> callback);

    @Headers("User-Agent: SupermarketApp")
    @FormUrlEncoded
    @POST("/UserProfile")
    void UserProfile1(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key, @Field("Username") String username, @Field("Password") String password, @Field("NewPassword") String newpassword, @Field("Name") String name, @Field("Mobile") String mobile, @Field("Address") String address, Callback<String> callback);


    //supermarket
    @Headers("User-Agent: SupermarketApp")
    @GET("/Supermarket")
    void getSupermarketDetails(@Header("TS") String TS, @Header("ID") String ID, @Header("SupermarketAPIKey") String Key, @Query("id") int id, Callback<SupermarketDetails> callback);


    @Headers("User-Agent: SupermarketApp")
    @FormUrlEncoded
    @POST("/SuperLike")
    void UserLike(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key, @Field("Username") String username, @Field("SuperProID") int id, Callback<Boolean> callback);

    @Headers("User-Agent: SupermarketApp")
    @FormUrlEncoded
    @POST("/SuperDisLike")
    void UserDisLike(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key, @Field("Username") String username, @Field("SuperProID") int id, Callback<Boolean> callback);
    @Headers("User-Agent: SupermarketApp")
    @FormUrlEncoded
    @POST("/SuperComment")
    void SendReport(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key, @Field("SupermarketID") int supermarketID, @Field("Body") String body, Callback<Boolean> callback);


    @Headers("User-Agent: SupermarketApp")
    @FormUrlEncoded
    @POST("/SuperComment")
    void SendComment(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key, @Field("Username") String username, @Field("SuperProID") int superproID, @Field("Body") String body, Callback<Boolean> callback);

    @Headers("User-Agent: SupermarketApp")
    @FormUrlEncoded
    @POST("/SuperComment")
    void SendComment1(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key, @Field("Username") String username, @Field("SuperProID") int superproID, @Field("Body") String body, Callback<String> callback);

    @Headers("User-Agent: SupermarketApp")
    @GET("/SuperComment")
    void GetComment(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key, @Query("currentpage") int currentpage, @Query("id") int id, Callback<List<Comment>> callback);


    //products
    @Headers("User-Agent: SupermarketApp")
    @GET("/Product")
    void getProductItem(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key, @Query("currentpage") int currentpage, @Query("id") int id, Callback<List<SimpleProduct>> callback);

    @Headers("User-Agent: SupermarketApp")
    @GET("/Product")
    void getProductItem1(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key, @Query("currentpage") int currentpage, @Query("id") int id, Callback<String> callback);

    @Headers("User-Agent: SupermarketApp")
    @GET("/Product")
    void getProductsDetails(@Header("TS") String TS, @Header("ID") String ID, @Header("SupermarketAPIKey") String Key, @Query("id") int id, Callback<ProductDetails> callback);

    @Headers("User-Agent: SupermarketApp")
    @FormUrlEncoded
    @POST("/ProductLike")
    void UserProLike(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key, @Field("Username") String username, @Field("SuperProID") int id, Callback<Boolean> callback);

    @Headers("User-Agent: SupermarketApp")
    @FormUrlEncoded
    @POST("/ProductDisLike")
    void UserProDisLike(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key, @Field("Username") String username, @Field("SuperProID") int id, Callback<Boolean> callback);

    @Headers("User-Agent: SupermarketApp")
    @GET("/ProductComment")
    void GetProComment(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key, @Query("currentpage") int currentpage, @Query("id") int id, Callback<List<Comment>> callback);

    @Headers("User-Agent: SupermarketApp")
    @FormUrlEncoded
    @POST("/ProductComment")
    void SendProComment(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key, @Field("Username") String username, @Field("SuperProID") int superproID, @Field("Body") String body, Callback<Boolean> callback);


    //Cat & SubCat
    @Headers("User-Agent: SupermarketApp")
    @GET("/Category")
    void GetCatgeory(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key, Callback<List<Category>> callback);

    @Headers("User-Agent: SupermarketApp")
    @GET("/SubCategory")
    void GetSubCatgeory(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key, Callback<List<Category>> callback);

    @Headers("User-Agent: SupermarketApp")
    @GET("/SubCategory")
    void GetFirstSubCatgeory(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key, @Query("nu") int nu, Callback<List<Category>> callback);

    @Headers("User-Agent: SupermarketApp")
    @GET("/Product")
    void GetProductCatgeory(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key, @Query("CategoryName") String categoryName, @Query("SubCategoryName") String subcategoryName, @Query("SpermarketID") int spermarketID, Callback<List<SimpleProduct>> callback);

    //order
    @Headers("User-Agent: SupermarketApp")
    @FormUrlEncoded
    @POST("/Order")
    void SaveOrder(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key,
                    @Field("UserID") String UserID,
                    @Field("SupermarketID") int SupermarketID,
                    @Field("Orders") String Orders,
                    @Field("CountsOrders") String CountsOrders,
                    @Field("ClientPrice") String ClientPrice,
                    @Field("SuperPrice") String SuperPrice,
                    @Field("NewAddress") String NewAddress,
                    @Field("Cash") Boolean Cash,
                    @Field("Elec") Boolean Elec,
                    @Field("Pos") Boolean Pos,
                    @Field("Factor") Boolean Factor,
                    Callback<Boolean> callback);

    @Headers("User-Agent: SupermarketApp")
    @GET("/Order")
    void GetAllFactor(@Header("TS") String TS, @Header("ID") String ID,@Header("SupermarketAPIKey") String Key, @Query("currentpage") int currentpage,@Query("nu") String nu, Callback<List<SimpleOrder>> callback);

    @Headers("User-Agent: SupermarketApp")
    @GET("/Order")
    void GetFactorDetails(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key,@Query("id") int id, Callback<Order> callback);

    @Headers("User-Agent: SupermarketApp")
    @GET("/Order")
    void SearchFactors(@Header("TS") String TS, @Header("ID") String ID, @Header("SupermarketAPIKey") String Key,@Query("fromdate") String fromdate,@Query("todate") String todate,@Query("supermarketname") String supermarketname, Callback<List<SimpleOrder>> callback);

    //Attr
    @Headers("User-Agent: SupermarketApp")
    @GET("/GetAttributes")
    void GetAttr(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key,Callback<List<Attributes>> callback);

//search in supermarket
@Headers("User-Agent: SupermarketApp")
@GET("/Search")
void SearchProductSupermarket(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key,
                   @Query("Attributes") String Attributes,
                   @Query("FromPrice") String FromPrice,
                   @Query("ToPrice") String ToPrice,
                   @Query("NameProduct") String NameProduct,
                   @Query("Category") String Category,
                   @Query("SubCategory") String SubCategory,
                   @Query("SuperID") int SuperID,
                   @Query("IsAvailable") Boolean IsAvailable,Callback<List<SimpleProduct>> callback);
    //search all
    @Headers("User-Agent: SupermarketApp")
    @GET("/Search")
    void SearchProduct(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key,
                       @Query("Attributes") String Attributes,
                       @Query("FromPrice") String FromPrice,
                       @Query("ToPrice") String ToPrice,
                       @Query("NameProduct") String NameProduct,
                       @Query("Category") String Category,
                       @Query("SuperName") String SuperName,
                       @Query("IsAvailable") Boolean IsAvailable,Callback<List<SearchProduct>> callback);

    @Headers("User-Agent: SupermarketApp")
    @GET("/Supermarket")
    void GetNearestSupermarket(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key,
                               @Query("currentpage") int currentpage,
                               @Query("Lat") double Lat,
                               @Query("Lng") double Lng,Callback<List<GpsSupermarket>> callback);

    @Headers("User-Agent: SupermarketApp")
    @GET("/Supermarket")
    void GetDistrictSupermarket(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key,
                                @Query("currentpage") int currentpage,
                                @Query("district") String district,
                                Callback<List<GpsSupermarket>> callback);

    @Headers("User-Agent: SupermarketApp")
    @GET("/OpenSupermarket")
    void geetJobTime(@Header("TS") String TS, @Header("SupermarketAPIKey") String Key,
                                @Query("id") int id,
                                @Query("day") int day,
                                Callback<String> callback);
}
