package service;

import dto.CreatePurchaseItemDTO;
import exception.ApiException;
import model.PurchaseItemModel;
import repository.PurchaseItemRepository;
import repository.PurchaseRepository;
import util.TextNormalizer;

import java.math.BigDecimal;
import java.util.List;

public class PurchaseItemServiceImpl implements PurchaseItemService {

    private final PurchaseItemRepository purchaseItemRepository;
    private final PurchaseRepository purchaseRepository; // NOVO

    public PurchaseItemServiceImpl(
            PurchaseItemRepository purchaseItemRepository,
            PurchaseRepository purchaseRepository // NOVO
    ) {
        this.purchaseItemRepository = purchaseItemRepository;
        this.purchaseRepository = purchaseRepository;
    }

    @Override
    public void savePurchaseItem(CreatePurchaseItemDTO dto, Integer userId) {

        validatePurchaseItem(dto);

        // 🔒 a compra informada precisa ser do usuário logado
        if (purchaseRepository.findByIdAndUser(dto.getPurchaseId(), userId) == null) {
            throw new ApiException("Purchase not found", 404);
        }

        // padroniza o nome do produto (trim, espaços colapsados, Title Case)
        // aqui, no service, para valer tanto pra quem usa a tela quanto pra
        // quem eventualmente bater direto na API
        dto.setProductName(TextNormalizer.normalizeProductName(dto.getProductName()));

        BigDecimal subtotal = calculateSubtotal(dto);

        PurchaseItemModel purchaseItem = new PurchaseItemModel();
        purchaseItem.setPurchaseId(dto.getPurchaseId());
        purchaseItem.setProductName(dto.getProductName());
        purchaseItem.setQuantity(dto.getQuantity());
        purchaseItem.setUnitPrice(dto.getUnitPrice());
        purchaseItem.setPromotionActive(dto.getPromotionActive());
        purchaseItem.setPromotionType(dto.getPromotionType());
        purchaseItem.setPromotionDescription(dto.getPromotionDescription());
        purchaseItem.setSubtotal(subtotal);

        purchaseItemRepository.save(purchaseItem);
    }

    @Override
    public void updatePurchaseItem(Integer purchaseItemsId, CreatePurchaseItemDTO dto, Integer userId) {

        PurchaseItemModel purchaseItem =
                purchaseItemRepository.findByIdAndUser(purchaseItemsId, userId);

        if (purchaseItem == null) {
            throw new ApiException("Purchase item not found", 404);
        }

        if (purchaseRepository.findByIdAndUser(dto.getPurchaseId(), userId) == null) {
            throw new ApiException("Purchase not found", 404);
        }

        validatePurchaseItem(dto);
        dto.setProductName(TextNormalizer.normalizeProductName(dto.getProductName()));
        BigDecimal subtotal = calculateSubtotal(dto);

        purchaseItem.setPurchaseId(dto.getPurchaseId());
        purchaseItem.setProductName(dto.getProductName());
        purchaseItem.setQuantity(dto.getQuantity());
        purchaseItem.setUnitPrice(dto.getUnitPrice());
        purchaseItem.setPromotionActive(dto.getPromotionActive());
        purchaseItem.setPromotionType(dto.getPromotionType());
        purchaseItem.setPromotionDescription(dto.getPromotionDescription());
        purchaseItem.setSubtotal(subtotal);

        purchaseItemRepository.update(purchaseItem, userId);
    }

    @Override
    public void deletePurchaseItem(Integer purchaseItemsId, Integer userId) {

        if (purchaseItemRepository.findByIdAndUser(purchaseItemsId, userId) == null) {
            throw new ApiException("Purchase item not found", 404);
        }

        purchaseItemRepository.delete(purchaseItemsId, userId);
    }

    @Override
    public List<PurchaseItemModel> findAllPurchaseItems(Integer userId) {
        return purchaseItemRepository.findAllByUser(userId);
    }

    @Override
    public PurchaseItemModel findPurchaseItemById(Integer purchaseItemsId, Integer userId) {
        PurchaseItemModel item = purchaseItemRepository.findByIdAndUser(purchaseItemsId, userId);
        if (item == null) {
            throw new ApiException("Purchase item not found", 404);
        }
        return item;
    }

    @Override
    public List<String> findDistinctProductNames(Integer userId) {
        return purchaseItemRepository.findDistinctProductNamesByUser(userId);
    }

    private void validatePurchaseItem(CreatePurchaseItemDTO dto) {
        if (dto.getProductName() == null || dto.getProductName().trim().isEmpty()) {
            throw new ApiException("Product name is required", 400);
        }
        if (dto.getQuantity() <= 0) {
            throw new ApiException("Quantity must be greater than zero", 400);
        }
        if (dto.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Unit price must be greater than zero", 400);
        }
    }

    private BigDecimal calculateSubtotal(CreatePurchaseItemDTO dto) {
        return dto.getUnitPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
    }
}