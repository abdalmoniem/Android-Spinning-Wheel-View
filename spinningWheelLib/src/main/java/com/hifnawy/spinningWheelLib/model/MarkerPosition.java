package com.hifnawy.spinningWheelLib.model;

/**
 * Created by AbdAlMoniem AlHifnawy
 */

public enum MarkerPosition {
    TOP(0),
    TOP_RIGHT(45),
    RIGHT(90),
    BOTTOM_RIGHT(135),
    BOTTOM(180),
    BOTTOM_LEFT(225),
    LEFT(270),
    TOP_LEFT(315);

    int degreeOffset;

    MarkerPosition(int degreeOffset) {
        this.degreeOffset = degreeOffset;
    }


    public int getDegreeOffset() {
        return degreeOffset;
    }
}
