package controllers.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import com.google.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import play.mvc.Controller;
import play.mvc.Result;

@Slf4j
public class AuthController extends Controller {

  private final static char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
  private final static String TOKEN = "happybirthday";
  private final static MessageDigest MD;

  @Inject private WxMpService service;

  static {
    try {
      MD = MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Sort Token, timestamp and nonce, then concatenate together, encrypt it by SHA-1 and compare it with signature
   */
  public static boolean checkSignature(@NonNull String signature, @NonNull String timestamp, @NonNull String nonce) {
    String[] arr = new String[] { TOKEN, timestamp, nonce };
    Arrays.sort(arr);
    log.debug("After sorted: " + Arrays.toString(arr));
    StringBuilder content = new StringBuilder();
    for (int i = 0; i < arr.length; i++) {
      content.append(arr[i]);
    }

    byte[] digest = MD.digest(content.toString().getBytes());
    StringBuilder signatureBuilder = new StringBuilder();
    for (int i = 0; i < digest.length; i++) {
      signatureBuilder.append(byteToHexStr(digest[i]));
    }

    String generatedSignature = signatureBuilder.toString();
    boolean res = generatedSignature.equals(signature.toUpperCase());
    if (!res) {
      log.error("signature {} doesn't match generated {} based on {} {} {}", signature, generatedSignature, TOKEN, timestamp, nonce);
    }
    return res;
  }

  private static String byteToHexStr(byte mByte) {
    char[] tempArr = new char[2];
    tempArr[0] = DIGITS[(mByte >>> 4) & 0X0F];
    tempArr[1] = DIGITS[mByte & 0X0F];
    return new String(tempArr);
  }

  public Result verifySignature(String signature, String timestamp, String nonce, String echostr) {
    log.info("Signature: {}, Timestamp: {}, Nonce: {}, Echostr: {}", signature, timestamp, nonce, echostr);
    if (signature == null || timestamp == null || nonce == null || echostr == null) {
      log.error("Can't verify signature because some arguments are null: {}, {}, {}, {}", signature, timestamp, nonce, echostr);
      return badRequest("There is a null argument!");
    }

    if (!checkSignature(signature, timestamp, nonce)) {
      return unauthorized("checkSignature() failed!");
    }
    return ok(echostr);
  }

  public Result getWechatCallBackIp() throws WxErrorException {
    return ok(Arrays.toString(service.getCallbackIP()));
  }

  public Result getAccessToken() {
    String accessToken;
    try {
      accessToken = service.getAccessToken();
    } catch (WxErrorException e) {
      log.error("Failed to get AccessToken: {}", e);
      return internalServerError(e.toString());
    }
    return ok(accessToken);
  }

}
