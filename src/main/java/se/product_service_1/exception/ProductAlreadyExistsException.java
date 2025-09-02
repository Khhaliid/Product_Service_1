package se.product_service_1.exception;

public class ProductAlreadyExistsException extends RuntimeException {
  public ProductAlreadyExistsException(String message) {
    super(message);
  }
}
