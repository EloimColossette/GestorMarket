package service;

import dto.CreatePurchaseDTO;
import model.PurchaseModel;
import java.util.List;

public interface PurchaseService {

    void savePurchase(CreatePurchaseDTO dto, Integer userId);

    List<PurchaseModel> findAllPurchases(Integer userId);

    PurchaseModel findPurchaseById(Integer purchasesId, Integer userId);

    void updatePurchase(Integer purchasesId, CreatePurchaseDTO dto, Integer userId);

    void deletePurchase(Integer purchasesId, Integer userId);
}