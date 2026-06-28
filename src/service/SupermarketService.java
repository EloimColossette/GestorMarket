package service;

import dto.CreateSupermarketDTO;
import model.SupermarketModel;

import java.util.List;

public interface SupermarketService {

    void saveSupermarket(
            CreateSupermarketDTO createSupermarketDTO
    );

    List<SupermarketModel> findAllSupermarkets();

    SupermarketModel findSupermarketById(
            Integer supermarketsId
    );

    void updateSupermarket(
            Integer supermarketsId,
            CreateSupermarketDTO createSupermarketDTO
    );

    void deleteSupermarket(
            Integer supermarketsId
    );
}