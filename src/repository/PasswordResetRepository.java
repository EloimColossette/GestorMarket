package repository;

import model.UserModel;
import java.sql.Timestamp;

public interface PasswordResetRepository {

    void salvarToken(int userId, String token, Timestamp expiration) throws Exception;

    UserModel buscarPorToken(String token) throws Exception;

    boolean tokenValido(String token) throws Exception;

    void deletarToken(String token) throws Exception;

    void deletarExpirados() throws Exception;

    void deletarPorUsuario(int userId) throws Exception;
}