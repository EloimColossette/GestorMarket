package service;

import dto.CreateSupermarketDTO;
import model.SupermarketModel;
import repository.SupermarketRepository;

import java.util.List;

public class SupermarketServiceImpl
        implements SupermarketService {

    private final SupermarketRepository
            supermarketRepository;

    public SupermarketServiceImpl(
            SupermarketRepository supermarketRepository
    ) {

        this.supermarketRepository =
                supermarketRepository;
    }

    @Override
    public void saveSupermarket(
            CreateSupermarketDTO createSupermarketDTO
    ) {

        validateSupermarket(
                createSupermarketDTO
        );

        SupermarketModel supermarket =
                new SupermarketModel();

        supermarket.setName(
                createSupermarketDTO.getName()
        );

        supermarketRepository.save(
                supermarket
        );
    }

    @Override
    public List<SupermarketModel>
    findAllSupermarkets() {

        return supermarketRepository.findAll();
    }

    @Override
    public SupermarketModel findSupermarketById(
            Integer supermarketsId
    ) {

        return supermarketRepository.findById(
                supermarketsId
        );
    }

    @Override
    public void updateSupermarket(
            Integer supermarketsId,
            CreateSupermarketDTO createSupermarketDTO
    ) {

        SupermarketModel supermarket =
                supermarketRepository.findById(
                        supermarketsId
                );

        if (supermarket == null) {

            throw new IllegalArgumentException(
                    "Supermarket not found"
            );
        }

        validateSupermarket(
                createSupermarketDTO
        );

        supermarket.setName(
                createSupermarketDTO.getName()
        );

        supermarketRepository.update(
                supermarket
        );
    }

    @Override
    public void deleteSupermarket(
            Integer supermarketsId
    ) {

        SupermarketModel supermarket =
                supermarketRepository.findById(
                        supermarketsId
                );

        if (supermarket == null) {

            throw new IllegalArgumentException(
                    "Supermarket not found"
            );
        }

        supermarketRepository.delete(
                supermarketsId
        );
    }

    private void validateSupermarket(
            CreateSupermarketDTO createSupermarketDTO
    ) {

        if (
                createSupermarketDTO.getName()
                        == null
                        ||
                        createSupermarketDTO.getName()
                                .trim()
                                .isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Supermarket name is required"
            );
        }
    }
}