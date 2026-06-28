package repository;

import model.SupermarketModel;

import java.util.List;

public interface SupermarketRepository {

    void save(
            SupermarketModel supermarketModel
    );

    List<SupermarketModel> findAll();

    SupermarketModel findById(
            Integer supermarketsId
    );

    void update(
            SupermarketModel supermarketModel
    );

    void delete(
            Integer supermarketsId
    );
}