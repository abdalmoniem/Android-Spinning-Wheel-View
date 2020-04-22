package com.hifnawy.spinningWheelLib.exceptions;

/**
 * Created by AbdAlMoniem AlHifnawy
 */

public class InvalidWheelSectionDataException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Invalid data in wheel section";

    public InvalidWheelSectionDataException() {
        super(DEFAULT_MESSAGE);
    }
    public InvalidWheelSectionDataException(String message) {
        super(message);
    }
    public InvalidWheelSectionDataException(String message, Throwable cause) {
        super(message, cause);
    }

}
