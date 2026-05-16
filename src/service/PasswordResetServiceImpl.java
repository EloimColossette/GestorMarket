package service;

import exception.ApiException;
import model.UserModel;
import repository.UserRepository;
import repository.PasswordResetRepository;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.logging.Logger;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class PasswordResetServiceImpl implements PasswordResetService {

    private static final Logger logger = Logger.getLogger(PasswordResetServiceImpl.class.getName());

    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final EmailService emailService;
    private final Argon2 argon2 = Argon2Factory.create();

    public PasswordResetServiceImpl(
            UserRepository userRepository,
            PasswordResetRepository passwordResetRepository
    ) {
        this.userRepository = userRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.emailService = new EmailService();
    }

    // =========================
    // SOLICITAR RESET
    // =========================
    @Override
    public void solicitarResetSenha(String email) throws Exception {

        passwordResetRepository.deletarExpirados();

        UserModel user = userRepository.buscarEmail(email);

        if (user == null) {
            throw new ApiException("Email não encontrado", 404);
        }

        passwordResetRepository.deletarPorUsuario(user.getId());

        String token = UUID.randomUUID().toString();

        Timestamp expiration = new Timestamp(
                System.currentTimeMillis() + (1000 * 60 * 10)
        );

        passwordResetRepository.salvarToken(user.getId(), token, expiration);

        emailService.enviarEmailRecuperacao(email, token);

        logger.info("Token enviado para: " + email);
    }

    // =========================
    // REDEFINIR SENHA
    // =========================
    @Override
    public void redefinirSenha(String token, String newPassword) throws Exception {

        if (!passwordResetRepository.tokenValido(token)) {

            passwordResetRepository.deletarToken(token);

            throw new ApiException(
                    "Token inválido ou expirado",
                    404
            );
        }

        UserModel user = passwordResetRepository.buscarPorToken(token);

        if (user == null) {
            throw new ApiException("Token inválido", 404);
        }

        validarSenha(newPassword);

        String passwordHash = argon2.hash(3, 65536, 1, newPassword.toCharArray());

        userRepository.atualizarUsuario(
                user.getId(),
                user.getEmail(),
                passwordHash,
                user.getFirstName(),
                user.getLastName(),
                user.getCpf(),
                user.getPhoneNumber()
        );

        passwordResetRepository.deletarToken(token);

        logger.info("Senha redefinida: usuário " + user.getId());
    }

    // =========================
    // VALIDAÇÃO
    // =========================
    private void validarSenha(String password) {

        if (password == null || password.isEmpty()) {
            throw new ApiException("Senha obrigatória", 400);
        }

        if (password.length() < 6) {
            throw new ApiException("Senha deve ter no mínimo 6 caracteres", 400);
        }

        if (!password.matches(".*[0-9].*")) {
            throw new ApiException("Senha deve conter número", 400);
        }

        if (!password.matches(".*[!@#$%^&*()_+=-].*")) {
            throw new ApiException("Senha deve conter caractere especial", 400);
        }
    }
}