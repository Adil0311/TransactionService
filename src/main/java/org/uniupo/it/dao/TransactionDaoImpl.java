package org.uniupo.it.dao;

import org.uniupo.it.model.Transaction;

import java.sql.*;

public class TransactionDaoImpl implements TransactionDao {

    @Override
    public void registerTransaction(Transaction transaction) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQLQueries.Transaction.INSERT_TRANSACTION)) {
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setString(2, transaction.getDrinkCode());
            stmt.setInt(3, transaction.getSugarQuantity());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int transactionId = rs.getInt("transactionId");
                System.out.println("Transaction registered with id: " + transactionId);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
