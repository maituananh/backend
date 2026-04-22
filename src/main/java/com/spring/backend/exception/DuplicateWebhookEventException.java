package com.spring.backend.exception;

public class DuplicateWebhookEventException extends RuntimeException {
  public DuplicateWebhookEventException(String eventId) {
    super("Duplicate Stripe event: " + eventId);
  }
}
