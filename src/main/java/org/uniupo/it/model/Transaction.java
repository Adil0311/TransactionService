package org.uniupo.it.model;

import java.sql.Timestamp;

public class Transaction {
    private Integer transactionId;
    private Timestamp timestamp;
    private String drinkCode;
    private int sugarQuantity;

    public Transaction(Integer transactionId, Timestamp timestamp, String drinkCode, int sugarQuantity) {
        this.transactionId = transactionId;
        this.timestamp = timestamp;
        this.drinkCode = drinkCode;
        this.sugarQuantity = sugarQuantity;
    }

    public Transaction(String selectedBeverage, int sugarQuantity) {
        this.drinkCode = selectedBeverage;
        this.sugarQuantity = sugarQuantity;
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getDrinkCode() {
        return drinkCode;
    }

    public void setDrinkCode(String drinkCode) {
        this.drinkCode = drinkCode;
    }

    public int getSugarQuantity() {
        return sugarQuantity;
    }

    public void setSugarQuantity(int sugarQuantity) {
        this.sugarQuantity = sugarQuantity;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", timestamp=" + timestamp +
                ", drinkCode='" + drinkCode + '\'' +
                ", sugarQuantity=" + sugarQuantity +
                '}';
    }
}
