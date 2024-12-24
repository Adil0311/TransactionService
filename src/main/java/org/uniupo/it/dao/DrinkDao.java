package org.uniupo.it.dao;

public interface DrinkDao {
    double getPriceByCode(String drinkCode);
    double getCurrentCredit();
}