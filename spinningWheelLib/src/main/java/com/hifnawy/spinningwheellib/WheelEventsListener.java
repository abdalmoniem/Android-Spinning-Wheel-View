package com.hifnawy.spinningwheellib;

/**
 * Created by AbdAlMoniem AlHifnawy
 */

public interface WheelEventsListener {
    void onWheelStopped();
    void onWheelFlung();
    void onWheelSectionChanged(int sectionIndex, double angle);
    void onWheelSettled(int sectionIndex, double angle);
}
