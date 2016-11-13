package controllers.message;

import javax.inject.Inject;

import midtier.message.MsgProcessor;
import play.mvc.Controller;
import play.mvc.Result;

public class RankingController extends Controller {

  @Inject MsgProcessor msgProcessor;

  public Result rankings() {

    return ok(msgProcessor.getLatestScoreBoardMsgString());
  }

}
