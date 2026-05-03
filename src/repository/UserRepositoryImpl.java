package repository;

import database.Database;
import exception.ApiException;
import model.UserModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepositoryImpl implements UserRepository {

    @Override
    public List<UserModel> listarUsuarios() {

        String sql = "SELECT id_user, first_name, last_name, cpf, email, phone_number FROM users";

        List<UserModel> users = new ArrayList<>();

        try (Connection conn = Database.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                UserModel user = new UserModel();

                user.setId(rs.getInt("id_user"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setCpf(rs.getString("cpf"));
                user.setEmail(rs.getString("email"));
                user.setPhoneNumber(rs.getString("phone_number"));

                users.add(user);
            }

        } catch (Exception e) {
            System.out.println("[UserRepository] erro listarUsuarios: " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }

    @Override
    public void criarUsuario(String email, String password, String firstName,
                             String lastName, String cpf, String phoneNumber) {

        String sql = "INSERT INTO users (email, password, first_name, last_name, cpf, phone_number) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.conectar();
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

            if(msg.contains("unique_email")){
                throw new ApiException("Email ja está cadastrado", 409);
            }
            if(msg.contains("unique_cpf")){
                throw new ApiException("CPF ja esta cadastrado", 409);
            }
            if(msg.contains("unique_phone_number")){
                throw new ApiException("Phone ja esta cadastrado", 409);
            }
            throw new ApiException("Erro ao criar usuario", 409);
        }
    }

    @Override
    public void atualizarUsuario(int id, String email, String password,
                                 String firstName, String lastName,
                                 String cpf, String phoneNumber) {

        String sql = """
                UPDATE users 
                SET email = ?, password = ?, first_name = ?, last_name = ?, cpf = ?, phone_number = ?
                WHERE id_user = ?
                """;

        try (Connection conn = Database.conectar();
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
            throw new RuntimeException("Erro ao atualizar usuário", e);
        }
    }

    @Override
    public void atualizarParcialmenteUsuario(int id, String email, String password,
                                             String firstName, String lastName,
                                             String cpf, String phoneNumber) {

        StringBuilder sql = new StringBuilder("UPDATE users SET ");
        List<Object> values = new ArrayList<>();

        if (email != null && !email.isEmpty()) {
            sql.append("email = ?, ");
            values.add(email);
        }
        if (password != null && !password.isEmpty()) {
            sql.append("password = ?, ");
            values.add(password);
        }
        if (firstName != null && !firstName.isEmpty()) {
            sql.append("first_name = ?, ");
            values.add(firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            sql.append("last_name = ?, ");
            values.add(lastName);
        }
        if (cpf != null && !cpf.isEmpty()) {
            sql.append("cpf = ?, ");
            values.add(cpf);
        }
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            sql.append("phone_number = ?, ");
            values.add(phoneNumber);
        }

        if (values.isEmpty()) {
            System.out.println("[UserRepository] nenhum campo para atualizar");
            return;
        }

        sql.setLength(sql.length() - 2);
        sql.append(" WHERE id_user = ?");
        values.add(id);

        try (Connection conn = Database.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }

            stmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("[UserRepository] erro atualizarParcial: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void deletarUsuario(int id) {

        String sql = "DELETE FROM users WHERE id_user = ?";

        try (Connection conn = Database.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("[UserRepository] erro deletarUsuario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public UserModel buscarEmail(String email) throws Exception {
        System.out.println("[REPO] Iniciando buscarEmail: " + email);

        String sql = "SELECT * FROM users WHERE email = ?";

        System.out.println("[REPO] Abrindo conexão...");
        Connection conn = Database.conectar();
        System.out.println("[REPO] Conexão aberta!");

        try (
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, email);
            System.out.println("[REPO] Executando query...");
            ResultSet resultSet = stmt.executeQuery();
            System.out.println("[REPO] Query executada!");

            if (resultSet.next()) {
                UserModel user = new UserModel();
                user.setId(resultSet.getInt("id_user"));
                user.setEmail(resultSet.getString("email"));
                user.setFirstName(resultSet.getString("first_name"));
                user.setLastName(resultSet.getString("last_name"));
                user.setCpf(resultSet.getString("cpf"));
                user.setPassword(resultSet.getString("password"));
                user.setPhoneNumber(resultSet.getString("phone_number"));
                System.out.println("[REPO] Usuário encontrado: " + user.getId());
                return user;
            }
        } finally {
            conn.close();
        }

        System.out.println("[REPO] Usuário não encontrado");
        return null;
    }
}