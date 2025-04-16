package ru.askar.common.exception;

public class UserRejectedToFillFieldsException extends Exception {
    public UserRejectedToFillFieldsException() {
        super("Пользователь отказался заполнять поля");
    }
}
