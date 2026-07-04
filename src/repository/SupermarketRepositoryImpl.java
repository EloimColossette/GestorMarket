package repository;

import database.Database;
import model.SupermarketModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SupermarketRepositoryImpl
        implements SupermarketRepository {

    private static final Logger logger =
            Logger.getLogger(
                    SupermarketRepositoryImpl.class.getName()
            );

    // =========================================
    // SAVE
    // =========================================
    @Override
    public void saveSupermarket(
            SupermarketModel supermarketModel
    ) {

        String sql = """
            INSERT INTO supermarkets (
                name
            )
            VALUES (?)
            """;

        try (

                Connection conn =
                        Database.connect();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)

        ) {

            stmt.setString(
                    1,
                    supermarketModel.getName()
            );

            stmt.executeUpdate();

            logger.info(
                    "Supermarket saved successfully!"
            );

        } catch (SQLException e) {

            logger.log(
                    Level.SEVERE,
                    "Error saving supermarket",
                    e
            );
        }
    }

    // =========================================
    // FIND ALL
    // =========================================
    @Override
    public List<SupermarketModel> findAllSupermarkets() {

        List<SupermarketModel> supermarkets =
                new ArrayList<>();

        String sql = """
            SELECT *
            FROM supermarkets
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

                SupermarketModel supermarket =
                        new SupermarketModel();

                supermarket.setId(
                        resultSet.getInt(
                                "supermarkets_id"
                        )
                );

                supermarket.setName(
                        resultSet.getString(
                                "name"
                        )
                );

                supermarkets.add(
                        supermarket
                );
            }

        } catch (SQLException e) {

            logger.log(
                    Level.SEVERE,
                    "Error loading supermarkets",
                    e
            );
        }

        return supermarkets;
    }

    // =========================================
    // FIND BY ID
    // =========================================
    @Override
    public SupermarketModel findSupermarketById(
            Integer supermarketsId
    ) {

        String sql = """
            SELECT *
            FROM supermarkets
            WHERE supermarkets_id = ?
            """;

        try (

                Connection conn =
                        Database.connect();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)

        ) {

            stmt.setInt(
                    1,
                    supermarketsId
            );

            ResultSet resultSet =
                    stmt.executeQuery();

            if (resultSet.next()) {

                SupermarketModel supermarket =
                        new SupermarketModel();

                supermarket.setId(
                        resultSet.getInt(
                                "supermarkets_id"
                        )
                );

                supermarket.setName(
                        resultSet.getString(
                                "name"
                        )
                );

                return supermarket;
            }

        } catch (SQLException e) {

            logger.log(
                    Level.SEVERE,
                    "Error finding supermarket",
                    e
            );
        }

        return null;
    }

    // =========================================
    // UPDATE
    // =========================================
    @Override
    public void updateSupermarket(
            SupermarketModel supermarketModel
    ) {

        String sql = """
            UPDATE supermarkets
            SET name = ?
            WHERE supermarkets_id = ?
            """;

        try (

                Connection conn =
                        Database.connect();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)

        ) {

            stmt.setString(
                    1,
                    supermarketModel.getName()
            );

            stmt.setInt(
                    2,
                    supermarketModel.getId()
            );

            stmt.executeUpdate();

            logger.info(
                    "Supermarket updated successfully!"
            );

        } catch (SQLException e) {

            logger.log(
                    Level.SEVERE,
                    "Error updating supermarket",
                    e
            );
        }
    }

    // =========================================
    // DELETE
    // =========================================
    @Override
    public void deleteSupermarket(
            Integer supermarketsId
    ) {

        String sql = """
            DELETE FROM supermarkets
            WHERE supermarkets_id = ?
            """;

        try (

                Connection conn =
                        Database.connect();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)

        ) {

            stmt.setInt(
                    1,
                    supermarketsId
            );

            stmt.executeUpdate();

            logger.info(
                    "Supermarket deleted successfully!"
            );

        } catch (SQLException e) {

            logger.log(
                    Level.SEVERE,
                    "Error deleting supermarket",
                    e
            );
        }
    }
}