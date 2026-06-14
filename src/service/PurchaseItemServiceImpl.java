package service;

import dto.CreatePurchaseItemDTO;
import model.PurchaseItemModel;
import repository.PurchaseItemRepository;

import java.math.BigDecimal;
import java.util.List;

public class PurchaseItemServiceImpl
        implements PurchaseItemService {

    private final PurchaseItemRepository purchaseItemRepository;

    public PurchaseItemServiceImpl(
            PurchaseItemRepository purchaseItemRepository
    ) {
        this.purchaseItemRepository =
                purchaseItemRepository;
    }

    @Override
    public void savePurchaseItem(
            CreatePurchaseItemDTO createPurchaseItemDTO
    ) {

        validatePurchaseItem(
                createPurchaseItemDTO
        );

        BigDecimal subtotal =
                calculateSubtotal(
                        createPurchaseItemDTO
                );

        PurchaseItemModel purchaseItem =
                new PurchaseItemModel();

        purchaseItem.setPurchaseId(
                createPurchaseItemDTO.getPurchaseId()
        );

        purchaseItem.setProductName(
                createPurchaseItemDTO.getProductName()
        );

        purchaseItem.setQuantity(
                createPurchaseItemDTO.getQuantity()
        );

        purchaseItem.setUnitPrice(
                createPurchaseItemDTO.getUnitPrice()
        );

        purchaseItem.setPromotionActive(
                createPurchaseItemDTO.getPromotionActive()
        );

        purchaseItem.setPromotionType(
                createPurchaseItemDTO.getPromotionType()
        );

        purchaseItem.setPromotionDescription(
                createPurchaseItemDTO.getPromotionDescription()
        );

        purchaseItem.setSubtotal(
                subtotal
        );

        purchaseItemRepository.save(
                purchaseItem
        );
    }

    @Override
    public void updatePurchaseItem(
            Integer purchaseItemsId,
            CreatePurchaseItemDTO createPurchaseItemDTO
    ) {

        PurchaseItemModel purchaseItem =
                purchaseItemRepository.findById(
                        purchaseItemsId
                );

        if (purchaseItem == null) {

            throw new IllegalArgumentException(
                    "Purchase item not found"
            );
        }

        validatePurchaseItem(
                createPurchaseItemDTO
        );

        BigDecimal subtotal =
                calculateSubtotal(
                        createPurchaseItemDTO
                );

        purchaseItem.setPurchaseId(
                createPurchaseItemDTO.getPurchaseId()
        );

        purchaseItem.setProductName(
                createPurchaseItemDTO.getProductName()
        );

        purchaseItem.setQuantity(
                createPurchaseItemDTO.getQuantity()
        );

        purchaseItem.setUnitPrice(
                createPurchaseItemDTO.getUnitPrice()
        );

        purchaseItem.setPromotionActive(
                createPurchaseItemDTO.getPromotionActive()
        );

        purchaseItem.setPromotionType(
                createPurchaseItemDTO.getPromotionType()
        );

        purchaseItem.setPromotionDescription(
                createPurchaseItemDTO.getPromotionDescription()
        );

        purchaseItem.setSubtotal(
                subtotal
        );

        purchaseItemRepository.update(
                purchaseItem
        );
    }

    @Override
    public void deletePurchaseItem(
            Integer purchaseItemsId
    ) {

        PurchaseItemModel purchaseItem =
                purchaseItemRepository.findById(
                        purchaseItemsId
                );

        if (purchaseItem == null) {

            throw new IllegalArgumentException(
                    "Purchase item not found"
            );
        }

        purchaseItemRepository.delete(
                purchaseItemsId
        );
    }

    private void validatePurchaseItem(
            CreatePurchaseItemDTO createPurchaseItemDTO
    ) {

        if (
                createPurchaseItemDTO.getProductName() == null ||
                        createPurchaseItemDTO.getProductName().trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Product name is required"
            );
        }

        if (
                createPurchaseItemDTO.getQuantity() <= 0
        ) {

            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        if (
                createPurchaseItemDTO.getUnitPrice()
                        .compareTo(BigDecimal.ZERO) <= 0
        ) {

            throw new IllegalArgumentException(
                    "Unit price must be greater than zero"
            );
        }
    }

    private BigDecimal calculateSubtotal(
            CreatePurchaseItemDTO createPurchaseItemDTO
    ) {

        return createPurchaseItemDTO
                .getUnitPrice()
                .multiply(
                        BigDecimal.valueOf(
                                createPurchaseItemDTO.getQuantity()
                        )
                );
    }

    @Override
    public List<PurchaseItemModel>
    findAllPurchaseItems() {

        return purchaseItemRepository.findAll();
    }

    @Override
    public PurchaseItemModel findPurchaseItemById(
            Integer purchaseItemsId
    ) {

        return purchaseItemRepository.findById(
                purchaseItemsId
        );
    }

}