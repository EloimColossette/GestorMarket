package repository;

import model.PurchaseModel;

import java.util.List;

public interface PurchaseRepository {

    Integer save(
            PurchaseModel purchaseModel
    );

    List<PurchaseModel> findAll();

    PurchaseModel findById(
            Integer purchasesId
    );

    void update(
            PurchaseModel purchaseModel
    );

    void delete(
            Integer purchasesId
    );
}