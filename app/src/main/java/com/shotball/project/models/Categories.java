package com.shotball.project.models;

import android.content.Context;

import com.shotball.project.MyApplication;
import com.shotball.project.R;

import java.util.ArrayList;

public enum Categories {

    All(0, R.string.category_all),
    Clothes(1, R.string.category_clothes),
    Electronics(2, R.string.category_electronics),
    Furniture(3, R.string.category_furniture),
    Household_products(4, R.string.category_household),
    Tools(5, R.string.category_tools),
    Children_goods(6, R.string.category_children),
    Pet_supplies(7, R.string.category_pet),
    Food(8, R.string.category_food),
    Sporting_goods(9, R.string.category_sport),
    Cosmetics(10, R.string.category_cosmetics),
    Others(11, R.string.category_others);

    private final int value;
    private final int name;

    Categories(int value, int name) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public int getName() {
        return name;
    }

    public static int getCount() {
        return Categories.values().length;
    }

    public static int getNameByValue(int value) {
        if (value < getCount() - 1) {
            return Categories.values()[value].name;
        }
        return 0;
    }

    public static String[] getStringArray() {
        ArrayList<String> res = new ArrayList<>();
        Context ctx = MyApplication.getAppContext();

        for (Categories category : Categories.values()) {
            res.add(ctx.getString(category.getName()));
        }

        return res.toArray(new String[getCount()]);
    }

}
