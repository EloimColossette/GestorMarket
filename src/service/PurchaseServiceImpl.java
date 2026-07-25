package service;

import dto.CreatePurchaseDTO;
import dto.CreatePurchaseItemDTO;
import model.PurchaseItemModel;
import model.PurchaseModel;
import repository.PurchaseItemRepository;
import repository.PurchaseRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PurchaseServiceImpl
        implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;

    public PurchaseServiceImpl(
            PurchaseRepository purchaseRepository,
            PurchaseItemRepository purchaseItemRepository
    ) {
        this.purchaseRepository = purchaseRepository;
        this.purchaseItemRepository = purchaseItemRepository;
    }

    @Override
    public void savePurchase(
            CreatePurchaseDTO createPurchaseDTO
    ) {

        if (createPurchaseDTO.getSupermarketId() == null) {
            throw new IllegalArgumentException("Supermarket is required");
        }

        PurchaseModel purchaseModel = new PurchaseModel();

        purchaseModel.setSupermarketId(createPurchaseDTO.getSupermarketId());

        purchaseModel.setPurchaseDate(
                createPurchaseDTO.getPurchaseDate() == null
                        ? LocalDate.now()
                        : createPurchaseDTO.getPurchaseDate()
        );

        purchaseModel.setTotal(BigDecimal.ZERO);

        // salva a compra e recupera o ID gerado (purchases_id)
        Integer purchasesId = purchaseRepository.save(purchaseModel);

        // salva cada item vinculado a essa compra e soma o total
        BigDecimal total = BigDecimal.ZERO;

        List<CreatePurchaseItemDTO> items = createPurchaseDTO.getItems();

        if (items != null) {

            for (CreatePurchaseItemDTO itemDTO : items) {

                validatePurchaseItem(itemDTO);

                BigDecimal subtotal =
                        itemDTO.getUnitPrice()
                                .multiply(BigDecimal.valueOf(itemDTO.getQuantity()));

                PurchaseItemModel purchaseItem = new PurchaseItemModel();

                purchaseItem.setPurchaseId(purchasesId);
                purchaseItem.setProductName(itemDTO.getProductName());
                purchaseItem.setQuantity(itemDTO.getQuantity());
                purchaseItem.setUnitPrice(itemDTO.getUnitPrice());

                purchaseItem.setPromotionActive(
                        itemDTO.getPromotionActive() != null && itemDTO.getPromotionActive()
                );

                purchaseItem.setPromotionType(itemDTO.getPromotionType());
                purchaseItem.setPromotionDescription(itemDTO.getPromotionDescription());
                purchaseItem.setSubtotal(subtotal);

                purchaseItemRepository.save(purchaseItem);

                total = total.add(subtotal);
            }
        }

        // atualiza o total da compra com a soma dos itens
        purchaseModel.setPurchasesId(purchasesId);
        purchaseModel.setTotal(total);
        purchaseRepository.update(purchaseModel);
    }

    private void validatePurchaseItem(CreatePurchaseItemDTO itemDTO) {

        if (itemDTO.getProductName() == null || itemDTO.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }

        if (itemDTO.getQuantity() == null || itemDTO.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        if (itemDTO.getUnitPrice() == null || itemDTO.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than zero");
        }
    }

    @Override
    public void updatePurchase(Integer purchasesId, CreatePurchaseDTO createPurchaseDTO) {

        PurchaseModel purchaseModel = purchaseRepository.findById(purchasesId);

        if (purchaseModel == null) {
            throw new IllegalArgumentException("Purchase not found");
        }

        purchaseModel.setSupermarketId(createPurchaseDTO.getSupermarketId());
        purchaseModel.setPurchaseDate(createPurchaseDTO.getPurchaseDate());

        purchaseRepository.update(purchaseModel);
    }

    @Override
    public void deletePurchase(Integer purchasesId) {

        PurchaseModel purchaseModel = purchaseRepository.findById(purchasesId);

        if (purchaseModel == null) {
            throw new IllegalArgumentException("Purchase not found");
        }

        purchaseRepository.delete(purchasesId);
    }

    @Override
    public List<PurchaseModel> findAllPurchases() {
        return purchaseRepository.findAll();
    }

    @Override
    public PurchaseModel findPurchaseById(Integer purchasesId) {
        return purchaseRepository.findById(purchasesId);
    }
}