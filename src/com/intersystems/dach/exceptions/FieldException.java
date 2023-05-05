package com.intersystems.dach.exceptions;

public class FieldException extends Exception {

    public FieldException(String fieldName, String message) {
        super("Field " + fieldName + ": " + message);
    }

    public FieldException() {
        super();
    }

}
