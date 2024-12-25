package org.uniupo.it.dao;

public final class  SQLQueries {
    private SQLQueries() {
    }

    public static final class Balance {
        public static final String GET_CURRENT_CREDIT =
                "SELECT \"totalCredit\" FROM machine.\"Machine\" LIMIT 1";
    }

    public static final class Drink {
        public static final String GET_DRINK_PRICE =
                "SELECT price FROM machine.\"Drink\" WHERE code = ?";
    }

    public static  final class Transaction{
        public static final String INSERT_TRANSACTION = """
            INSERT INTO machine."Transaction"("timeStamp", "drinkCode", "sugarQuantity")
            VALUES (?, ?, ?)
            RETURNING "transactionId"
            """;
    }
}
