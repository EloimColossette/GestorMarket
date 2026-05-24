package model;

import java.math.BigDecimal;

public class PurchaseItemModel {

    private Integer purchaseItemsId;

    private Integer purchaseId;

    private Integer productId;

    private Integer quantity;

    private BigDecimal unitPrice;

    private Boolean promotionActive;

    private String promotionType;

    private String promotionDescription;

    private BigDecimal subtotal;

    public PurchaseItemModel() {
    }

    public PurchaseItemModel(
            Integer purchaseItemsId,
            Integer purchaseId,
            Integer productId,
            Integer quantity,
            BigDecimal unitPrice,
            Boolean promotionActive,
            String promotionType,
            String promotionDescription,
            BigDecimal subtotal
    ) {

        this.purchaseItemsId = purchaseItemsId;
        this.purchaseId = purchaseId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.promotionActive = promotionActive;
        this.promotionType = promotionType;
        this.promotionDescription = promotionDescription;
        this.subtotal = subtotal;
    }

    public Integer getPurchaseItemsId() {
        return purchaseItemsId;
    }

    public void setPurchaseItemsId(
            Integer purchaseItemsId
    ) {
        this.purchaseItemsId = purchaseItemsId;
    }

    public Integer getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(
            Integer purchaseId
    ) {
        this.purchaseId = purchaseId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(
            Integer productId
    ) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(
            Integer quantity
    ) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(
            BigDecimal unitPrice
    ) {
        this.unitPrice = unitPrice;
    }

    public Boolean getPromotionActive() {
        return promotionActive;
    }

    public void setPromotionActive(
            Boolean promotionActive
    ) {
        this.promotionActive = promotionActive;
    }

    public String getPromotionType() {
        return promotionType;
    }

    public void setPromotionType(
            String promotionType
    ) {
        this.promotionType = promotionType;
    }

    public String getPromotionDescription() {
        return promotionDescription;
    }

    public void setPromotionDescription(
            String promotionDescription
    ) {
        this.promotionDescription = promotionDescription;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(
            BigDecimal subtotal
    ) {
        this.subtotal = subtotal;
    }
}