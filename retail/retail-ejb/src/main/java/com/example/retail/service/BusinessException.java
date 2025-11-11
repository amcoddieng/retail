package com.example.retail.service;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}