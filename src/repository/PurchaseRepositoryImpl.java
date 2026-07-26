package repository;

import database.Database;
import model.PurchaseModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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

}