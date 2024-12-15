package org.uniupo.it.model;

public class Selection {
    private String drinkCode;
    private int sugarLevel;

    public Selection(String drinkCode, int sugarLevel) {
        this.drinkCode = drinkCode;
        this.sugarLevel = sugarLevel;
    }

    public String getDrinkCode() {
        return drinkCode;
    }

    public int getSugarLevel() {
        return sugarLevel;
    }

    public void setDrinkCode(String drinkCode) {
        this.drinkCode = drinkCode;
    }

    public void setSugarLevel(int sugarLevel) {
        this.sugarLevel = sugarLevel;
    }

    public String toString() {
        return "Selection{" +
                "drinkCode='" + drinkCode + '\'' +
                ", sugarLevel=" + sugarLevel +
                '}';
    }
}
