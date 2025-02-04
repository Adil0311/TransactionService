package org.uniupo.it.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DrinkDaoImpl implements DrinkDao {
    private final String instituteId;
    private final String machineId;
    public DrinkDaoImpl(String instituteId,String machineId) {
        this.instituteId = instituteId;
        this.machineId = machineId;
    }
    @Override
    public double getPriceByCode(String drinkCode) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQLQueries.Drink.getGetDrinkPrice(instituteId,machineId))) {

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
