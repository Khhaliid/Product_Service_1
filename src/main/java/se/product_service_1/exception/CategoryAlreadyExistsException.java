package se.product_service_1.exception;

public class CategoryAlreadyExistsException extends RuntimeException {
    public CategoryAlreadyExistsException(String message)
    {
        super(message);
    }
}
