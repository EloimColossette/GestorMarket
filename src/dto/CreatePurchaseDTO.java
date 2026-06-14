package dto;

import java.time.LocalDate;

public class CreatePurchaseDTO {
    private LocalDate purchaseDate;

    public CreatePurchaseDTO(){

    }

    public CreatePurchaseDTO(LocalDate purchaseDate){
        this.purchaseDate = purchaseDate;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
}
