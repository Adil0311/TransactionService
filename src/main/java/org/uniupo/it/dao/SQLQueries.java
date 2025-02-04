package org.uniupo.it.dao;

public final class SQLQueries {
    private SQLQueries() {
    }

    public static String getSchemaName(String instituteId, String machineId) {
        return String.format("machine_%s_%s",
                instituteId.toLowerCase().replace("-", "_"),
                machineId.toLowerCase().replace("-", "_"));
    }

    public static final class Balance {
        public static final String GET_CURRENT_CREDIT =
                "SELECT \"totalCredit\" FROM machine.\"Machine\" LIMIT 1";
    }

    public static final class Drink {
        private static final String GET_DRINK_PRICE =
                "SELECT price FROM %s.\"Drink\" WHERE code = ?";
        public static String getGetDrinkPrice(String instituteId, String machineId) {
            return String.format(String.format(GET_DRINK_PRICE, getSchemaName(instituteId, machineId)));
        }

    }

    public static final class Transaction {
        private static final String INSERT_TRANSACTION = """
                INSERT INTO %s."Transaction"("timeStamp", "drinkCode", "sugarQuantity")
                VALUES (?, ?, ?)
                RETURNING *
                """;
        private static final String GET_CURRENT_CREDIT =
                "SELECT \"totalCredit\" FROM %s.\"Machine\" LIMIT 1";

        public static String insertTransaction(String instituteId, String machineId) {
            return String.format(INSERT_TRANSACTION, getSchemaName(instituteId, machineId));
        }

        public static String getCurrentCredit(String instituteId, String machineId) {
            return String.format(GET_CURRENT_CREDIT, getSchemaName(instituteId, machineId));
        }


    }
}
