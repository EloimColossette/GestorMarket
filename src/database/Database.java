package database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import envloader.EnvLoader;

import java.sql.Connection;
import java.sql.SQLException;

public class Database {

    private static final HikariDataSource dataSource;

    // Ajustes de pool com valores padrão sensatos para uma aplicação
    // pequena/média. Podem ser tunados aqui se a carga aumentar.
    private static final int MAX_POOL_SIZE = 10;
    private static final int MIN_IDLE = 2;
    private static final long CONNECTION_TIMEOUT_MS = 30_000;   // tempo máx. esperando uma conexão livre
    private static final long IDLE_TIMEOUT_MS = 10 * 60_000;    // tempo máx. ociosa antes de ser fechada
    private static final long MAX_LIFETIME_MS = 30 * 60_000;    // tempo máx. de vida de uma conexão

    static {
        HikariConfig config = new HikariConfig();
        config.setPoolName("SistemaComprasPool");
        config.setJdbcUrl(EnvLoader.get("DB_URL"));
        config.setUsername(EnvLoader.get("DB_USER"));
        config.setPassword(EnvLoader.get("DB_PASSWORD"));
        config.setDriverClassName("org.postgresql.Driver");

        config.setMaximumPoolSize(MAX_POOL_SIZE);
        config.setMinimumIdle(MIN_IDLE);
        config.setConnectionTimeout(CONNECTION_TIMEOUT_MS);
        config.setIdleTimeout(IDLE_TIMEOUT_MS);
        config.setMaxLifetime(MAX_LIFETIME_MS);

        // Prepared statement caching no driver do Postgres (recomendado pelo
        // próprio guia oficial do HikariCP para reduzir overhead de parsing).
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);

        // Fecha o pool de forma limpa quando a aplicação for encerrada
        // (Ctrl+C, kill, etc.), evitando conexões "penduradas" no Postgres.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!dataSource.isClosed()) {
                dataSource.close();
                System.out.println("[Database] Connection pool closed.");
            }
        }));

        System.out.println("[Database] Connection pool initialized (maxPoolSize=" + MAX_POOL_SIZE + ")");
    }

    /**
     * Pega uma conexão emprestada do pool. Quem chamar é responsável por
     * fechá-la (try-with-resources) para que ela volte ao pool — exatamente
     * como já era feito antes.
     */
    public static Connection connect() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
            return null;
        }
    }
}