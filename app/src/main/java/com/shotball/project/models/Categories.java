package com.shotball.project.models;

import com.shotball.project.R;

public enum Categories {

    All(0, R.string.category_all),
    Clothes(1, R.string.category_clothes),
    Electronics(2, R.string.category_electronics),
    Furniture(3, R.string.category_furniture),
    Household_products(4, R.string.category_household),
    Tools(5, R.string.category_tools),
    Сhildens_goods(6, R.string.category_children),
    Pet_supplies(7, R.string.category_pet),
    Food(8, R.string.category_food),
    Sporting_goods(9, R.string.category_sport),
    Сosmetics(10, R.string.category_cosmetics),
    Others(11, R.string.category_others);

    private final int value;
    private final int name;

    private Categories(int value, int name) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public int getName() {
        return name;
    }

    public static int getCategoriesCount() {
        return Others.value;
    }

    public static int getNameByValue(int value) {
        for (Categories item : Categories.values()) {
            if (item.value == value) {
                return item.name;
            }
        }

        return 0;
    }

}
