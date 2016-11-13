package data.model;

import java.util.TreeSet;

import javax.inject.Inject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.bson.types.ObjectId;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.Id;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.mongodb.DB;

import data.BaseDao;

@Slf4j
@Data
@NoArgsConstructor
public class User implements Model<ObjectId> {
  @Id @NonNull private ObjectId id;
  @JsonProperty @NonNull private String wxUserId;
  @JsonProperty @NonNull private String name;
  @JsonProperty private TreeSet<ScoredSentence> scoredSentences = new TreeSet<>();

  public User(String wxUserId, String name) {
    this.id = new ObjectId();
    this.wxUserId = wxUserId;
    this.name = name;
  }

  @Data
  @AllArgsConstructor
  @EqualsAndHashCode(of = { "raw" })
  @NoArgsConstructor
  public static class ScoredSentence implements Comparable<ScoredSentence> {
    @JsonProperty @NonNull public String raw;
    @JsonProperty public int score;

    @Override
    public int compareTo(ScoredSentence o) {
      return this.score - o.score;
    }
  }

  public static class Dao extends BaseDao<User, ObjectId> {
    public static final String COLLECTION_NAME = "user";

    @Inject
    public Dao(DB db) {
      super(db.getCollection(COLLECTION_NAME), User.class, ObjectId.class);
    }

    public User findUserByWxUserId(String wxUserId) {
      log.debug("User.Dao::findUserByWxUserId({})", wxUserId);
      DBCursor<User> users = find(DBQuery.is("wxUserId", wxUserId));
      if (!users.hasNext()) {
        return null;
      }
      return users.next();
    }

    public void updateScoredSentences(User user) {
      updateById(user.getId(), ImmutableMap.of("scoredSentences", user.getScoredSentences()));
    }

    public void updateUserName(ObjectId id, String newName) {
//      User user = findUserByWxUserId(wxUserId);
//      if (user == null) {
//        throw new IllegalStateException("Can not find user by wxUserId:" + wxUserId);
//      }
      updateById(id, ImmutableMap.of("name", newName));
    }
  }

}
