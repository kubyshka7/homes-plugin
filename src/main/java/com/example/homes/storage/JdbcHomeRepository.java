package com.example.homes.storage;

import com.example.homes.HomesPlugin;
import com.example.homes.model.Home;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcHomeRepository implements HomeRepository{
    private final Database database;

    public JdbcHomeRepository(Database database) {
        this.database = database;
    }

    @Override
    public void saveHome(Home home) {
        String sql = """
        INSERT INTO homes(player_uuid, home_name, world, x, y, z, yaw, pitch, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, now(), now())
        ON CONFLICT (player_uuid, home_name)
        DO UPDATE SET
            world      = EXCLUDED.world,
            x          = EXCLUDED.x,
            y          = EXCLUDED.y,
            z          = EXCLUDED.z,
            yaw        = EXCLUDED.yaw,
            pitch      = EXCLUDED.pitch,
            updated_at = now()
        """;
        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, home.playerUuid());
            ps.setString(2, home.homeName());
            ps.setString(3, home.world());
            ps.setDouble(4, home.x());
            ps.setDouble(5, home.y());
            ps.setDouble(6, home.z());
            ps.setFloat(7, home.yaw());
            ps.setFloat(8, home.pitch());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<Home> findHome(UUID playerUuid, String name) {
        String sql = "SELECT * FROM homes WHERE player_uuid = ? AND home_name = ?";
        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, playerUuid);
            ps.setString(2, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new Home(
                        playerUuid,
                        rs.getString("home_name"),
                        rs.getString("world"),
                        rs.getDouble("x"),
                        rs.getDouble("y"),
                        rs.getDouble("z"),
                        rs.getFloat("yaw"),
                        rs.getFloat("pitch")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<String> findHomes(UUID playerUuid) {
        List<String> homes = new ArrayList<>();
        String sql = "SELECT home_name FROM homes WHERE player_uuid = ? ORDER BY home_name";
        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, playerUuid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                homes.add(rs.getString("home_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return homes;
    }

    @Override
    public boolean deleteHome(UUID playerUuid, String homeName) {

        String sql = "DELETE FROM homes WHERE player_uuid = ? AND home_name = ?";

        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, playerUuid);
            ps.setString(2, homeName);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int countHomes(UUID playerUuid) {
        String sql = "SELECT COUNT(*) FROM homes WHERE player_uuid = ?";
        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, playerUuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
