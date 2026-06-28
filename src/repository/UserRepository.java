package repository;

import model.UserModel;
import java.util.List;

public interface UserRepository {

    List<UserModel> findAllUsers() throws Exception;

    void createUser(String email, String password, String firstName,
                    String lastName, String cpf, String phoneNumber) throws Exception;

    void updateUser(int id, String email, String password,
                    String firstName, String lastName,
                    String cpf, String phoneNumber) throws Exception;

    void partialUpdateUser(int id, String email, String password,
                           String firstName, String lastName,
                           String cpf, String phoneNumber) throws Exception;

    void deleteUser(int id) throws Exception;

    UserModel findByEmail(String email) throws Exception;
}
