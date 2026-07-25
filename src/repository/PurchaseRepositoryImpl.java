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
    public List<PurchaseModel> findAll() {

        List<PurchaseModel> purchases =
                new ArrayList<>();

        String sql = """
            SELECT *
            FROM purchases
            """;

        try (

                Connection conn =
                        Database.connect();

                PreparedStatement stmt =
                        conn.prepareStatement(sql);

                ResultSet resultSet =
                        stmt.executeQuery()

        ) {

            while (resultSet.next()) {

                PurchaseModel purchase =
                        new PurchaseModel();

                purchase.setPurchasesId(
                        resultSet.getInt(
                                "purchases_id"
                        )
                );

                purchase.setSupermarketId(
                        resultSet.getInt(
                                "supermarket_id"
                        )
                );

                purchase.setPurchaseDate(
                        resultSet.getDate(
                                "purchase_date"
                        ).toLocalDate()
                );

                purchase.setTotal(
                        resultSet.getBigDecimal(
                                "total"
                        )
                );

                purchases.add(
                        purchase
                );
            }

        } catch (SQLException e) {

            logger.log(
                    Level.SEVERE,
                    "Error loading purchases",
                    e
            );
        }

        return purchases;
    }

    @Override
    public PurchaseModel findById(
            Integer purchasesId
    ) {

        String sql = """
            SELECT *
            FROM purchases
            WHERE purchases_id = ?
            """;

        try (

                Connection conn =
                        Database.connect();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)

        ) {

            stmt.setInt(
                    1,
                    purchasesId
            );

            ResultSet resultSet =
                    stmt.executeQuery();

            if (resultSet.next()) {

                PurchaseModel purchase =
                        new PurchaseModel();

                purchase.setPurchasesId(
                        resultSet.getInt(
                                "purchases_id"
                        )
                );

                purchase.setSupermarketId(
                        resultSet.getInt(
                                "supermarket_id"
                        )
                );

                purchase.setPurchaseDate(
                        resultSet.getDate(
                                "purchase_date"
                        ).toLocalDate()
                );

                purchase.setTotal(
                        resultSet.getBigDecimal(
                                "total"
                        )
                );

                return purchase;
            }

        } catch (SQLException e) {

            logger.log(
                    Level.SEVERE,
                    "Error finding purchase",
                    e
            );
        }

        return null;
    }

    @Override
    public void update(
            PurchaseModel purchaseModel
    ) {

        String sql = """
            UPDATE purchases
            SET supermarket_id = ?,
                purchase_date = ?,
                total = ?
            WHERE purchases_id = ?
            """;

        try (

                Connection conn =
                        Database.connect();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)

        ) {

            stmt.setInt(
                    1,
                    purchaseModel.getSupermarketId()
            );

            stmt.setDate(
                    2,
                    Date.valueOf(
                            purchaseModel.getPurchaseDate()
                    )
            );

            stmt.setBigDecimal(
                    3,
                    purchaseModel.getTotal()
            );

            stmt.setInt(
                    4,
                    purchaseModel.getPurchasesId()
            );

            stmt.executeUpdate();

            logger.info(
                    "Purchase updated successfully!"
            );

        } catch (SQLException e) {

            logger.log(
                    Level.SEVERE,
                    "Error updating purchase",
                    e
            );
        }
    }

    @Override
    public void delete(
            Integer purchasesId
    ) {

        String sql = """
            DELETE FROM purchases
            WHERE purchases_id = ?
            """;

        try (

                Connection conn =
                        Database.connect();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)

        ) {

            stmt.setInt(
                    1,
                    purchasesId
            );

            stmt.executeUpdate();

            logger.info(
                    "Purchase deleted successfully!"
            );

        } catch (SQLException e) {

            logger.log(
                    Level.SEVERE,
                    "Error deleting purchase",
                    e
            );
        }
    }
}