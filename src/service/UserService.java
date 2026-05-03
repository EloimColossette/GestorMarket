package service;

import model.UserModel;
import dto.UserRequest;
import java.util.List;

public interface UserService {

    List<UserModel> listarUsuarios() throws Exception;

    void criarUsuario(UserRequest usuario) throws Exception;

    void atualizarUsuario(int id, UserRequest usuario) throws Exception;

    void atualizarParcialmenteUsuario(int id, UserRequest usuario) throws Exception;

    void excluirUsuario(int id) throws Exception;
}