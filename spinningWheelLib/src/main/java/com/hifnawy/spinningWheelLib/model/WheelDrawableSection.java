package com.hifnawy.spinningWheelLib.model;

import androidx.annotation.DrawableRes;

/**
 * Created by AbdAlMoniem AlHifnawy
 */

public class WheelDrawableSection extends WheelSection {

    private @DrawableRes int drawable;


    public WheelDrawableSection(@DrawableRes int drawable) {
        this.drawable = drawable;
    }



    public SectionType getType() {
        return SectionType.DRAWABLE;
    }


    public @DrawableRes int getDrawableRes() {
        return drawable;
    }


    @Override
    public String toString() {
        return
                "SectionType= " +   getType() +
                        ", DrawableRes= " + drawable;
    }
}
