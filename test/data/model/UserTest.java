package data.model;

import org.bson.types.ObjectId;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserTest {

  @Test
  public void testScoresOrder() {
    User u = new User("testWxId", "testUserName");
    u.getScoredSentences().add(new User.ScoredSentence("statement1", 11));
    u.getScoredSentences().add(new User.ScoredSentence("statement2", 1));
    u.getScoredSentences().add(new User.ScoredSentence("statement2", 5));

    assertEquals(11, u.getScoredSentences().pollLast().getScore());
    assertEquals(5, u.getScoredSentences().pollLast().getScore());
    assertEquals(1, u.getScoredSentences().pollLast().getScore());
  }
}
