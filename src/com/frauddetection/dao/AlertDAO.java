package com.frauddetection.dao;

import com.frauddetection.model.Alert;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlertDAO {

    public int addAlert(Alert alert) throws SQLException {
        String sql = "INSERT INTO alerts (txn_id, risk_level, reason, created_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, alert.getTxnId());
            ps.setString(2, alert.getRiskLevel());
            ps.setString(3, alert.getReason());
            ps.setTimestamp(4, Timestamp.valueOf(alert.getCreatedAt()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public List<Alert> getAllAlerts() throws SQLException {
        String sql = "SELECT a.alert_id, a.txn_id, a.risk_level, a.reason, a.created_at, t.status AS txn_status "
                + "FROM alerts a JOIN transactions t ON a.txn_id = t.txn_id "
                + "ORDER BY a.created_at DESC";
        List<Alert> alerts = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) alerts.add(mapRow(rs));
        }
        return alerts;
    }

    public List<Alert> getAlertsByUser(int userId) throws SQLException {
        String sql = "SELECT a.alert_id, a.txn_id, a.risk_level, a.reason, a.created_at, t.status AS txn_status "
                + "FROM alerts a JOIN transactions t ON a.txn_id = t.txn_id "
                + "WHERE t.user_id = ? AND t.status = 'FLAGGED' "
                + "ORDER BY a.created_at DESC";
        List<Alert> alerts = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) alerts.add(mapRow(rs));
            }
        }
        return alerts;
    }

    private Alert mapRow(ResultSet rs) throws SQLException {
        Alert alert = new Alert(
                rs.getInt("alert_id"), rs.getInt("txn_id"), rs.getString("risk_level"),
                rs.getString("reason"), rs.getTimestamp("created_at").toLocalDateTime()
        );
        alert.setTransactionStatus(rs.getString("txn_status"));
        return alert;
    }
}
