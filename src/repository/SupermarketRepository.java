package repository;

import model.SupermarketModel;

import java.util.List;

public interface SupermarketRepository {

    void saveSupermarket(
            SupermarketModel supermarketModel
    );

    List<SupermarketModel> findAllSupermarkets();

    SupermarketModel findSupermarketById(
            Integer supermarketsId
    );

    void updateSupermarket(
            SupermarketModel supermarketModel
    );

    void deleteSupermarket(
            Integer supermarketsId
    );
}