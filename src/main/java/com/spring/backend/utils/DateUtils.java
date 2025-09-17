package com.spring.backend.utils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateUtils {

  public static Date getTime(int time) {
    long nowMillis = System.currentTimeMillis();
    long expMillis = nowMillis + TimeUnit.MINUTES.toMillis(time);
    return new Date(expMillis);
  }
}
