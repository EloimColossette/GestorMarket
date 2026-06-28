package server;

import com.sun.net.httpserver.HttpServer;
import controller.*;

import repository.*;
import service.*;

import security.AuthFilter;
import security.CorsFilter;

import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static final Logger logger = Logger.getLogger(Server.class.getName());

    public static void start() {

        try {
            logger.info("Starting server on port 8080...");

            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

            // =========================
            // REPOSITORIES
            // =========================
            UserRepository         userRepository         = new UserRepositoryImpl();
            PasswordResetRepository passwordResetRepository = new PasswordResetRepositoryImpl();
            SupermarketRepository  supermarketRepository  = new SupermarketRepositoryImpl();
            PurchaseRepository     purchaseRepository     = new PurchaseRepositoryImpl();
            PurchaseItemRepository purchaseItemRepository = new PurchaseItemRepositoryImpl();

            // =========================
            // SERVICES
            // =========================
            UserService         userService         = new UserServiceImpl(userRepository);
            AuthService         authService         = new AuthService(userRepository);
            PasswordResetService passwordResetService = new PasswordResetServiceImpl(userRepository, passwordResetRepository);
            SupermarketService  supermarketService  = new SupermarketServiceImpl(supermarketRepository);
            PurchaseService     purchaseService     = new PurchaseServiceImpl(purchaseRepository);
            PurchaseItemService purchaseItemService = new PurchaseItemServiceImpl(purchaseItemRepository);

            // =========================
            // CONTROLLERS
            // =========================
            // Renamed from UsuarioController to UserController
            UserController           userController           = new UserController(userService);
            AuthController           authController           = new AuthController(authService);
            PasswordResetController  passwordResetController  = new PasswordResetController(passwordResetService);
            SupermarketController    supermarketController    = new SupermarketController(supermarketService);
            PurchaseController       purchaseController       = new PurchaseController(purchaseService);
            PurchaseItemController   purchaseItemController   = new PurchaseItemController(purchaseItemService);

            // =========================
            // ROUTES
            // =========================
            // Public routes — no AuthFilter
            var ctxLogin    = server.createContext("/login",    authController);
            ctxLogin.getFilters().add(new CorsFilter());

            var ctxPassword = server.createContext("/password", passwordResetController);
            ctxPassword.getFilters().add(new CorsFilter());

            // Protected routes — CorsFilter + AuthFilter
            // (AuthFilter itself also allows POST /users and /password/* without a token)
            var ctxUsers = server.createContext("/users", userController);
            ctxUsers.getFilters().add(new CorsFilter());
            ctxUsers.getFilters().add(new AuthFilter());

            var ctxSupermarkets = server.createContext("/supermarkets", supermarketController);
            ctxSupermarkets.getFilters().add(new CorsFilter());
            ctxSupermarkets.getFilters().add(new AuthFilter());

            var ctxPurchases = server.createContext("/purchases", purchaseController);
            ctxPurchases.getFilters().add(new CorsFilter());
            ctxPurchases.getFilters().add(new AuthFilter());

            var ctxPurchaseItems = server.createContext("/purchase-items", purchaseItemController);
            ctxPurchaseItems.getFilters().add(new CorsFilter());
            ctxPurchaseItems.getFilters().add(new AuthFilter());

            // Static files — must be last (most generic path)
            server.createContext("/", new StaticFileHandler("public"))
                    .getFilters().add(new CorsFilter());

            server.setExecutor(null);
            server.start();

            logger.info("Server running at http://localhost:8080");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting server", e);
        }
    }
}
