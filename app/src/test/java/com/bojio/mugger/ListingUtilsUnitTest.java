package com.bojio.mugger;

import com.bojio.mugger.listings.ListingUtils;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ListingUtilsUnitTest {
  @Test
  public void testGetDayTimestamp() {
    // ~10pm
    assertEquals(1531238400000L, ListingUtils.getDayTimestamp(1531288508000L));
    // 11:59:59:999
    assertEquals(1531152000000L, ListingUtils.getDayTimestamp(1531238400000L - 1));
    // 12am
    assertEquals(1531238400000L, ListingUtils.getDayTimestamp(1531238400000L));
    // 00:00:00:001
    assertEquals(1531238400000L, ListingUtils.getDayTimestamp(1531238400001L));
  }

  @Test
  public void testIsSameDate() {
    Assert.assertTrue(ListingUtils.isSameDate(1, 0));
    // Will fail if timezone is set to GMT (default)
    Assert.assertTrue(ListingUtils.isSameDate(1531180800000L, 1531180800000L - 1));
    // Check if timezone is set to GMT +8
    Assert.assertFalse(ListingUtils.isSameDate(1531152000000L, 1531152000000L - 1));
    Assert.assertFalse(ListingUtils.isSameDate(0, Long.MAX_VALUE));
  }

  @Test
  public void testDaysApart() {
    Assert.assertEquals(0, ListingUtils.daysApart(1, 0));
    Assert.assertEquals(-106751991166L, ListingUtils.daysApart(0, Long.MAX_VALUE));
    Assert.assertEquals(106751991166L, ListingUtils.daysApart(Long.MAX_VALUE, 0));
    // Check if timezone is set to GMT +8. 23:59:59:999 to 00:00:00:000 the next day
    Assert.assertEquals(1, ListingUtils.daysApart(1531152000000L, 1531152000000L - 1));
  }

  @Test
  public void testIsBetween() {
    Assert.assertFalse(ListingUtils.isBetween(0,0,1));
    Assert.assertFalse(ListingUtils.isBetween(0, 0, 2));
    Assert.assertFalse(ListingUtils.isBetween(2, 0, 2));
    Assert.assertFalse(ListingUtils.isBetween(3, 0, 2));
    Assert.assertTrue(ListingUtils.isBetween(1, 0, 2));
  }
}