package service;

import dto.CreatePurchaseDTO;
import model.PurchaseModel;
import repository.PurchaseRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PurchaseServiceImpl
        implements PurchaseService {

    private final PurchaseRepository purchaseRepository;

    public PurchaseServiceImpl(
            PurchaseRepository purchaseRepository
    ) {
        this.purchaseRepository =
                purchaseRepository;
    }

    @Override
    public void savePurchase(
            CreatePurchaseDTO createPurchaseDTO
    ) {

        PurchaseModel purchaseModel =
                new PurchaseModel();

        purchaseModel.setPurchaseDate(
                createPurchaseDTO.getPurchaseDate() == null
                        ? LocalDate.now()
                        : createPurchaseDTO.getPurchaseDate()
        );

        purchaseModel.setTotal(
                BigDecimal.ZERO
        );

        purchaseRepository.save(
                purchaseModel
        );
    }

    @Override
    public void updatePurchase(
            Integer purchasesId,
            CreatePurchaseDTO createPurchaseDTO
    ) {

        PurchaseModel purchaseModel =
                purchaseRepository.findById(
                        purchasesId
                );

        if (purchaseModel == null) {

            throw new IllegalArgumentException(
                    "Purchase not found"
            );
        }

        purchaseModel.setPurchaseDate(
                createPurchaseDTO.getPurchaseDate()
        );

        purchaseRepository.update(
                purchaseModel
        );
    }

    @Override
    public void deletePurchase(
            Integer purchasesId
    ) {

        PurchaseModel purchaseModel =
                purchaseRepository.findById(
                        purchasesId
                );

        if (purchaseModel == null) {

            throw new IllegalArgumentException(
                    "Purchase not found"
            );
        }

        purchaseRepository.delete(
                purchasesId
        );
    }

    @Override
    public List<PurchaseModel> findAllPurchases() {

        return purchaseRepository.findAll();
    }

    @Override
    public PurchaseModel findPurchaseById(
            Integer purchasesId
    ) {

        return purchaseRepository.findById(
                purchasesId
        );
    }
}