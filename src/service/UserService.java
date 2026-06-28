package service;

import model.UserModel;
import dto.UserRequest;
import java.util.List;

public interface UserService {

    List<UserModel> findAllUsers() throws Exception;

    void createUser(UserRequest user) throws Exception;

    void updateUser(int id, UserRequest user) throws Exception;

    void partialUpdateUser(int id, UserRequest user) throws Exception;

    void deleteUser(int id) throws Exception;
}
