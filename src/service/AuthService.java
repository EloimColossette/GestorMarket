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
            UserModel user = userRepository.buscarEmail(email);

            if (user == null) {
                throw new ApiException("Usuário não encontrado", 404);
            }

            boolean valido = argon2.verify(
                    user.getPassword(),
                    password.toCharArray()
            );

            if (!valido) {
                throw new ApiException("Usuário ou senha inválidos", 401);
            }

            return JwtUtil.generateToken(user.getEmail());

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Erro interno", 500);
        }
    }
}