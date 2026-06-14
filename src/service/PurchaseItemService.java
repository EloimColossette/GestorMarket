package service;

import dto.CreatePurchaseItemDTO;
import model.PurchaseItemModel;

import java.util.List;

public interface PurchaseItemService {

    void savePurchaseItem(
            CreatePurchaseItemDTO createPurchaseItemDTO
    );

    List<PurchaseItemModel> findAllPurchaseItems();

    PurchaseItemModel findPurchaseItemById(
            Integer purchaseItemsId
    );

    void updatePurchaseItem(
            Integer purchaseItemsId,
            CreatePurchaseItemDTO createPurchaseItemDTO
    );

    void deletePurchaseItem(
            Integer purchaseItemsId
    );
}