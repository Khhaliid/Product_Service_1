package se.product_service_1.exception;

public class CategoryNotEmptyException extends RuntimeException {

    public CategoryNotEmptyException(String message) {
        super(message);
    }
}
