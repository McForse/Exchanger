package com.shotball.project.models;

public enum Categories {

    All(0),
    Clothes(1),
    Electronics(2),
    Furniture(3),
    Household_products(4),
    Tools(5),
    Сhildens_goods(6),
    Pet_supplies(7),
    Food(8),
    Sporting_goods(9),
    Сosmetics(10),
    Others(11);

    private final int value;
    private Categories(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static int getCategoriesCount() {
        return Others.value;
    }

}
