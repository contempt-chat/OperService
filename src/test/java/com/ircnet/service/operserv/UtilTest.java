package com.ircnet.service.operserv;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UtilTest {
  @Test
  public void wdhms2secTest1() {
    long result = new Wdhms2sec("1w2d3h4m5s").parse();

    long expected = 0;
    // +1 week
    expected += 604800;
    // +2 days
    expected += 172800;
    // +3 hours
    expected += 10800;
    // +4 minutes
    expected += 240;
    // +5 seconds
    expected += 5;

    assertEquals(expected, result);
  }

  @Test
  public void wdhms2secTest2() {
    long result = new Wdhms2sec("1").parse();
    assertEquals(60, result);
  }

  @Test
  public void wdhms2secTest3() {
    long result = new Wdhms2sec("1m1").parse();
    assertEquals(120, result);
  }

  @Test
  public void wdhms2secTest4() {
    Exception exception = assertThrows(NumberFormatException.class, () -> {
      long result = new Wdhms2sec("9999999999999999999999999999999999999999999999999w").parse();
    });
  }
}
