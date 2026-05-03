package service;

import exception.ApiException;
import model.UserModel;
import repository.UserRepository;
import dto.UserRequest;
import java.util.Hashtable;
import java.util.List;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final Argon2 argon2 = Argon2Factory.create();

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserModel> listarUsuarios() throws Exception {
        return userRepository.listarUsuarios();
    }

    @Override
    public void criarUsuario(UserRequest usuario) throws Exception {

        if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
            throw new ApiException("Password Obrigatório", 400);
        }

        validatePassword(usuario.getPassword());
        validatePhone(usuario.getPhoneNumber());
        validateEmail(usuario.getEmail());

        String encryptedPassword = hashPassword(usuario.getPassword());

        userRepository.criarUsuario(
                usuario.getEmail(),
                encryptedPassword,
                usuario.getFirstName(),
                usuario.getLastName(),
                usuario.getCpf(),
                usuario.getPhoneNumber()
        );
    }

    @Override
    public void atualizarUsuario(int id, UserRequest usuario) throws Exception {

        if (id <= 0) {
            throw new ApiException("ID inválido", 400);
        }

        if (usuario.getPhoneNumber() != null && !usuario.getPhoneNumber().isEmpty()) {
            validatePhone(usuario.getPhoneNumber());
        }
        if (usuario.getEmail() != null && !usuario.getEmail().isEmpty()) {
            validateEmail(usuario.getEmail());
        }

        String password = usuario.getPassword();

        if (password != null && !password.isEmpty()) {
            validatePassword(password);
            password = hashPassword(password);
        }

        userRepository.atualizarUsuario(
                id,
                usuario.getEmail(),
                password,
                usuario.getFirstName(),
                usuario.getLastName(),
                usuario.getCpf(),
                usuario.getPhoneNumber()
        );
    }

    @Override
    public void atualizarParcialmenteUsuario(int id, UserRequest usuario) throws Exception {

        if (id <= 0) {
            throw new ApiException("ID inválido", 400);
        }

        if (usuario.getPhoneNumber() != null && !usuario.getPhoneNumber().isEmpty()) {
            validatePhone(usuario.getPhoneNumber());
        }

        if (usuario.getEmail() != null && !usuario.getEmail().isEmpty()) {
            validateEmail(usuario.getEmail());
        }

        String password = usuario.getPassword();

        if (password != null && !password.isEmpty()) {
            validatePassword(password);
            password = hashPassword(password);
        }

        userRepository.atualizarParcialmenteUsuario(
                id,
                usuario.getEmail(),
                password,
                usuario.getFirstName(),
                usuario.getLastName(),
                usuario.getCpf(),
                usuario.getPhoneNumber()
        );
    }

    @Override
    public void excluirUsuario(int id) throws Exception {

        if (id <= 0) {
            throw new ApiException("ID inválido", 400);
        }

        userRepository.deletarUsuario(id);
    }

    // =========================
    // VALIDAÇÕES
    // =========================

    private void validatePhone(String phone) {

        if (phone == null || phone.isEmpty()) {
            throw new ApiException("Telefone obrigatório", 400);
        }

        if (!phone.matches("^\\+?[0-9]{10,15}$")) {
            throw new ApiException("Telefone inválido", 400);
        }
    }

    private void validateEmail(String email) {

        if (email == null || email.isEmpty()) {
            throw new ApiException("Email obrigatório", 400);
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ApiException("Email inválido", 400);
        }

        if (!dominioExiste(email)) {
            throw new ApiException("Domínio de email inválido", 400);
        }
    }

    private boolean dominioExiste(String email) {

        try {
            String domain = email.substring(email.indexOf("@") + 1);

            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");

            DirContext ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(domain, new String[]{"MX"});
            Attribute attr = attrs.get("MX");

            return attr != null && attr.size() > 0;

        } catch (Exception e) {
            return false;
        }
    }

    private String hashPassword(String password) {
        return argon2.hash(3, 65536, 1, password.toCharArray());
    }

    private void validatePassword(String password) {
        if(password.length() < 6) {
            throw new ApiException("A senha deve conter no minino 6 caracteres", 400);
        }
        if(!password.matches(".*[0-9].*")) {
            throw new ApiException("Senha deve conter numeros", 400);
        }
        if(!password.matches(".*[!@#$%^&*()_+=-].*")) {
            throw new ApiException("Senha deve conter caractere especiais", 400);
        }
    }
}