package dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PurchaseReportItem {

    private Integer purchasesId;
    private String supermarketName;
    private LocalDate purchaseDate;
    private BigDecimal total;

    public PurchaseReportItem() {}

    public PurchaseReportItem(Integer purchasesId, String supermarketName, LocalDate purchaseDate, BigDecimal total) {
        this.purchasesId = purchasesId;
        this.supermarketName = supermarketName;
        this.purchaseDate = purchaseDate;
        this.total = total;
    }

    public Integer getPurchasesId() { return purchasesId; }
    public void setPurchasesId(Integer purchasesId) { this.purchasesId = purchasesId; }

    public String getSupermarketName() { return supermarketName; }
    public void setSupermarketName(String supermarketName) { this.supermarketName = supermarketName; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}