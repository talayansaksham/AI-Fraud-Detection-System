package com.frauddetection.dao;

import com.frauddetection.model.Transaction;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public int addTransaction(Transaction txn) throws SQLException {
        String sql = "INSERT INTO transactions (user_id, amount, location, merchant_category, timestamp, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, txn.getUserId());
            ps.setDouble(2, txn.getAmount());
            ps.setString(3, txn.getLocation());
            ps.setString(4, txn.getMerchantCategory());
            ps.setTimestamp(5, Timestamp.valueOf(txn.getTimestamp()));
            ps.setString(6, txn.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public void updateStatus(int txnId, String newStatus) throws SQLException {
        String sql = "UPDATE transactions SET status = ? WHERE txn_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, txnId);
            ps.executeUpdate();
        }
    }

    public List<Transaction> getAllTransactions() throws SQLException {
        String sql = "SELECT txn_id, user_id, amount, location, merchant_category, timestamp, status "
                + "FROM transactions ORDER BY timestamp DESC";
        return runQuery(sql);
    }

    public List<Transaction> getTransactionsByUser(int userId) throws SQLException {
        String sql = "SELECT txn_id, user_id, amount, location, merchant_category, timestamp, status "
                + "FROM transactions WHERE user_id = ? ORDER BY timestamp DESC";
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int countRecentTransactionsByUser(int userId, int withinMinutes) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM transactions "
                + "WHERE user_id = ? AND timestamp >= (NOW() - INTERVAL ? MINUTE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, withinMinutes);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("cnt");
            }
        }
        return 0;
    }

    private List<Transaction> runQuery(String sql) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getInt("txn_id"), rs.getInt("user_id"), rs.getDouble("amount"),
                rs.getString("location"), rs.getString("merchant_category"),
                rs.getTimestamp("timestamp").toLocalDateTime(), rs.getString("status")
        );
    }
}
