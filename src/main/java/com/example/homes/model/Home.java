package com.example.homes.model;

import java.util.UUID;

public record Home(
    UUID playerUuid,
    String homeName,
    String world,
    double x,
    double y,
    double z,
    float yaw,
    float pitch
){}
