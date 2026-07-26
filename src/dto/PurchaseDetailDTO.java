package dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PurchaseDetailDTO {

    private Integer purchasesId;
    private LocalDate purchaseDate;
    private BigDecimal total;
    private List<PurchaseItemDetailDTO> items = new ArrayList<>();

    public PurchaseDetailDTO() {}

    public PurchaseDetailDTO(Integer purchasesId, LocalDate purchaseDate, BigDecimal total) {
        this.purchasesId = purchasesId;
        this.purchaseDate = purchaseDate;
        this.total = total;
    }

    public Integer getPurchasesId() { return purchasesId; }
    public void setPurchasesId(Integer purchasesId) { this.purchasesId = purchasesId; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public List<PurchaseItemDetailDTO> getItems() { return items; }
    public void setItems(List<PurchaseItemDetailDTO> items) { this.items = items; }
}