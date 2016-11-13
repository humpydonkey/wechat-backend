package controllers.message;

import java.io.StringWriter;
import java.util.List;

import javax.inject.Inject;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.bean.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.WxMpXmlOutMessage;
import midtier.message.MsgProcessor;

import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.Word;
import org.w3c.dom.Document;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import data.model.ScoreBoard;
import data.model.User;

@Slf4j
public class MessageController extends Controller {

//  @Inject private WxMpService service;
  @Inject private WxMpMessageRouter messageRouter;
  @Inject private MsgProcessor msgProcessor;
  @Inject private User.Dao useDao;

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

    msgProcessor.processMsgContent(message);
    log.info("Message content after processed: {}", message.getContent());

    WxMpXmlOutMessage outMessage = messageRouter.route(message);

    String result = outMessage.toXml();
    log.info("Replying: \n{}", result);
    return ok(result);
  }

  public Result addSentence(@NonNull String wxUserId, @NonNull String userName, @NonNull String sentence) {
    User user = useDao.findUserByWxUserId(wxUserId);
    if (user == null) {
      user = new User(wxUserId, userName);
      useDao.insert(user);
    }
    User.ScoredSentence scoredSentence = msgProcessor.addRawSentenceToUser(user, sentence);
    return ok(new ScoreBoard.UserScoredSentence(user, scoredSentence).toMsgString());
  }

  public Result segment(String raw) {
    if (raw == null) {
      return ok("No statement provided");
    }
    List<Word> words = WordSegmenter.seg(raw);
    return ok(words.toString());
  }
}
