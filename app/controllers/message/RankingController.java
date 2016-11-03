package controllers.message;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.rankings;

public class RankingController extends Controller {

  public Result rankings() {
    List<UserScore> userScoreList = new ArrayList<>();
    userScoreList.add(new UserScore("User1", 240));
    userScoreList.add(new UserScore("User2", 193));
    userScoreList.add(new UserScore("User3", 171));
    userScoreList.add(new UserScore("User4", 170));
    userScoreList.add(new UserScore("User5", 99));
    return ok(rankings.render(userScoreList));
  }

  @AllArgsConstructor
  public static class UserScore {
    public String username;
    public int score;
  }
}
