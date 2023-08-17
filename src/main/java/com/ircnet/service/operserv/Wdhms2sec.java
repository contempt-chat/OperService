package com.ircnet.service.operserv;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts strings like 1w2d3h4m5s into seconds.
 */
public class Wdhms2sec {
  // If unit is not set, use minutes (like ircd)
  final static char DEFAULT_MULTIPLIER = 'm';

  /**
   * A string like 1w2d3h4m5s.
   */
  private String input;

  /**
   * Current number.
   */
  private StringBuilder currentNumber;

  /**
   * Current unit.
   */
  private char currentUnit;

  /**
   * Seconds.
   */
  private long output;

  /**
   * Maps a unit to seconds (e.g. for minutes: 'm' to 60).
   */
  private Map<Character, Long> unitToSecondsMap;

  public Wdhms2sec(String input) {
    this.input = input;
    this.currentNumber = new StringBuilder();

    this.unitToSecondsMap = new HashMap<>();
    unitToSecondsMap.put('w', 604800L);
    unitToSecondsMap.put('d', 86400L);
    unitToSecondsMap.put('h', 3600L);
    unitToSecondsMap.put('m', 60L);
    unitToSecondsMap.put('s', 1L);
  }

  public long parse() {
    if (StringUtils.isBlank(input)) {
      throw new IllegalArgumentException("input must not be blank");
    }

    String regex = "^[\\d" + StringUtils.join(unitToSecondsMap.keySet()) + "]+$";

    if (!input.matches(regex)) {
      throw new IllegalArgumentException("input has wrong format");
    }

    for (char c : input.toCharArray()) {
      if(Character.isAlphabetic(c)) {
        currentUnit = c;
        addSeconds();
      }
      else {
        currentNumber.append(c);
      }
    }

    if(currentNumber.length() > 0) {
      addSeconds();
    }

    return output;
  }

  private void addSeconds() {
    if (currentNumber.length() == 0) {
      // Got something like 1dw
      return;
    }

    long number = Long.parseLong(currentNumber.toString());
    output += number *  unitToSecondsMap.get(currentUnit != '\u0000' ? currentUnit : DEFAULT_MULTIPLIER) ;
    currentNumber = new StringBuilder();
  }
}
