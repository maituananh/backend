package com.spring.backend.utils;

import java.util.UUID;

public class FileUtils {
  public static String createNewName(String fileName) {
    String uuid = UUID.randomUUID().toString();
    return uuid + "-" + fileName;
  }
}
