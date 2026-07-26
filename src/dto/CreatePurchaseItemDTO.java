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

    // ── usados apenas para CALCULAR o subtotal com desconto ──
    // (não existem colunas próprias no banco; o resultado final
    //  vai para a coluna "subtotal" e um resumo para "promotion_type")

    // tipo "leve_pague": ex. Leve 2 Pague 1
    private Integer promotionBuyQuantity;
    private Integer promotionPayQuantity;

    // tipo "percentual": ex. 10 (%)
    private BigDecimal promotionPercent;

    // tipo "valor_fixo": desconto em R$ sobre o total do item
    private BigDecimal promotionDiscountValue;

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

    public Integer getPromotionBuyQuantity() {
        return promotionBuyQuantity;
    }

    public void setPromotionBuyQuantity(Integer promotionBuyQuantity) {
        this.promotionBuyQuantity = promotionBuyQuantity;
    }

    public Integer getPromotionPayQuantity() {
        return promotionPayQuantity;
    }

    public void setPromotionPayQuantity(Integer promotionPayQuantity) {
        this.promotionPayQuantity = promotionPayQuantity;
    }

    public BigDecimal getPromotionPercent() {
        return promotionPercent;
    }

    public void setPromotionPercent(BigDecimal promotionPercent) {
        this.promotionPercent = promotionPercent;
    }

    public BigDecimal getPromotionDiscountValue() {
        return promotionDiscountValue;
    }

    public void setPromotionDiscountValue(BigDecimal promotionDiscountValue) {
        this.promotionDiscountValue = promotionDiscountValue;
    }
}