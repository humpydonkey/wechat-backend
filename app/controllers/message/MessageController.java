package controllers.message;

import java.io.StringWriter;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.WxMpXmlOutMessage;

import org.w3c.dom.Document;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import com.google.inject.Inject;

@Slf4j
public class MessageController extends Controller {

  @Inject private WxMpService service;
  @Inject private WxMpMessageRouter messageRouter;

  @SneakyThrows(TransformerException.class)
  private static String toString(Document doc) {
    DOMSource domSource = new DOMSource(doc);
    StringWriter writer = new StringWriter();
    StreamResult result = new StreamResult(writer);
    TransformerFactory.newInstance().newTransformer().transform(domSource, result);
    return writer.toString();
  }

  @BodyParser.Of(BodyParser.Xml.class)
  public Result create() {
    Document dom = request().body().asXml();
    log.info("Received xml: \n{}", toString(dom));

    WxMpXmlMessage message = WxMpXmlMessage.fromXml(toString(dom));
    log.debug("Received msg: {}", message);

    WxMpXmlOutMessage outMessage = WxMpXmlOutMessage.TEXT().fromUser(message.getToUser()).toUser(message.getFromUser()).content("我收到了你的消息: " + message.getContent()).build();
    String result = outMessage.toXml();
    log.info("Replying: \n{}", result);
    return ok(result);
  }

}
