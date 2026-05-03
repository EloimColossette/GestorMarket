package application;

import config.LogConfig;
import server.Server;
import java.util.logging.Logger;
import java.util.logging.Level;


public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args){
        try{
            LogConfig.config();
            
            logger.info("Iniciando Servidor...");

            Server.start();

        }catch (Exception e){
            logger.severe("Erro ao iniciar o servidor!");
            logger.log(Level.SEVERE, "Detalhes do erro: ", e);
        }
        
    }
    
}
