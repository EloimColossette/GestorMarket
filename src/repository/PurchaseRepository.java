package repository;

import model.PurchaseModel;
import java.util.List;

public interface PurchaseRepository {

    Integer save(PurchaseModel purchaseModel);

    List<PurchaseModel> findAllByUser(Integer userId);

    PurchaseModel findByIdAndUser(Integer purchasesId, Integer userId);

    void update(PurchaseModel purchaseModel, Integer userId);

    void delete(Integer purchasesId, Integer userId);
}