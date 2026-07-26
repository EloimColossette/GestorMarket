package repository;

import database.Database;
import model.PurchaseItemModel;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class PurchaseItemRepositoryImpl implements PurchaseItemRepository {

    private static final Logger logger =
            Logger.getLogger(
                    PurchaseItemRepositoryImpl.class.getName()
            );

    @Override
    public void save(PurchaseItemModel purchaseItemModel) {
        String sql = """
            INSERT INTO purchase_items (
                purchase_id,
                product_name,
                quantity,
                unit_price,
                promotion_active,
                promotion_type,
                promotion_description,
                subtotal
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (

                Connection conn =
                        Database.connect();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)

        ) {

            stmt.setInt(
                    1,
                    purchaseItemModel.getPurchaseId()
            );

            stmt.setString(
                    2,
                    purchaseItemModel.getProductName()
            );

            stmt.setInt(
                    3,
                    purchaseItemModel.getQuantity()
            );

            stmt.setBigDecimal(
                    4,
                    purchaseItemModel.getUnitPrice()
            );

            stmt.setBoolean(
                    5,
                    purchaseItemModel.getPromotionActive()
            );

            stmt.setString(
                    6,
                    purchaseItemModel.getPromotionType()
            );

            stmt.setString(
                    7,
                    purchaseItemModel.getPromotionDescription()
            );

            stmt.setBigDecimal(
                    8,
                    purchaseItemModel.getSubtotal()
            );

            stmt.executeUpdate();

            logger.info(
                    "Purchase item saved successfully!"
            );


        } catch (SQLException e) {

            logger.log(
                    Level.SEVERE,
                    "Error saving purchase item",
                    e
            );
        }
    }

    @Override
    public List<PurchaseItemModel> findAllByUser(Integer userId) {

        List<PurchaseItemModel> purchaseItems = new ArrayList<>();

        // Encadeia: item -> compra -> supermercado -> dono
        String sql = """
        SELECT pi.*
        FROM purchase_items pi
        JOIN purchases p     ON p.purchases_id = pi.purchase_id
        JOIN supermarkets s  ON s.supermarkets_id = p.supermarket_id
        WHERE s.user_id = ?
        """;

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    purchaseItems.add(mapRow(resultSet));
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error loading purchase items", e);
        }

        return purchaseItems;
    }

    @Override
    public PurchaseItemModel findByIdAndUser(Integer purchaseItemsId, Integer userId) {

        String sql = """
        SELECT pi.*
        FROM purchase_items pi
        JOIN purchases p     ON p.purchases_id = pi.purchase_id
        JOIN supermarkets s  ON s.supermarkets_id = p.supermarket_id
        WHERE pi.purchase_items_id = ? AND s.user_id = ?
        """;

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, purchaseItemsId);
            stmt.setInt(2, userId);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding purchase item", e);
        }

        return null;
    }

    @Override
    public void update(PurchaseItemModel purchaseItemModel, Integer userId) {

        String sql = """
        UPDATE purchase_items
        SET purchase_id = ?,
            product_name = ?,
            quantity = ?,
            unit_price = ?,
            promotion_active = ?,
            promotion_type = ?,
            promotion_description = ?,
            subtotal = ?
        WHERE purchase_items_id = ?
          AND purchase_id IN (
              SELECT p.purchases_id
              FROM purchases p
              JOIN supermarkets s ON s.supermarkets_id = p.supermarket_id
              WHERE s.user_id = ?
          )
        """;

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, purchaseItemModel.getPurchaseId());
            stmt.setString(2, purchaseItemModel.getProductName());
            stmt.setInt(3, purchaseItemModel.getQuantity());
            stmt.setBigDecimal(4, purchaseItemModel.getUnitPrice());
            stmt.setBoolean(5, purchaseItemModel.getPromotionActive());
            stmt.setString(6, purchaseItemModel.getPromotionType());
            stmt.setString(7, purchaseItemModel.getPromotionDescription());
            stmt.setBigDecimal(8, purchaseItemModel.getSubtotal());
            stmt.setInt(9, purchaseItemModel.getPurchaseItemsId());
            stmt.setInt(10, userId);

            stmt.executeUpdate();

            logger.info("Purchase item updated successfully!");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating purchase item", e);
        }
    }

    @Override
    public void delete(Integer purchaseItemsId, Integer userId) {

        String sql = """
        DELETE FROM purchase_items
        WHERE purchase_items_id = ?
          AND purchase_id IN (
              SELECT p.purchases_id
              FROM purchases p
              JOIN supermarkets s ON s.supermarkets_id = p.supermarket_id
              WHERE s.user_id = ?
          )
        """;

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, purchaseItemsId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();

            logger.info("Purchase item deleted successfully!");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting purchase item", e);
        }
    }

    private PurchaseItemModel mapRow(ResultSet rs) throws SQLException {
        PurchaseItemModel purchaseItem = new PurchaseItemModel();
        purchaseItem.setPurchaseItemsId(rs.getInt("purchase_items_id"));
        purchaseItem.setPurchaseId(rs.getInt("purchase_id"));
        purchaseItem.setProductName(rs.getString("product_name"));
        purchaseItem.setQuantity(rs.getInt("quantity"));
        purchaseItem.setUnitPrice(rs.getBigDecimal("unit_price"));
        purchaseItem.setPromotionActive(rs.getBoolean("promotion_active"));
        purchaseItem.setPromotionType(rs.getString("promotion_type"));
        purchaseItem.setPromotionDescription(rs.getString("promotion_description"));
        purchaseItem.setSubtotal(rs.getBigDecimal("subtotal"));
        return purchaseItem;
    }
}
