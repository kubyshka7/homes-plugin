package com.example.homes.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private final JavaPlugin plugin;
    private HikariDataSource dataSource;

    public Database(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        FileConfiguration config = plugin.getConfig();

        String host     = config.getString("storage.postgres.host", "localhost");
        int    port     = config.getInt("storage.postgres.port", 5432);
        String database = config.getString("storage.postgres.database", "minecraft");
        String schema   = config.getString("storage.postgres.schema", "public");
        String user     = config.getString("storage.postgres.user", "mc");
        String password = config.getString("storage.postgres.password", "secret");
        boolean ssl     = config.getBoolean("storage.postgres.ssl", false);

        String url = String.format(
                "jdbc:postgresql://%s:%d/%s?currentSchema=%s&ssl=%b",
                host, port, database, schema, ssl
        );

        HikariConfig hikari = new HikariConfig();
        hikari.setDriverClassName("org.postgresql.Driver");
        hikari.setJdbcUrl(url);
        hikari.setUsername(user);
        hikari.setPassword(password);
        hikari.setMaximumPoolSize(config.getInt("storage.postgres.hikari.maximumPoolSize", 10));
        hikari.setMinimumIdle(config.getInt("storage.postgres.hikari.minimumIdle", 2));
        hikari.setConnectionTimeout(config.getLong("storage.postgres.hikari.connectionTimeoutMs", 10000));

        dataSource = new HikariDataSource(hikari);

        createTable();
    }

    private void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS homes (
                player_uuid UUID        NOT NULL,
                home_name   TEXT        NOT NULL,
                world       TEXT        NOT NULL,
                x           DOUBLE PRECISION NOT NULL,
                y           DOUBLE PRECISION NOT NULL,
                z           DOUBLE PRECISION NOT NULL,
                yaw         REAL        NOT NULL,
                pitch       REAL        NOT NULL,
                created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                PRIMARY KEY (player_uuid, home_name)
            )
            """;
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
            st.execute(sql);
            plugin.getLogger().info("Таблица homes готова.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Не удалось создать таблицу: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null) dataSource.close();
    }
}