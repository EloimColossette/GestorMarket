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
                        Database.conectar();

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
    public void update(
            PurchaseItemModel purchaseItemModel
    ) {

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
        """;

        try (

                Connection conn =
                        Database.conectar();

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

            stmt.setInt(
                    9,
                    purchaseItemModel.getPurchaseItemsId()
            );

            stmt.executeUpdate();

            logger.info(
                    "Purchase item updated successfully!"
            );

        } catch (SQLException e) {

            logger.log(
                    Level.SEVERE,
                    "Error updating purchase item",
                    e
            );
        }
    }

    @Override
    public void delete(
            Integer purchaseItemsId
    ) {

        String sql = """
            DELETE FROM purchase_items
            WHERE purchase_items_id = ?
            """;

        try (

                Connection conn =
                        Database.conectar();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)

        ) {

            stmt.setInt(
                    1,
                    purchaseItemsId
            );

            stmt.executeUpdate();

            logger.info(
                    "Purchase item deleted successfully!"
            );

        } catch (SQLException e) {

            logger.log(
                    Level.SEVERE,
                    "Error deleting purchase item",
                    e
            );
        }
    }

    @Override
    public List<PurchaseItemModel> findAll() {

        List<PurchaseItemModel> purchaseItems =
                new ArrayList<>();

        String sql = """
                SELECT * FROM purchase_items
                """;

        try (

                Connection conn =
                        Database.conectar();

                PreparedStatement stmt =
                        conn.prepareStatement(sql);

                ResultSet resultSet =
                        stmt.executeQuery()

        ) {

            while (resultSet.next()) {

                PurchaseItemModel purchaseItem =
                        new PurchaseItemModel();

                purchaseItem.setPurchaseItemsId(
                        resultSet.getInt(
                                "purchase_items_id"
                        )
                );

                purchaseItem.setPurchaseId(
                        resultSet.getInt(
                                "purchase_id"
                        )
                );

                purchaseItem.setProductName(
                        resultSet.getString(
                                "product_name"
                        )
                );

                purchaseItem.setQuantity(
                        resultSet.getInt(
                                "quantity"
                        )
                );

                purchaseItem.setUnitPrice(
                        resultSet.getBigDecimal(
                                "unit_price"
                        )
                );

                purchaseItem.setPromotionActive(
                        resultSet.getBoolean(
                                "promotion_active"
                        )
                );

                purchaseItem.setPromotionType(
                        resultSet.getString(
                                "promotion_type"
                        )
                );

                purchaseItem.setPromotionDescription(
                        resultSet.getString(
                                "promotion_description"
                        )
                );

                purchaseItem.setSubtotal(
                        resultSet.getBigDecimal(
                                "subtotal"
                        )
                );

                purchaseItems.add(
                        purchaseItem
                );
            }

        } catch (SQLException e) {

            logger.log(
                    Level.SEVERE,
                    "Error loading purchase items",
                    e
            );
        }

        return purchaseItems;
    }

    @Override
    public PurchaseItemModel findById(
            Integer purchaseItemsId
    ) {

        String sql = """
            SELECT *
            FROM purchase_items
            WHERE purchase_items_id = ?
            """;

        try (

                Connection conn =
                        Database.conectar();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)

        ) {

            stmt.setInt(
                    1,
                    purchaseItemsId
            );

            ResultSet resultSet =
                    stmt.executeQuery();

            if (resultSet.next()) {

                PurchaseItemModel purchaseItem =
                        new PurchaseItemModel();

                purchaseItem.setPurchaseItemsId(
                        resultSet.getInt(
                                "purchase_items_id"
                        )
                );

                purchaseItem.setPurchaseId(
                        resultSet.getInt(
                                "purchase_id"
                        )
                );

                purchaseItem.setProductName(
                        resultSet.getString(
                                "product_name"
                        )
                );

                purchaseItem.setQuantity(
                        resultSet.getInt(
                                "quantity"
                        )
                );

                purchaseItem.setUnitPrice(
                        resultSet.getBigDecimal(
                                "unit_price"
                        )
                );

                purchaseItem.setPromotionActive(
                        resultSet.getBoolean(
                                "promotion_active"
                        )
                );

                purchaseItem.setPromotionType(
                        resultSet.getString(
                                "promotion_type"
                        )
                );

                purchaseItem.setPromotionDescription(
                        resultSet.getString(
                                "promotion_description"
                        )
                );

                purchaseItem.setSubtotal(
                        resultSet.getBigDecimal(
                                "subtotal"
                        )
                );

                return purchaseItem;
            }

        } catch (SQLException e) {

            logger.log(
                    Level.SEVERE,
                    "Error finding purchase item",
                    e
            );
        }

        return null;
    }
}
