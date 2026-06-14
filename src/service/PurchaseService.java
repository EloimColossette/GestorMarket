package service;

import dto.CreatePurchaseDTO;
import model.PurchaseModel;

import java.util.List;

public interface PurchaseService {

    void savePurchase(
            CreatePurchaseDTO createPurchaseDTO
    );

    List<PurchaseModel> findAllPurchases();

    PurchaseModel findPurchaseById(
            Integer purchasesId
    );

    void updatePurchase(
            Integer purchasesId,
            CreatePurchaseDTO createPurchaseDTO
    );

    void deletePurchase(
            Integer purchasesId
    );
}