package repository;

import model.PurchaseItemModel;

import java.util.List;

public interface PurchaseItemRepository {

    void save(
            PurchaseItemModel purchaseItemModel
    );

    List<PurchaseItemModel> findAll();

    PurchaseItemModel findById(
            Integer purchaseItemsId
    );

    void update(
            PurchaseItemModel purchaseItemModel
    );

    void delete(
            Integer purchaseItemsId
    );
}