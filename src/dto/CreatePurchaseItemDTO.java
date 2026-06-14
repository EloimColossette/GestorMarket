package dto;

import java.math.BigDecimal;

public class CreatePurchaseItemDTO {

    private Integer purchaseId;

    private String productName;

    private Integer quantity;

    private BigDecimal unitPrice;

    private Boolean promotionActive;

    private String promotionType;

    private String promotionDescription;

    public CreatePurchaseItemDTO() {
    }

    public CreatePurchaseItemDTO(
            Integer purchaseId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            Boolean promotionActive,
            String promotionType,
            String promotionDescription
    ) {

        this.purchaseId = purchaseId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.promotionActive = promotionActive;
        this.promotionType = promotionType;
        this.promotionDescription = promotionDescription;
    }

    public Integer getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(Integer purchaseId) {
        this.purchaseId = purchaseId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Boolean getPromotionActive() {
        return promotionActive;
    }

    public void setPromotionActive(Boolean promotionActive) {
        this.promotionActive = promotionActive;
    }

    public String getPromotionType() {
        return promotionType;
    }

    public void setPromotionType(String promotionType) {
        this.promotionType = promotionType;
    }

    public String getPromotionDescription() {
        return promotionDescription;
    }

    public void setPromotionDescription(String promotionDescription) {
        this.promotionDescription = promotionDescription;
    }
}