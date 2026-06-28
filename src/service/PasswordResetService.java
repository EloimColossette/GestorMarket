package service;

public interface PasswordResetService {

    void requestPasswordReset(String email) throws Exception;

    void resetPassword(String token, String newPassword) throws Exception;
}
