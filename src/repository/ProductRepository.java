package repository;

import model.ProductModel;
import model.PurchaseModel;
import java.util.List;

public interface ProductRepository {
    void save (ProductModel productModel);
    List<ProductModel>findAll();
}
