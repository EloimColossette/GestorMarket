package service;

import dto.CreatePurchaseItemDTO;
import model.PurchaseItemModel;
import java.util.List;

public interface PurchaseItemService {

    void savePurchaseItem(CreatePurchaseItemDTO dto, Integer userId);

    List<PurchaseItemModel> findAllPurchaseItems(Integer userId);

    PurchaseItemModel findPurchaseItemById(Integer purchaseItemsId, Integer userId);

    void updatePurchaseItem(Integer purchaseItemsId, CreatePurchaseItemDTO dto, Integer userId);

    void deletePurchaseItem(Integer purchaseItemsId, Integer userId);
}