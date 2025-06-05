package com.walkmanx21.junit.dao;

import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static java.sql.DriverManager.getConnection;

public class UserDao {

    @SneakyThrows
    public boolean delete(Integer userId) {
        try (Connection connection = getConnection("url", "username", "password")) {
            return true;
        }
    }
}
