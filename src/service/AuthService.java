package service;

import exception.ApiException;
import model.UserModel;
import repository.UserRepository;
import security.JwtUtil;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class AuthService {

    private final UserRepository userRepository;
    private final Argon2 argon2 = Argon2Factory.create();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String login(String email, String password) {

        try {
            UserModel user = userRepository.findByEmail(email);

            // SECURITY: same message whether the email doesn't exist or the password is wrong.
            // Returning "user not found" for unknown emails would let attackers enumerate valid accounts.
            if (user == null) {
                throw new ApiException("Invalid email or password", 401);
            }

            boolean valid = argon2.verify(user.getPassword(), password.toCharArray());

            if (!valid) {
                throw new ApiException("Invalid email or password", 401);
            }

            return JwtUtil.generateToken(user.getEmail());

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Internal error", 500);
        }
    }
}
