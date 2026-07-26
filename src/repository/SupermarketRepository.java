package repository;

import model.SupermarketModel;
import java.util.List;

public interface SupermarketRepository {

    void saveSupermarket(SupermarketModel supermarketModel);


    List<SupermarketModel> findAllSupermarketsByUser(Integer userId);


    SupermarketModel findSupermarketByIdAndUser(Integer supermarketsId, Integer userId);

    void updateSupermarket(SupermarketModel supermarketModel);

    void deleteSupermarket(Integer supermarketsId);
}