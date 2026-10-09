package com.frauddetection.dao;

import com.frauddetection.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public int addUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, email, usual_location, password_hash) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getUsualLocation());
            ps.setString(4, user.getPassword());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public List<User> getAllUsers() throws SQLException {
        String sql = "SELECT user_id, username, email, usual_location, password_hash FROM users ORDER BY user_id";
        List<User> users = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) users.add(mapRow(rs));
        }
        return users;
    }

    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT user_id, username, email, usual_location, password_hash FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT user_id, username, email, usual_location, password_hash "
                + "FROM users WHERE username = ? AND password_hash = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"), rs.getString("username"), rs.getString("email"),
                rs.getString("usual_location"), rs.getString("password_hash")
        );
    }
}
