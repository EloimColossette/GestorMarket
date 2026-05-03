package service;

public interface PasswordResetService {

    void solicitarResetSenha(String email) throws Exception;

    void redefinirSenha(String token, String newPassword) throws Exception;
}