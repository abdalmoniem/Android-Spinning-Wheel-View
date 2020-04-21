package com.hifnawy.spinningwheellib.exceptions;

import com.hifnawy.spinningwheellib.Constants;

/**
 * Created by AbdAlMoniem AlHifnawy
 */

public class InvalidWheelSectionsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Invalid amount of Wheel Sections. Should be more or equal than " + Constants.MINIMUM_WHEEL_SECTIONS
            + " and less or equal than " + Constants.MAXIMUM_WHEEL_SECTIONS;

    public InvalidWheelSectionsException() {
        super(DEFAULT_MESSAGE);
    }
    public InvalidWheelSectionsException(String message) {
        super(message);
    }
    public InvalidWheelSectionsException(String message, Throwable cause) {
        super(message, cause);
    }

}
