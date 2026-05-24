package model;

public class ProductModel {
    private Integer productsId;
    private String name;

    public ProductModel() {
    }

    public ProductModel(Integer id, String name) {
        this.productsId = productsId;
        this.name = name;
    }

    public Integer getProductsId() {
        return productsId;
    }

    public void setProductsId(Integer productsId) {
        this.productsId = productsId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
