package com.spring.backend.utils;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtils {

  public static Date getTime(int time) {
    long nowMillis = System.currentTimeMillis();
    long expMillis = nowMillis + TimeUnit.MINUTES.toMillis(time);
    return new Date(expMillis);
  }

  public static int calculateAge(String ddMMYYYY) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    LocalDate birthDate = LocalDate.parse(ddMMYYYY, formatter);
    return Period.between(birthDate, LocalDate.now()).getYears();
  }
}
