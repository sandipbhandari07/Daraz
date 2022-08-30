package project.clone.daraz;

public class ModelProduct {
    private String productid,producttitle,productdesc,productcategory,productquantity,producticon,orginalprice,discountprice,discountnotes,discountavailable
    ,timestamp,uid;

    public ModelProduct(){

    }

    public ModelProduct(String productid, String producttitle, String productdesc, String productcategory, String productquantity, String producticon, String orginalprice, String discountprice, String discountnotes, String discountavailable, String timestamp, String uid) {
        this.productid = productid;
        this.producttitle = producttitle;
        this.productdesc = productdesc;
        this.productcategory = productcategory;
        this.productquantity = productquantity;
        this.producticon = producticon;
        this.orginalprice = orginalprice;
        this.discountprice = discountprice;
        this.discountnotes = discountnotes;
        this.discountavailable = discountavailable;
        this.timestamp = timestamp;
        this.uid = uid;
    }

    public String getProductid() {
        return productid;
    }

    public void setProductid(String productid) {
        this.productid = productid;
    }

    public String getProducttitle() {
        return producttitle;
    }

    public void setProducttitle(String producttitle) {
        this.producttitle = producttitle;
    }

    public String getProductdesc() {
        return productdesc;
    }

    public void setProductdesc(String productdesc) {
        this.productdesc = productdesc;
    }

    public String getProductcategory() {
        return productcategory;
    }

    public void setProductcategory(String productcategory) {
        this.productcategory = productcategory;
    }

    public String getProductquantity() {
        return productquantity;
    }

    public void setProductquantity(String productquantity) {
        this.productquantity = productquantity;
    }

    public String getProducticon() {
        return producticon;
    }

    public void setProducticon(String producticon) {
        this.producticon = producticon;
    }

    public String getOrginalprice() {
        return orginalprice;
    }

    public void setOrginalprice(String orginalprice) {
        this.orginalprice = orginalprice;
    }

    public String getDiscountprice() {
        return discountprice;
    }

    public void setDiscountprice(String discountprice) {
        this.discountprice = discountprice;
    }

    public String getDiscountnotes() {
        return discountnotes;
    }

    public void setDiscountnotes(String discountnotes) {
        this.discountnotes = discountnotes;
    }

    public String getDiscountavailable() {
        return discountavailable;
    }

    public void setDiscountavailable(String discountavailable) {
        this.discountavailable = discountavailable;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
