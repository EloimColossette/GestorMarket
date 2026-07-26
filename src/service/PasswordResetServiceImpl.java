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

    public PasswordResetServiceImpl(UserRepository userRepository,
                                    PasswordResetRepository passwordResetRepository) {
        this.userRepository = userRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.emailService = new EmailService();
    }

    @Override
    public void requestPasswordReset(String email) throws Exception {

        passwordResetRepository.deleteExpiredTokens();

        UserModel user = userRepository.findByEmail(email);

        // Sempre responde "sucesso" pra não revelar quais e-mails existem.
        // Só envia o e-mail de fato se o usuário existir.
        if (user != null) {
            passwordResetRepository.deleteTokensByUser(user.getId());

            String token = UUID.randomUUID().toString();
            Timestamp expiration = new Timestamp(System.currentTimeMillis() + (1000 * 60 * 10));

            passwordResetRepository.saveToken(user.getId(), token, expiration);
            emailService.enviarEmailRecuperacao(email, token);

            logger.info("Reset token sent to: " + email);
        } else {
            logger.info("Password reset requested for non-existent email");
        }
    }

    @Override
    public void resetPassword(String token, String newPassword) throws Exception {

        if (!passwordResetRepository.isTokenValid(token)) {
            passwordResetRepository.deleteToken(token);
            throw new ApiException("Invalid or expired token", 404);
        }

        UserModel user = passwordResetRepository.findByToken(token);

        if (user == null) {
            throw new ApiException("Invalid token", 404);
        }

        validatePassword(newPassword);

        String passwordHash = argon2.hash(3, 65536, 1, newPassword.toCharArray());

        userRepository.updateUser(
                user.getId(),
                user.getEmail(),
                passwordHash,
                user.getFirstName(),
                user.getLastName(),
                user.getCpf(),
                user.getPhoneNumber()
        );

        passwordResetRepository.deleteToken(token);

        logger.info("Password reset for user " + user.getId());
    }

    private void validatePassword(String password) {
        if (password == null || password.isEmpty())         throw new ApiException("Password is required", 400);
        if (password.length() < 6)                         throw new ApiException("Password must be at least 6 characters", 400);
        if (!password.matches(".*[0-9].*"))                throw new ApiException("Password must contain a number", 400);
        if (!password.matches(".*[!@#$%^&*()_+=-].*"))    throw new ApiException("Password must contain a special character", 400);
    }
}
