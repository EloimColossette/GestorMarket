package server;

import com.sun.net.httpserver.HttpServer;
import controller.*;

import repository.*;
import service.*;

import security.CorsFilter;

import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static final Logger logger = Logger.getLogger(Server.class.getName());

    public static void start() {

        try {
            logger.info("Criando servidor na porta 8080...");

            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

            // =========================
            // REPOSITORIES
            // =========================
            UserRepository userRepository = new UserRepositoryImpl();
            PasswordResetRepository passwordResetRepository = new PasswordResetRepositoryImpl();
            PurchaseRepository purchaseRepository = new PurchaseRepositoryImpl();
            PurchaseItemRepository purchaseItemRepository = new PurchaseItemRepositoryImpl();

            // =========================
            // SERVICES
            // =========================
            UserService userService = new UserServiceImpl(userRepository);
            AuthService authService = new AuthService(userRepository);
            PasswordResetService passwordResetService =
                    new PasswordResetServiceImpl(userRepository, passwordResetRepository);
            PurchaseService purchaseService = new PurchaseServiceImpl(purchaseRepository);
            PurchaseItemService purchaseItemService = new PurchaseItemServiceImpl(purchaseItemRepository);


            // =========================
            // CONTROLLERS
            // =========================
            UsuarioController usuarioController = new UsuarioController(userService);
            AuthController authController = new AuthController(authService);
            PasswordResetController passwordResetController =
                    new PasswordResetController(passwordResetService);
            PurchaseController purchaseController = new PurchaseController(purchaseService);
            PurchaseItemController purchaseItemController = new PurchaseItemController(purchaseItemService);

            // =========================
            // ROUTES
            // =========================
            server.createContext("/usuarios", usuarioController)
                    .getFilters().add(new CorsFilter());
            server.createContext("/password", passwordResetController)
                    .getFilters().add(new CorsFilter());
            server.createContext("/login", authController)
                    .getFilters().add(new CorsFilter());
            server.createContext("/purchases", purchaseController)
                    .getFilters().add(new CorsFilter());

            server.createContext("/purchase-items", purchaseItemController)
                    .getFilters().add(new CorsFilter());

            // "/" por último e com o mais longo match possível
            server.createContext("/", new StaticFileHandler("public"))
                    .getFilters().add(new CorsFilter());

            server.setExecutor(null);
            server.start();

            logger.info("Servidor rodando em http://localhost:8080");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao iniciar servidor", e);
        }
    }
}