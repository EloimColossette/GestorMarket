package repository;

import database.Database;
import dto.PurchaseDetailDTO;
import dto.PurchaseItemDetailDTO;
import dto.PurchaseReportItem;
import dto.SupermarketSummaryDTO;
import model.PurchaseModel;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PurchaseRepositoryImpl
        implements PurchaseRepository {

    private static final Logger logger =
            Logger.getLogger(
                    PurchaseRepositoryImpl.class.getName()
            );

    @Override
    public Integer save(
            PurchaseModel purchaseModel
    ) {

        String sql = """
            INSERT INTO purchases (
                supermarket_id,
                purchase_date,
                total
            )
            VALUES (?, ?, ?)
            """;

        try (

                Connection conn =
                        Database.connect();

                PreparedStatement stmt =
                        conn.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )

        ) {

            stmt.setInt(1, purchaseModel.getSupermarketId());
            stmt.setDate(2, Date.valueOf(purchaseModel.getPurchaseDate()));
            stmt.setBigDecimal(3, purchaseModel.getTotal());

            stmt.executeUpdate();

            logger.info("Purchase saved successfully!");

            try (
                    ResultSet generatedKeys = stmt.getGeneratedKeys()
            ) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error saving purchase", e);
        }

        return null;
    }

    @Override
    public List<PurchaseModel> findAllByUser(Integer userId) {

        List<PurchaseModel> purchases = new ArrayList<>();

        String sql = """
        SELECT p.*
        FROM purchases p
        JOIN supermarkets s ON s.supermarkets_id = p.supermarket_id
        WHERE s.user_id = ?
        """;

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    purchases.add(mapRow(resultSet));
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error loading purchases", e);
        }

        return purchases;
    }

    @Override
    public PurchaseModel findByIdAndUser(Integer purchasesId, Integer userId) {

        String sql = """
        SELECT p.*
        FROM purchases p
        JOIN supermarkets s ON s.supermarkets_id = p.supermarket_id
        WHERE p.purchases_id = ? AND s.user_id = ?
        """;

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, purchasesId);
            stmt.setInt(2, userId);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding purchase", e);
        }

        return null;
    }

    @Override
    public void update(PurchaseModel purchaseModel, Integer userId) {

        // A subquery garante que só atualiza se a compra pertencer
        // (pelo supermercado atual) ao usuário informado em userId.
        String sql = """
        UPDATE purchases
        SET supermarket_id = ?,
            purchase_date = ?,
            total = ?
        WHERE purchases_id = ?
          AND supermarket_id IN (
              SELECT supermarkets_id FROM supermarkets WHERE user_id = ?
          )
        """;

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, purchaseModel.getSupermarketId());
            stmt.setDate(2, Date.valueOf(purchaseModel.getPurchaseDate()));
            stmt.setBigDecimal(3, purchaseModel.getTotal());
            stmt.setInt(4, purchaseModel.getPurchasesId());
            stmt.setInt(5, userId);

            stmt.executeUpdate();

            logger.info("Purchase updated successfully!");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating purchase", e);
        }
    }

    @Override
    public void delete(Integer purchasesId, Integer userId) {

        String sql = """
        DELETE FROM purchases
        WHERE purchases_id = ?
          AND supermarket_id IN (
              SELECT supermarkets_id FROM supermarkets WHERE user_id = ?
          )
        """;

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, purchasesId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();

            logger.info("Purchase deleted successfully!");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting purchase", e);
        }
    }

    private PurchaseModel mapRow(ResultSet rs) throws SQLException {
        PurchaseModel purchase = new PurchaseModel();
        purchase.setPurchasesId(rs.getInt("purchases_id"));
        purchase.setSupermarketId(rs.getInt("supermarket_id"));
        purchase.setPurchaseDate(rs.getDate("purchase_date").toLocalDate());
        purchase.setTotal(rs.getBigDecimal("total"));
        return purchase;
    }

    @Override
    public List<PurchaseReportItem> findReportByUser(
            Integer userId, String supermarketName, LocalDate startDate, LocalDate endDate
    ) {

        List<PurchaseReportItem> report = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
        SELECT p.purchases_id, p.purchase_date, p.total, s.name AS supermarket_name
        FROM purchases p
        JOIN supermarkets s ON s.supermarkets_id = p.supermarket_id
        WHERE s.user_id = ?
        """);

        if (supermarketName != null && !supermarketName.isBlank()) {
            sql.append(" AND s.name ILIKE ? ");
        }
        if (startDate != null) {
            sql.append(" AND p.purchase_date >= ? ");
        }
        if (endDate != null) {
            sql.append(" AND p.purchase_date <= ? ");
        }
        sql.append(" ORDER BY p.purchase_date DESC ");

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int index = 1;
            stmt.setInt(index++, userId);

            if (supermarketName != null && !supermarketName.isBlank()) {
                stmt.setString(index++, "%" + supermarketName.trim() + "%");
            }
            if (startDate != null) {
                stmt.setDate(index++, Date.valueOf(startDate));
            }
            if (endDate != null) {
                stmt.setDate(index++, Date.valueOf(endDate));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    report.add(new PurchaseReportItem(
                            rs.getInt("purchases_id"),
                            rs.getString("supermarket_name"),
                            rs.getDate("purchase_date").toLocalDate(),
                            rs.getBigDecimal("total")
                    ));
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error loading purchase report", e);
        }

        return report;
    }

    @Override
    public List<SupermarketSummaryDTO> findSummaryByUser(
            Integer userId, Integer supermarketId, LocalDate date
    ) {

        List<SupermarketSummaryDTO> summary = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
        SELECT s.supermarkets_id, s.name, SUM(p.total) AS total
        FROM purchases p
        JOIN supermarkets s ON s.supermarkets_id = p.supermarket_id
        WHERE s.user_id = ?
        """);

        if (supermarketId != null) sql.append(" AND s.supermarkets_id = ? ");
        if (date != null)          sql.append(" AND p.purchase_date = ? ");

        sql.append(" GROUP BY s.supermarkets_id, s.name ORDER BY s.name ");

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int index = 1;
            stmt.setInt(index++, userId);
            if (supermarketId != null) stmt.setInt(index++, supermarketId);
            if (date != null)          stmt.setDate(index++, Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    summary.add(new SupermarketSummaryDTO(
                            rs.getInt("supermarkets_id"),
                            rs.getString("name"),
                            rs.getBigDecimal("total")
                    ));
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error loading purchase summary", e);
        }

        return summary;
    }

    @Override
    public List<PurchaseDetailDTO> findDetailByUser(
            Integer userId, Integer supermarketId, LocalDate date
    ) {

        // Map preserva a ordem de inserção -> compras aparecem na ordem da consulta (mais recente primeiro)
        Map<Integer, PurchaseDetailDTO> purchasesById = new LinkedHashMap<>();

        StringBuilder sql = new StringBuilder("""
        SELECT p.purchases_id, p.purchase_date, p.total AS purchase_total,
               pi.product_name, pi.quantity, pi.unit_price,
               pi.promotion_active, pi.promotion_type, pi.promotion_description, pi.subtotal
        FROM purchases p
        JOIN supermarkets s ON s.supermarkets_id = p.supermarket_id
        LEFT JOIN purchase_items pi ON pi.purchase_id = p.purchases_id
        WHERE s.user_id = ? AND s.supermarkets_id = ?
        """);

        if (date != null) sql.append(" AND p.purchase_date = ? ");

        sql.append(" ORDER BY p.purchase_date DESC, p.purchases_id ");

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int index = 1;
            stmt.setInt(index++, userId);
            stmt.setInt(index++, supermarketId);
            if (date != null) stmt.setDate(index++, Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    int purchasesId = rs.getInt("purchases_id");
                    LocalDate purchaseDate = rs.getDate("purchase_date").toLocalDate();
                    java.math.BigDecimal purchaseTotal = rs.getBigDecimal("purchase_total");

                    PurchaseDetailDTO purchase = purchasesById.get(purchasesId);
                    if (purchase == null) {
                        purchase = new PurchaseDetailDTO(purchasesId, purchaseDate, purchaseTotal);
                        purchasesById.put(purchasesId, purchase);
                    }

                    // LEFT JOIN: se a compra não tiver nenhum item, product_name vem null
                    if (rs.getString("product_name") != null) {
                        PurchaseItemDetailDTO item = new PurchaseItemDetailDTO();
                        item.setProductName(rs.getString("product_name"));
                        item.setQuantity(rs.getInt("quantity"));
                        item.setUnitPrice(rs.getBigDecimal("unit_price"));
                        item.setPromotionActive(rs.getBoolean("promotion_active"));
                        item.setPromotionType(rs.getString("promotion_type"));
                        item.setPromotionDescription(rs.getString("promotion_description"));
                        item.setSubtotal(rs.getBigDecimal("subtotal"));

                        purchase.getItems().add(item);
                    }
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error loading purchase detail", e);
        }

        return new ArrayList<>(purchasesById.values());
    }

}