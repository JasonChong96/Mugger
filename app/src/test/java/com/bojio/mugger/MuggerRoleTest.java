package com.bojio.mugger;

import com.bojio.mugger.authentication.MuggerRole;

import org.junit.Assert;
import org.junit.Test;

public class MuggerRoleTest {
  @Test
  public void testCheck() {
    Assert.assertTrue(MuggerRole.USER.check(MuggerRole.USER));
    Assert.assertFalse(MuggerRole.MODERATOR.check(MuggerRole.USER));
    Assert.assertTrue(MuggerRole.MODERATOR.check(MuggerRole.ADMIN));
  }

  @Test
  public void testCheckSuperiorityTo() {
    Assert.assertFalse(MuggerRole.ADMIN.checkSuperiorityTo(MuggerRole.ADMIN));
    Assert.assertTrue(MuggerRole.MASTER.checkSuperiorityTo(MuggerRole.MODERATOR));
  }
}
