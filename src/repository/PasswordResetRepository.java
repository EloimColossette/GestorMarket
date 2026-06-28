package repository;

import model.UserModel;
import java.sql.Timestamp;

public interface PasswordResetRepository {

    void saveToken(int userId, String token, Timestamp expiration) throws Exception;

    UserModel findByToken(String token) throws Exception;

    boolean isTokenValid(String token) throws Exception;

    void deleteToken(String token) throws Exception;

    void deleteExpiredTokens() throws Exception;

    void deleteTokensByUser(int userId) throws Exception;
}
