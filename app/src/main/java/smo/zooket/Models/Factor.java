package smo.zooket.Models;

/**
 * Created by Smo on 5/6/2017.
 */
public class Factor {
    private String ServiceProvider ;
    private String Services ;
    private String DateReserve ;
    private String PrePayment ;
    private String FactorCode ;
    private String MainCost ;
    private boolean IsOff ;
    private String NameArayeshgah;
    private String UserName ;

    public String getServiceProvider() {
        return ServiceProvider;
    }

    public void setServiceProvider(String serviceProvider) {
        ServiceProvider = serviceProvider;
    }

    public String getServices() {
        return Services;
    }

    public void setServices(String services) {
        Services = services;
    }

    public String getDateReserve() {
        return DateReserve;
    }

    public void setDateReserve(String dateReserve) {
        DateReserve = dateReserve;
    }

    public String getPrePayment() {
        return PrePayment;
    }

    public void setPrePayment(String prePayment) {
        PrePayment = prePayment;
    }

    public String getFactorCode() {
        return FactorCode;
    }

    public void setFactorCode(String factorCode) {
        FactorCode = factorCode;
    }

    public String getMainCost() {
        return MainCost;
    }

    public void setMainCost(String mainCost) {
        MainCost = mainCost;
    }

    public boolean isOff() {
        return IsOff;
    }

    public void setOff(boolean off) {
        IsOff = off;
    }

    public String getNameArayeshgah() {
        return NameArayeshgah;
    }

    public void setNameArayeshgah(String nameArayeshgah) {
        NameArayeshgah = nameArayeshgah;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

}
