package service;

import dto.CreatePurchaseDTO;
import dto.PurchaseDetailDTO;
import dto.PurchaseReportItem;
import dto.SupermarketSummaryDTO;
import model.PurchaseModel;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseService {

    void savePurchase(CreatePurchaseDTO dto, Integer userId);

    List<PurchaseModel> findAllPurchases(Integer userId);

    PurchaseModel findPurchaseById(Integer purchasesId, Integer userId);

    void updatePurchase(Integer purchasesId, CreatePurchaseDTO dto, Integer userId);

    void deletePurchase(Integer purchasesId, Integer userId);

    List<PurchaseReportItem> getPurchaseReport(
            Integer userId, String supermarketName, LocalDate startDate, LocalDate endDate
    );

    List<SupermarketSummaryDTO> getPurchaseSummary(Integer userId, Integer supermarketId, LocalDate date);

    List<PurchaseDetailDTO> getPurchaseDetail(Integer userId, Integer supermarketId, LocalDate date);
}