package service;

import dto.*;
import exception.ApiException;
import model.PurchaseItemModel;
import model.PurchaseModel;
import repository.PurchaseItemRepository;
import repository.PurchaseRepository;
import repository.SupermarketRepository;
import util.TextNormalizer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final SupermarketRepository supermarketRepository;

    public PurchaseServiceImpl(
            PurchaseRepository purchaseRepository,
            PurchaseItemRepository purchaseItemRepository,
            SupermarketRepository supermarketRepository
    ) {
        this.purchaseRepository = purchaseRepository;
        this.purchaseItemRepository = purchaseItemRepository;
        this.supermarketRepository = supermarketRepository;
    }

    @Override
    public void savePurchase(CreatePurchaseDTO createPurchaseDTO, Integer userId) {

        if (createPurchaseDTO.getSupermarketId() == null) {
            throw new ApiException("Supermarket is required", 400);
        }

        if (supermarketRepository.findSupermarketByIdAndUser(
                createPurchaseDTO.getSupermarketId(), userId) == null) {
            throw new ApiException("Supermarket not found", 404);
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

                // padroniza o nome do produto antes de gravar (trim, espaços
                // colapsados, Title Case) -- evita "arroz"/"Arroz "/"ARROZ"
                // virarem registros diferentes e melhora o autocomplete
                itemDTO.setProductName(TextNormalizer.normalizeProductName(itemDTO.getProductName()));

                BigDecimal subtotal = calculateSubtotal(itemDTO);

                PurchaseItemModel purchaseItem = new PurchaseItemModel();

                purchaseItem.setPurchaseId(purchasesId);
                purchaseItem.setProductName(itemDTO.getProductName());
                purchaseItem.setQuantity(itemDTO.getQuantity());
                purchaseItem.setUnitPrice(itemDTO.getUnitPrice());

                boolean promotionActive =
                        itemDTO.getPromotionActive() != null && itemDTO.getPromotionActive();

                purchaseItem.setPromotionActive(promotionActive);

                purchaseItem.setPromotionType(
                        promotionActive
                                ? buildPromotionLabel(itemDTO)
                                : null
                );

                purchaseItem.setPromotionDescription(itemDTO.getPromotionDescription());
                purchaseItem.setSubtotal(subtotal);

                purchaseItemRepository.save(purchaseItem);

                total = total.add(subtotal);
            }
        }

        // atualiza o total da compra com a soma dos itens
        purchaseModel.setPurchasesId(purchasesId);
        purchaseModel.setTotal(total);
        purchaseRepository.update(purchaseModel, userId); // ✅ corrigido: agora passa userId
    }

    private BigDecimal calculateSubtotal(
            CreatePurchaseItemDTO itemDTO
    ) {

        BigDecimal fullPrice =
                itemDTO.getUnitPrice()
                        .multiply(BigDecimal.valueOf(itemDTO.getQuantity()));

        boolean promotionActive =
                itemDTO.getPromotionActive() != null && itemDTO.getPromotionActive();

        if (!promotionActive || itemDTO.getPromotionType() == null) {
            return fullPrice;
        }

        String type = itemDTO.getPromotionType().trim();

        return switch (type) {

            // Ex.: Leve 2, Pague 1 -> a cada "leve" unidades, só paga "pague"
            case "leve_pague" -> {

                Integer buyQty = itemDTO.getPromotionBuyQuantity();
                Integer payQty = itemDTO.getPromotionPayQuantity();

                if (buyQty == null || buyQty <= 0 || payQty == null || payQty <= 0) {
                    throw new IllegalArgumentException(
                            "Informe corretamente os valores de 'Leve' e 'Pague'"
                    );
                }

                if (payQty > buyQty) {
                    throw new IllegalArgumentException(
                            "'Pague' não pode ser maior que 'Leve'"
                    );
                }

                int quantity = itemDTO.getQuantity();

                int fullGroups = quantity / buyQty;
                int remainder = quantity % buyQty;

                int payableUnits = (fullGroups * payQty) + remainder;

                yield itemDTO.getUnitPrice()
                        .multiply(BigDecimal.valueOf(payableUnits));
            }

            // Ex.: 10% de desconto sobre o total do item
            case "percentual" -> {

                BigDecimal percent = itemDTO.getPromotionPercent();

                if (percent == null
                        || percent.compareTo(BigDecimal.ZERO) <= 0
                        || percent.compareTo(BigDecimal.valueOf(100)) > 0) {

                    throw new IllegalArgumentException(
                            "Informe um percentual de desconto entre 1 e 100"
                    );
                }

                BigDecimal discountFactor =
                        BigDecimal.ONE.subtract(
                                percent.divide(BigDecimal.valueOf(100))
                        );

                yield fullPrice.multiply(discountFactor);
            }

            // Ex.: R$ 5,00 de desconto no total do item
            case "valor_fixo" -> {

                BigDecimal discountValue = itemDTO.getPromotionDiscountValue();

                if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException(
                            "Informe um valor de desconto válido"
                    );
                }

                BigDecimal result = fullPrice.subtract(discountValue);

                // não deixa o subtotal ficar negativo
                yield result.compareTo(BigDecimal.ZERO) < 0
                        ? BigDecimal.ZERO
                        : result;
            }

            default -> fullPrice;
        };
    }

    private String buildPromotionLabel(
            CreatePurchaseItemDTO itemDTO
    ) {

        if (itemDTO.getPromotionType() == null) {
            return null;
        }

        return switch (itemDTO.getPromotionType().trim()) {

            case "leve_pague" -> "Leve " + itemDTO.getPromotionBuyQuantity()
                    + " Pague " + itemDTO.getPromotionPayQuantity();

            case "percentual" -> itemDTO.getPromotionPercent() + "% de desconto";

            case "valor_fixo" -> "Desconto de R$ " + itemDTO.getPromotionDiscountValue();

            default -> itemDTO.getPromotionType();
        };
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
    public void updatePurchase(Integer purchasesId, CreatePurchaseDTO createPurchaseDTO, Integer userId) {

        PurchaseModel purchaseModel = purchaseRepository.findByIdAndUser(purchasesId, userId);

        if (purchaseModel == null) {
            throw new ApiException("Purchase not found", 404);
        }

        if (createPurchaseDTO.getSupermarketId() != null
                && supermarketRepository.findSupermarketByIdAndUser(
                createPurchaseDTO.getSupermarketId(), userId) == null) {
            throw new ApiException("Supermarket not found", 404);
        }

        purchaseModel.setSupermarketId(createPurchaseDTO.getSupermarketId());
        purchaseModel.setPurchaseDate(createPurchaseDTO.getPurchaseDate());

        purchaseRepository.update(purchaseModel, userId);
    }

    @Override
    public void deletePurchase(Integer purchasesId, Integer userId) {

        PurchaseModel purchaseModel = purchaseRepository.findByIdAndUser(purchasesId, userId);

        if (purchaseModel == null) {
            throw new ApiException("Purchase not found", 404);
        }

        purchaseRepository.delete(purchasesId, userId);
    }

    @Override
    public List<PurchaseModel> findAllPurchases(Integer userId) {
        return purchaseRepository.findAllByUser(userId);
    }

    @Override
    public PurchaseModel findPurchaseById(Integer purchasesId, Integer userId) {
        PurchaseModel purchase = purchaseRepository.findByIdAndUser(purchasesId, userId);
        if (purchase == null) {
            throw new ApiException("Purchase not found", 404);
        }
        return purchase;
    }

    @Override
    public List<PurchaseReportItem> getPurchaseReport(
            Integer userId, String supermarketName, LocalDate startDate, LocalDate endDate
    ) {
        return purchaseRepository.findReportByUser(userId, supermarketName, startDate, endDate);
    }

    @Override
    public List<SupermarketSummaryDTO> getPurchaseSummary(Integer userId, Integer supermarketId, LocalDate date) {
        return purchaseRepository.findSummaryByUser(userId, supermarketId, date);
    }

    @Override
    public List<PurchaseDetailDTO> getPurchaseDetail(Integer userId, Integer supermarketId, LocalDate date) {

        if (supermarketId == null) {
            throw new ApiException("Supermarket is required", 400);
        }

        // 🔒 garante que o supermercado clicado é do usuário logado
        if (supermarketRepository.findSupermarketByIdAndUser(supermarketId, userId) == null) {
            throw new ApiException("Supermarket not found", 404);
        }

        return purchaseRepository.findDetailByUser(userId, supermarketId, date);
    }
}