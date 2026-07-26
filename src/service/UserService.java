package service;

import dto.UserRequest;
import model.UserModel;

import java.util.List;

public interface UserService {

    List<UserModel> findAllUsers() throws Exception;

    UserModel getOwnProfile(int authUserId) throws Exception;

    void createUser(UserRequest user) throws Exception;

    void updateUser(int id, int authUserId, UserRequest user) throws Exception;

    void partialUpdateUser(int id, int authUserId, UserRequest user) throws Exception;

    void deleteUser(int id, int authUserId) throws Exception;
}