package service;

import dto.CreateSupermarketDTO;
import model.SupermarketModel;
import java.util.List;

public interface SupermarketService {

    void saveSupermarket(CreateSupermarketDTO dto, Integer userId);

    List<SupermarketModel> findAllSupermarkets(Integer userId);

    SupermarketModel findSupermarketById(Integer supermarketsId, Integer userId);

    void updateSupermarket(Integer supermarketsId, CreateSupermarketDTO dto, Integer userId);

    void deleteSupermarket(Integer supermarketsId, Integer userId);
}