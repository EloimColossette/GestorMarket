package service;

import dto.CreateSupermarketDTO;
import exception.ApiException;
import model.SupermarketModel;
import repository.SupermarketRepository;
import repository.SupermarketRepositoryImpl;

import java.util.List;

public class SupermarketServiceImpl implements SupermarketService {

    private final SupermarketRepository supermarketRepository;

    public SupermarketServiceImpl(SupermarketRepository supermarketRepository) {
        this.supermarketRepository = supermarketRepository;
    }

    @Override
    public void saveSupermarket(CreateSupermarketDTO dto, Integer userId) {

        validateSupermarket(dto);

        SupermarketModel supermarket = new SupermarketModel();
        supermarket.setName(dto.getName());
        supermarket.setUserId(userId);

        supermarketRepository.saveSupermarket(supermarket);
    }

    @Override
    public List<SupermarketModel> findAllSupermarkets(Integer userId) {
        return supermarketRepository.findAllSupermarketsByUser(userId);
    }

    @Override
    public SupermarketModel findSupermarketById(Integer supermarketsId, Integer userId) {
        SupermarketModel supermarket =
                supermarketRepository.findSupermarketByIdAndUser(supermarketsId, userId);

        if (supermarket == null) {
            // 404 em vez de 403: não revela se o supermercado existe e é de outra pessoa
            throw new ApiException("Supermarket not found", 404);
        }
        return supermarket;
    }

    @Override
    public void updateSupermarket(Integer supermarketsId, CreateSupermarketDTO dto, Integer userId) {

        SupermarketModel supermarket = findSupermarketById(supermarketsId, userId); // já valida dono

        validateSupermarket(dto);
        supermarket.setName(dto.getName());

        supermarketRepository.updateSupermarket(supermarket);
    }

    @Override
    public void deleteSupermarket(Integer supermarketsId, Integer userId) {
        findSupermarketById(supermarketsId, userId); // garante que é dono antes de apagar
        ((SupermarketRepositoryImpl) supermarketRepository).deleteSupermarket(supermarketsId, userId);
    }

    private void validateSupermarket(CreateSupermarketDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new ApiException("Supermarket name is required", 400);
        }
    }
}