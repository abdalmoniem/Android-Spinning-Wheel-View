package com.hifnawy.spinningWheelLib;

import android.os.CountDownTimer;

public abstract class CustomCountDownTimer extends CountDownTimer {
    private boolean isRunning = false;

    public CustomCountDownTimer(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }

    public void startCountDown() {
        isRunning = true;
        super.start();
    }

    public void stop() {
        isRunning = false;
        onStopped();
        super.cancel();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public abstract void onStopped();
}
