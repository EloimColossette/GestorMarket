package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PurchaseModel {

    private Integer purchasesId;

    private Integer supermarketId;

    private LocalDate purchaseDate;

    private BigDecimal total;

    // Empty constructor
    public PurchaseModel() {
    }

    // Full constructor
    public PurchaseModel(
            Integer purchasesId,
            Integer supermarketId,
            LocalDate purchaseDate,
            BigDecimal total
    ) {

        this.purchasesId = purchasesId;
        this.supermarketId = supermarketId;
        this.purchaseDate = purchaseDate;
        this.total = total;
    }

    public Integer getPurchasesId() {
        return purchasesId;
    }

    public void setPurchasesId(
            Integer purchasesId
    ) {
        this.purchasesId = purchasesId;
    }

    public Integer getSupermarketId() {
        return supermarketId;
    }

    public void setSupermarketId(
            Integer supermarketId
    ) {
        this.supermarketId = supermarketId;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(
            LocalDate purchaseDate
    ) {
        this.purchaseDate = purchaseDate;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(
            BigDecimal total
    ) {
        this.total = total;
    }

    @Override
    public String toString() {

        return "PurchaseModel{" +
                "purchasesId=" + purchasesId +
                ", supermarketId=" + supermarketId +
                ", purchaseDate=" + purchaseDate +
                ", total=" + total +
                '}';
    }
}