package midtier.message;

import java.util.List;

import javax.inject.Inject;

import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.bean.WxMpXmlMessage;
import midtier.Scorer;
import data.model.GeneratedName;
import data.model.ScoreBoard;
import data.model.User;

/**
 * Decide how to process the message
 */
public class MsgProcessor {

  @Inject private User.Dao userDao;
  @Inject private GeneratedName.Dao nameDao;
  @Inject private ScoreBoard scoreBoard;

  public void processMsgContent(WxMpXmlMessage message) {
    String content = getContent(message);
    String wxUserId = message.getFromUser();
    if (content.startsWith(MsgConstants.REQUEST_NAME)) {
      String returnContent = setNewUserName(wxUserId, message.getContent());
      message.setContent(returnContent);
      return;
    } else if (content.startsWith(MsgConstants.REQUEST_GAME_RULE)) {
      String returnContent = MsgConstants.RESPONSE_GAME_RULE;
      message.setContent(returnContent);
      return;
    } else if (content.startsWith(MsgConstants.REQUEST_RANKINGS)) {
      String returnContent = getLatestScoreBoardMsgString();
      message.setContent(returnContent);
      return;
    }

    // otherwise, assume it's a blessing sentence
    User user = userDao.findUserByWxUserId(wxUserId);
    if (user == null) {
      user = createUserWithGeneratedName(wxUserId);
    }

    User.ScoredSentence scoredSentence = addRawSentenceToUser(user, content);

    String returnContent = "Hi " + user.getName() + ", "
        + String.format(MsgConstants.RESPONSE_MSG_SCORED, content, scoredSentence.getScore())
        + "\n\n" + MsgConstants.RESPONSE_GAME_RULE;
    message.setContent(returnContent);
  }

  public User.ScoredSentence addRawSentenceToUser(User user, String raw) {
    int score = Scorer.score(raw);
    User.ScoredSentence scoredSentence = new User.ScoredSentence(raw, score);
    if (!user.getScoredSentences().add(scoredSentence)) {
      return scoredSentence;
    }

    userDao.updateScoredSentences(user);
    return scoredSentence;
  }

  public String getLatestScoreBoardMsgString() {
    List<ScoreBoard.UserScoredSentence> scoreRecords = scoreBoard.getTopN(MsgConstants.TOP_N);
    return ScoreBoard.toUserMsgString(scoreRecords);
  }

  private User createUserWithGeneratedName(String wxUserId) {
    String name = nameDao.getAvailableNames().poll();
    User user = new User(wxUserId, name);
    userDao.insert(user);
    return user;
  }

  /**
   * Set a name for a user, create one if it doesn't exist
   *
   * @return return message for end user
   */
  private String setNewUserName(String wxUserId, String setUserNamePair) {
    String newName = extractName(setUserNamePair);
    if (newName == null) {
      return MsgConstants.RESPONSE_ERROR_ILLEGAL_FORMAT;
    }
    User user = userDao.findUserByWxUserId(wxUserId);
    if (user == null) {
      user = new User(wxUserId, newName);
      userDao.insert(user);
    } else {
      userDao.updateUserName(user.getId(), newName);
    }
    return "Hi " + newName + ", " + MsgConstants.RESPONSE_SET_NAME_SUCCESSFUL;
  }

  private String extractName(String userNameKeyValuePair) {
    // 姓名:XXX or 姓名 XXX
    int splitIdx = userNameKeyValuePair.indexOf('：');
    if (splitIdx < 0) {
      splitIdx = userNameKeyValuePair.indexOf(' ');
    }
    if (splitIdx < 0) {
      return null;
    }
    return userNameKeyValuePair.substring(splitIdx + 1);
  }

  private String getContent(WxMpXmlMessage message) {
    String type = message.getMsgType();
    switch (type) {
    case WxConsts.XML_MSG_TEXT:
      return message.getContent();
    case WxConsts.XML_MSG_VOICE:
      return message.getRecognition();
    default:
      throw new RuntimeException("Unknown messageType: " + type);
    }
  }
}
