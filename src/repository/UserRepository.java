package repository;

import model.UserModel;
import java.util.List;

public interface UserRepository {

    List<UserModel> listarUsuarios() throws Exception;

    void criarUsuario(String email, String password, String firstName,
                      String lastName, String cpf, String phoneNumber) throws Exception;

    void atualizarUsuario(int id, String email, String password,
                          String firstName, String lastName,
                          String cpf, String phoneNumber) throws Exception;

    void atualizarParcialmenteUsuario(int id, String email, String password,
                                      String firstName, String lastName,
                                      String cpf, String phoneNumber) throws Exception;

    void deletarUsuario(int id) throws Exception;

    UserModel buscarEmail(String email) throws Exception;
}