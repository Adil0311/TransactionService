package org.uniupo.it.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DrinkDaoImpl implements DrinkDao {
    @Override
    public double getPriceByCode(String drinkCode) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQLQueries.Drink.GET_DRINK_PRICE)) {

            stmt.setString(1, drinkCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("price");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving drink price", e);
        }
        return 0.0;
    }


}
