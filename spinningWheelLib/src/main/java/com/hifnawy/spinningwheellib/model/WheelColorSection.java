package com.hifnawy.spinningwheellib.model;

import androidx.annotation.ColorRes;

/**
 * Created by AbdAlMoniem AlHifnawy
 */

public class WheelColorSection extends WheelSection {
    private @ColorRes int color;


    public WheelColorSection(@ColorRes int color) {
        this.color = color;
    }



    public SectionType getType() {
        return SectionType.COLOR;
    }

    public @ColorRes int getColor() {
        return color;
    }


    @Override
    public String toString() {
        return
                "SectionType= " +  getType() +
                ", Color= " +      color;
    }
}
