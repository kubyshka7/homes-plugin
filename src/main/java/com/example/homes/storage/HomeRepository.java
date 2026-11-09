package com.example.homes.storage;

import com.example.homes.model.Home;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HomeRepository {
    void saveHome(Home home);
    Optional<Home> findHome(UUID playerId, String name);
    List<String> findHomes(UUID playerId);
    boolean deleteHome(UUID playerId, String name);
    int countHomes(UUID playerUuid);
}
