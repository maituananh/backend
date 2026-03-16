package com.spring.backend.service;

public interface AIClient {

  String classify(String message);

  String chat(String systemPrompt, String message);

  String chat(String systemPrompt, String message, String resource);
}
