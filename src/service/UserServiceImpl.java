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
    public List<UserModel> findAllUsers() throws Exception {
        return userRepository.findAllUsers();
    }

    @Override
    public void createUser(UserRequest user) throws Exception {

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new ApiException("Password is required", 400);
        }

        validatePassword(user.getPassword());
        validatePhone(user.getPhoneNumber());
        validateEmail(user.getEmail());

        String encryptedPassword = hashPassword(user.getPassword());

        userRepository.createUser(
                user.getEmail(),
                encryptedPassword,
                user.getFirstName(),
                user.getLastName(),
                user.getCpf(),
                user.getPhoneNumber()
        );
    }

    @Override
    public void updateUser(int id, UserRequest user) throws Exception {

        if (id <= 0) throw new ApiException("Invalid ID", 400);

        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) validatePhone(user.getPhoneNumber());
        if (user.getEmail()       != null && !user.getEmail().isEmpty())       validateEmail(user.getEmail());

        String password = user.getPassword();
        if (password != null && !password.isEmpty()) {
            validatePassword(password);
            password = hashPassword(password);
        }

        userRepository.updateUser(
                id,
                user.getEmail(),
                password,
                user.getFirstName(),
                user.getLastName(),
                user.getCpf(),
                user.getPhoneNumber()
        );
    }

    @Override
    public void partialUpdateUser(int id, UserRequest user) throws Exception {

        if (id <= 0) throw new ApiException("Invalid ID", 400);

        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) validatePhone(user.getPhoneNumber());
        if (user.getEmail()       != null && !user.getEmail().isEmpty())       validateEmail(user.getEmail());

        String password = user.getPassword();
        if (password != null && !password.isEmpty()) {
            validatePassword(password);
            password = hashPassword(password);
        }

        userRepository.partialUpdateUser(
                id,
                user.getEmail(),
                password,
                user.getFirstName(),
                user.getLastName(),
                user.getCpf(),
                user.getPhoneNumber()
        );
    }

    @Override
    public void deleteUser(int id) throws Exception {
        if (id <= 0) throw new ApiException("Invalid ID", 400);
        userRepository.deleteUser(id);
    }

    // ── VALIDATION ──────────────────────────────────────

    private void validatePhone(String phone) {
        if (phone == null || phone.isEmpty()) throw new ApiException("Phone is required", 400);
        if (!phone.matches("^\\+?[0-9]{10,15}$")) throw new ApiException("Invalid phone number", 400);
    }

    private void validateEmail(String email) {
        if (email == null || email.isEmpty()) throw new ApiException("Email is required", 400);
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) throw new ApiException("Invalid email", 400);
        if (!domainExists(email)) throw new ApiException("Invalid email domain", 400);
    }

    private boolean domainExists(String email) {
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
        if (password.length() < 6)                         throw new ApiException("Password must be at least 6 characters", 400);
        if (!password.matches(".*[0-9].*"))                throw new ApiException("Password must contain a number", 400);
        if (!password.matches(".*[!@#$%^&*()_+=-].*"))    throw new ApiException("Password must contain a special character", 400);
    }
}
