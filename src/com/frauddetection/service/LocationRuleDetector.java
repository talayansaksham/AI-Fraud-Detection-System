package com.frauddetection.service;

import com.frauddetection.dao.UserDAO;
import com.frauddetection.model.Transaction;
import com.frauddetection.model.User;
import java.sql.SQLException;

public class LocationRuleDetector implements FraudDetector {
    private final UserDAO userDAO;
    public LocationRuleDetector(UserDAO userDAO) { this.userDAO = userDAO; }

    @Override
    public String check(Transaction txn) {
        try {
            User user = userDAO.getUserById(txn.getUserId());
            if (user != null && user.getUsualLocation() != null
                    && !user.getUsualLocation().equalsIgnoreCase(txn.getLocation())) {
                return "Transaction location '" + txn.getLocation()
                        + "' differs from usual location '" + user.getUsualLocation() + "'";
            }
        } catch (SQLException e) {
            System.err.println("LocationRuleDetector could not query DB: " + e.getMessage());
        }
        return null;
    }

    @Override
    public String getName() { return "Location Rule"; }
}
