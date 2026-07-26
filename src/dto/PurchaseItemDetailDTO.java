package dto;

import java.math.BigDecimal;

public class PurchaseItemDetailDTO {

    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private Boolean promotionActive;
    private String promotionType;
    private String promotionDescription;
    private BigDecimal subtotal;

    public PurchaseItemDetailDTO() {}

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public Boolean getPromotionActive() { return promotionActive; }
    public void setPromotionActive(Boolean promotionActive) { this.promotionActive = promotionActive; }

    public String getPromotionType() { return promotionType; }
    public void setPromotionType(String promotionType) { this.promotionType = promotionType; }

    public String getPromotionDescription() { return promotionDescription; }
    public void setPromotionDescription(String promotionDescription) { this.promotionDescription = promotionDescription; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}