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

public class SupermarketRepositoryImpl implements SupermarketRepository {

    private static final Logger logger =
            Logger.getLogger(SupermarketRepositoryImpl.class.getName());

    // =========================================
    // SAVE
    // =========================================
    @Override
    public void saveSupermarket(SupermarketModel supermarketModel) {

        String sql = """
            INSERT INTO supermarkets (name, user_id)
            VALUES (?, ?)
            """;

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, supermarketModel.getName());
            stmt.setInt(2, supermarketModel.getUserId());
            stmt.executeUpdate();

            logger.info("Supermarket saved successfully!");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error saving supermarket", e);
        }
    }

    // =========================================
    // FIND ALL — só do usuário logado
    // =========================================
    @Override
    public List<SupermarketModel> findAllSupermarketsByUser(Integer userId) {

        List<SupermarketModel> supermarkets = new ArrayList<>();

        String sql = """
            SELECT supermarkets_id, name, user_id
            FROM supermarkets
            WHERE user_id = ?
            """;

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    supermarkets.add(mapRow(resultSet));
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error loading supermarkets", e);
        }

        return supermarkets;
    }

    // =========================================
    // FIND BY ID — só se pertencer ao usuário
    // =========================================
    @Override
    public SupermarketModel findSupermarketByIdAndUser(Integer supermarketsId, Integer userId) {

        String sql = """
            SELECT supermarkets_id, name, user_id
            FROM supermarkets
            WHERE supermarkets_id = ? AND user_id = ?
            """;

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supermarketsId);
            stmt.setInt(2, userId);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding supermarket", e);
        }

        return null;
    }

    // =========================================
    // UPDATE
    // =========================================
    @Override
    public void updateSupermarket(SupermarketModel supermarketModel) {

        // user_id na cláusula WHERE = segunda trava contra IDOR,
        // mesmo que o service esqueça de checar antes.
        String sql = """
            UPDATE supermarkets
            SET name = ?
            WHERE supermarkets_id = ? AND user_id = ?
            """;

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, supermarketModel.getName());
            stmt.setInt(2, supermarketModel.getId());
            stmt.setInt(3, supermarketModel.getUserId());
            stmt.executeUpdate();

            logger.info("Supermarket updated successfully!");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating supermarket", e);
        }
    }

    // =========================================
    // DELETE
    // =========================================
    public void deleteSupermarket(Integer supermarketsId, Integer userId) {

        String sql = """
            DELETE FROM supermarkets
            WHERE supermarkets_id = ? AND user_id = ?
            """;

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supermarketsId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();

            logger.info("Supermarket deleted successfully!");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting supermarket", e);
        }
    }

    @Override
    public void deleteSupermarket(Integer supermarketsId) {
        throw new UnsupportedOperationException("Use deleteSupermarket(id, userId)");
    }

    private SupermarketModel mapRow(ResultSet rs) throws SQLException {
        SupermarketModel supermarket = new SupermarketModel();
        supermarket.setId(rs.getInt("supermarkets_id"));
        supermarket.setName(rs.getString("name"));
        supermarket.setUserId(rs.getInt("user_id"));
        return supermarket;
    }
}