package com.hifnawy.spinningwheellib.model;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;


/**
 * Created by abicelis on 26/7/2017.
 */

public class WheelBitmapSection extends WheelSection {

    private Bitmap bitmap;


    public WheelBitmapSection(@NonNull Bitmap bitmap) {
        this.bitmap = bitmap;
    }


    public SectionType getType() {
        return SectionType.BITMAP;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    @Override
    public String toString() {
        return
                "SectionType= " +   getType() +
                ", Bitmap= " +      (bitmap != null ? bitmap.toString() : "NULL");

    }
}
