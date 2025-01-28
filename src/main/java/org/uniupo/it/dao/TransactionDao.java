package org.uniupo.it.dao;

import org.uniupo.it.model.Transaction;

public interface TransactionDao {
    void registerTransaction(Transaction transaction);
    double getCurrentCredit();
}
