package org.uniupo.it.model;

import java.sql.Timestamp;

public class TransactionMessage {
    String machineId;
    String instituteId;
    String drinkCode;
    int sugarLevel;
    int transactionId;
    Timestamp timestamp;

    public TransactionMessage(String machineId, String instituteId, String drinkCode, int sugarLevel, Integer transactionId, Timestamp timestamp) {
        this.machineId = machineId;
        this.instituteId = instituteId;
        this.drinkCode = drinkCode;
        this.sugarLevel = sugarLevel;
        this.transactionId = transactionId;
        this.timestamp = timestamp;
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public String getInstituteId() {
        return instituteId;
    }

    public void setInstituteId(String instituteId) {
        this.instituteId = instituteId;
    }

    public String getDrinkCode() {
        return drinkCode;
    }

    public void setDrinkCode(String drinkCode) {
        this.drinkCode = drinkCode;
    }

    public int getSugarLevel() {
        return sugarLevel;
    }

    public void setSugarLevel(int sugarLevel) {
        this.sugarLevel = sugarLevel;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "TransactionMessage{" +
                "machineId='" + machineId + '\'' +
                ", instituteId='" + instituteId + '\'' +
                ", drinkCode='" + drinkCode + '\'' +
                ", sugarLevel='" + sugarLevel + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
