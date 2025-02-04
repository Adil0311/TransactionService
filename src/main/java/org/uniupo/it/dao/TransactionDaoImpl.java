package org.uniupo.it.dao;

import org.uniupo.it.model.Transaction;

import java.sql.*;

public class TransactionDaoImpl implements TransactionDao {

    private String instituteId;
    private String machineId;
    public TransactionDaoImpl(String instituteId,String machineId) {
        this.instituteId = instituteId;
        this.machineId = machineId;
    }
    @Override
    public Transaction registerTransaction(Transaction transaction) {
        Transaction createdTransaction = null;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQLQueries.Transaction.insertTransaction(instituteId,machineId))) {
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setString(2, transaction.getDrinkCode());
            stmt.setInt(3, transaction.getSugarQuantity());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                createdTransaction = new Transaction(
                        rs.getInt("transactionId"),
                        rs.getTimestamp("timeStamp"),
                        rs.getString("drinkCode"),
                        rs.getInt("sugarQuantity")
                );
            }

        } catch (SQLException e) {
            System.out.println("Error registering transaction" + e.getMessage());
            throw new RuntimeException(e);
        }

        return createdTransaction;
    }

    @Override
    public double getCurrentCredit() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQLQueries.Transaction.getCurrentCredit(instituteId,machineId))) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("totalCredit");
            }
            return 0.0;
        } catch (SQLException e) {
            System.out.println("Error retrieving current credit"+e.getMessage());
            throw new RuntimeException("Error retrieving current credit", e);
        }
    }
}
