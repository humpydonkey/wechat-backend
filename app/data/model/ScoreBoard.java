package data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.mongojack.DBCursor;

@Data
@Slf4j
public class ScoreBoard {

  @Inject User.Dao userDao;

  public static String toUserMsgString(List<UserScoredSentence> board) {
    StringJoiner joiner = new StringJoiner("\n");
    IntStream.range(0, board.size()).forEach(i -> joiner.add((i + 1) + "." + board.get(i).toMsgString()));
    return joiner.toString();
  }

  public HashSet<UserScoredSentence> getAllRankings() {
    HashSet<UserScoredSentence> rankings = new HashSet<>();
    DBCursor<User> users = userDao.find();
    StreamSupport.stream(users.spliterator(), false)
        .flatMap(user -> user.getScoredSentences().stream().map(sentence -> new UserScoredSentence(user, sentence)))
        .forEach(rankings::add);

    log.info("Find {} ScoreRecords", rankings.size());
    return rankings;
  }

  /**
   * From the top to n-th
   */
  public List<UserScoredSentence> getTopN(int n) {
    HashSet<UserScoredSentence> allRankings = getAllRankings();
    List<UserScoredSentence> sortedRankings = new ArrayList<>(allRankings);
    Collections.sort(sortedRankings);     // ascending
    Collections.reverse(sortedRankings);  // descending
    n = Math.min(sortedRankings.size(), n);
    return sortedRankings.subList(0, n);
  }

  @Data
  @EqualsAndHashCode(of = { "scoredSentence" })
  public static class UserScoredSentence implements Comparable<UserScoredSentence> {
    @NonNull public User user;
    @NonNull public User.ScoredSentence scoredSentence;

    @Override
    public int compareTo(@NonNull UserScoredSentence o) {
      return this.scoredSentence.getScore() - o.getScoredSentence().getScore();
    }

    public String toMsgString() {
      return user.getName() + " - " + scoredSentence.getRaw() + " - " + scoredSentence.getScore() + "分";
    }
  }

}
