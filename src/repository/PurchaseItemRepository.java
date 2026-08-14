package repository;

import model.PurchaseItemModel;
import java.util.List;

public interface PurchaseItemRepository {

    void save(PurchaseItemModel purchaseItemModel);

    List<PurchaseItemModel> findAllByUser(Integer userId);

    PurchaseItemModel findByIdAndUser(Integer purchaseItemsId, Integer userId);

    void update(PurchaseItemModel purchaseItemModel, Integer userId);

    void delete(Integer purchaseItemsId, Integer userId);

    List<String> findDistinctProductNamesByUser(Integer userId);
}