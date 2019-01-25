package io.github.grantchan.ssh.common.transport.handler;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RequestHandlerTest {

  @Test
  public void testNegotiateNormalCase() {
    assertEquals("c", new RequestHandler(null).negotiate("a,b,c", "e,c"));
  }

  @Test
  public void testNegotiateWhenNothingInCommon() {
    assertNull(new RequestHandler(null).negotiate("a,b,d", "e,c"));
  }

}