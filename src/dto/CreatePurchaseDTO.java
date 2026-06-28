package dto;

import java.time.LocalDate;
import java.util.List;

public class CreatePurchaseDTO {

    private LocalDate purchaseDate;
    private Integer supermarketId;
    private List<CreatePurchaseItemDTO> items;

    public CreatePurchaseDTO() {
    }

    public CreatePurchaseDTO(LocalDate purchaseDate, Integer supermarketId, List<CreatePurchaseItemDTO> items) {
        this.purchaseDate = purchaseDate;
        this.supermarketId = supermarketId;
        this.items = items;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Integer getSupermarketId() {
        return supermarketId;
    }

    public void setSupermarketId(Integer supermarketId) {
        this.supermarketId = supermarketId;
    }

    public List<CreatePurchaseItemDTO> getItems() {
        return items;
    }

    public void setItems(List<CreatePurchaseItemDTO> items) {
        this.items = items;
    }
}