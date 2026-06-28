package repository;

import database.Database;
import exception.ApiException;
import model.UserModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepositoryImpl implements UserRepository {

    @Override
    public List<UserModel> findAllUsers() {

        String sql = "SELECT user_id, first_name, last_name, cpf, email, phone_number FROM users";
        List<UserModel> users = new ArrayList<>();

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                UserModel user = new UserModel();
                user.setId(rs.getInt("user_id"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setCpf(rs.getString("cpf"));
                user.setEmail(rs.getString("email"));
                user.setPhoneNumber(rs.getString("phone_number"));
                users.add(user);
            }

        } catch (Exception e) {
            System.out.println("[UserRepository] error in findAllUsers: " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }

    @Override
    public void createUser(String email, String password, String firstName,
                           String lastName, String cpf, String phoneNumber) {

        String sql = "INSERT INTO users (email, password, first_name, last_name, cpf, phone_number) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.setString(3, firstName);
            stmt.setString(4, lastName);
            stmt.setString(5, cpf);
            stmt.setString(6, phoneNumber);
            stmt.executeUpdate();

        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg.contains("unique_email"))        throw new ApiException("Email already registered", 409);
            if (msg.contains("unique_cpf"))          throw new ApiException("CPF already registered", 409);
            if (msg.contains("unique_phone_number")) throw new ApiException("Phone already registered", 409);
            throw new ApiException("Error creating user", 409);
        }
    }

    @Override
    public void updateUser(int id, String email, String password,
                           String firstName, String lastName,
                           String cpf, String phoneNumber) {

        String sql = """
                UPDATE users
                SET email = ?, password = ?, first_name = ?, last_name = ?, cpf = ?, phone_number = ?
                WHERE user_id = ?
                """;

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.setString(3, firstName);
            stmt.setString(4, lastName);
            stmt.setString(5, cpf);
            stmt.setString(6, phoneNumber);
            stmt.setInt(7, id);
            stmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Error updating user", e);
        }
    }

    @Override
    public void partialUpdateUser(int id, String email, String password,
                                  String firstName, String lastName,
                                  String cpf, String phoneNumber) {

        StringBuilder sql = new StringBuilder("UPDATE users SET ");
        List<Object> values = new ArrayList<>();

        if (email != null && !email.isEmpty())             { sql.append("email = ?, ");        values.add(email); }
        if (password != null && !password.isEmpty())       { sql.append("password = ?, ");     values.add(password); }
        if (firstName != null && !firstName.isEmpty())     { sql.append("first_name = ?, ");   values.add(firstName); }
        if (lastName != null && !lastName.isEmpty())       { sql.append("last_name = ?, ");    values.add(lastName); }
        if (cpf != null && !cpf.isEmpty())                 { sql.append("cpf = ?, ");          values.add(cpf); }
        if (phoneNumber != null && !phoneNumber.isEmpty()) { sql.append("phone_number = ?, "); values.add(phoneNumber); }

        if (values.isEmpty()) {
            System.out.println("[UserRepository] no fields to update");
            return;
        }

        sql.setLength(sql.length() - 2);
        sql.append(" WHERE user_id = ?");
        values.add(id);

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }
            stmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("[UserRepository] error in partialUpdateUser: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void deleteUser(int id) {

        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("[UserRepository] error in deleteUser: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public UserModel findByEmail(String email) throws Exception {

        String sql = "SELECT * FROM users WHERE email = ?";
        Connection conn = Database.connect();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UserModel user = new UserModel();
                user.setId(rs.getInt("user_id"));
                user.setEmail(rs.getString("email"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setCpf(rs.getString("cpf"));
                user.setPassword(rs.getString("password"));
                user.setPhoneNumber(rs.getString("phone_number"));
                return user;
            }

        } finally {
            conn.close();
        }

        return null;
    }
}
