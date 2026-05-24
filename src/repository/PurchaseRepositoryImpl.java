package repository;
import java.util.logging.Level;
import java.util.logging.Logger;
import database.Database;
import model.PurchaseModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseRepositoryImpl implements PurchaseRepository {

    private static final Logger logger =
            Logger.getLogger(
                    PurchaseRepositoryImpl.class.getName()
            );

    @Override
    public void save(PurchaseModel purchaseModel) {

        String sql = """
                INSERT INTO purchases (
                    purchase_date,
                    total
                )
                VALUES (?, ?)
                """;

        try (

                Connection conn =
                        Database.conectar();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)

        ) {

            stmt.setDate(
                    1,
                    Date.valueOf(
                            purchaseModel.getPurchaseDate()
                    )
            );

            stmt.setBigDecimal(
                    2,
                    purchaseModel.getTotal()
            );

            stmt.executeUpdate();

            logger.info(
                    "Purchase saved successfully!"
            );

        } catch (SQLException e) {

            logger.log(
                    Level.SEVERE,
                    "Error saving purchase",
                    e
            );
        }
    }

    @Override
    public List<PurchaseModel> findAll() {

        List<PurchaseModel> purchaseModels =
                new ArrayList<>();

        String sql = """
                SELECT * FROM purchases
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

                PurchaseModel purchaseModel =
                        new PurchaseModel();

                purchaseModel.setPurchasesId(
                        resultSet.getInt(
                                "purchases_id"
                        )
                );

                purchaseModel.setPurchaseDate(
                        resultSet.getDate(
                                "purchase_date"
                        ).toLocalDate()
                );

                purchaseModel.setTotal(
                        resultSet.getBigDecimal(
                                "total"
                        )
                );

                purchaseModels.add(
                        purchaseModel
                );
            }

        } catch (SQLException e) {

            logger.log(
                    Level.SEVERE,
                    "Error loading purchases",
                    e
            );
        }

        return purchaseModels;
    }
}