package repository;

import model.PurchaseModel;

import java.util.List;

public interface PurchaseRepository {

    void save(PurchaseModel purchaseModel);

    List<PurchaseModel> findAll();
}