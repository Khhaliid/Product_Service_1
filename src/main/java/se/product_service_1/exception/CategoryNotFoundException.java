package se.product_service_1.exception;

public class CategoryNotFoundException extends RuntimeException{

    public CategoryNotFoundException(String message) {
        super(message);
    }
}
