package controllers.auth;

import org.junit.Test;

import static org.junit.Assert.*;

public class AuthControllerTest {

  @Test
  public void testVerifySignature1() throws Exception {
    String signature = "633b8ad98c7bad23f8a898db36fee7550e0fbddc"; // token = "happybirthday";
    String timestamp = "1477709868";
    String nonce = "517143108";

    assertTrue(AuthController.checkSignature(signature, timestamp, nonce));
  }

  @Test
  public void testVerifySignature2() throws Exception {
    String signature = "55c6a3e79bb27088522daaeacfe6612fd15bf2fa"; // token = "happybirthday";
    String timestamp = "1477712403";
    String nonce = "1549277144";

    assertTrue(AuthController.checkSignature(signature, timestamp, nonce));
  }
}
