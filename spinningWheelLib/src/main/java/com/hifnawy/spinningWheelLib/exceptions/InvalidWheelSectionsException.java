package com.hifnawy.spinningWheelLib.exceptions;

/**
 * Created by AbdAlMoniem AlHifnawy
 */

public class InvalidWheelSectionsException extends RuntimeException {
    public InvalidWheelSectionsException(String message) {
        super(message);
    }

    public InvalidWheelSectionsException(String message, Throwable cause) {
        super(message, cause);
    }

}
