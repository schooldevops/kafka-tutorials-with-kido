package com.schooldevops.kafkatutorials.entities;


public class RetryTestException extends RuntimeException {

    public RetryTestException(String msg) {
        super(msg);
    }
}
