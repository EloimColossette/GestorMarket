package repository;

import dto.PurchaseDetailDTO;
import dto.PurchaseReportItem;
import dto.SupermarketSummaryDTO;
import model.PurchaseModel;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseRepository {

    Integer save(PurchaseModel purchaseModel);

    List<PurchaseModel> findAllByUser(Integer userId);

    PurchaseModel findByIdAndUser(Integer purchasesId, Integer userId);

    void update(PurchaseModel purchaseModel, Integer userId);

    void delete(Integer purchasesId, Integer userId);

    List<PurchaseReportItem> findReportByUser(
            Integer userId, String supermarketName, LocalDate startDate, LocalDate endDate
    );

    // NOVO — resumo agrupado por supermercado (nível 1 da tela de relatório)
    List<SupermarketSummaryDTO> findSummaryByUser(
            Integer userId, Integer supermarketId, LocalDate date
    );

    // NOVO — detalhe com itens de um supermercado específico (nível 2, ao clicar)
    List<PurchaseDetailDTO> findDetailByUser(
            Integer userId, Integer supermarketId, LocalDate date
    );
}