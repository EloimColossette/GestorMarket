package repository;

import database.Database;
import model.UserModel;

import java.sql.*;

public class PasswordResetRepositoryImpl implements PasswordResetRepository {

    @Override
    public void salvarToken(int userId, String token, Timestamp expiration) throws Exception {

        String sql = "INSERT INTO password_reset_tokens (user_id, token, expiration) VALUES (?, ?, ?)";

        try (
                Connection conn = Database.conectar();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, userId);
            stmt.setString(2, token);
            stmt.setTimestamp(3, expiration);

            stmt.executeUpdate();
        }
    }

    @Override
    public UserModel buscarPorToken(String token) throws Exception {

        String sql = """
                SELECT u.id_user, u.email, u.first_name, u.last_name, u.cpf, u.phone_number
                FROM users u
                JOIN password_reset_tokens t ON u.id_user = t.user_id
                WHERE t.token = ? AND t.expiration > NOW()
        """;

        try (
                Connection conn = Database.conectar();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, token);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UserModel user = new UserModel();

                user.setId(rs.getInt("id_user"));
                user.setEmail(rs.getString("email"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setCpf(rs.getString("cpf"));
                user.setPhoneNumber(rs.getString("phone_number"));

                return user;
            }
        }

        return null;
    }

    @Override
    public boolean tokenValido(String token) throws Exception {

        String sql = "SELECT expiration FROM password_reset_tokens WHERE token = ?";

        try (
                Connection conn = Database.conectar();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, token);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Timestamp expiration = rs.getTimestamp("expiration");
                return expiration.after(new Timestamp(System.currentTimeMillis()));
            }
        }

        return false;
    }

    @Override
    public void deletarToken(String token) throws Exception {

        String sql = "DELETE FROM password_reset_tokens WHERE token = ?";

        try (
                Connection conn = Database.conectar();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, token);
            stmt.executeUpdate();
        }
    }

    @Override
    public void deletarExpirados() throws Exception {

        String sql = "DELETE FROM password_reset_tokens WHERE expiration < NOW()";

        try (
                Connection conn = Database.conectar();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            int linhas = stmt.executeUpdate();
            System.out.println("[TOKEN] Tokens expirados removidos: " + linhas);
        }
    }

    @Override
    public void deletarPorUsuario(int userId) throws Exception {

        String sql = "DELETE FROM password_reset_tokens WHERE user_id = ?";

        try (
                Connection conn = Database.conectar();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
}