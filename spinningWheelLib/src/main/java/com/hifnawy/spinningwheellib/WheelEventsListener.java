package com.hifnawy.spinningwheellib;

/**
 * Created by abicelis on 26/7/2017.
 */

public interface WheelEventsListener {
    void onWheelStopped();
    void onWheelFlung();
    void onWheelSectionChanged(int sectionIndex, double angle);
    void onWheelSettled(int sectionIndex, double angle);
}
