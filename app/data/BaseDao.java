package data;

import java.util.Iterator;
import java.util.List;

import org.mongojack.DBCursor;
import org.mongojack.DBProjection;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate.Builder;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import data.model.Model;

public class BaseDao<T extends Model<K>, K> {
  private static final String DB_NAME = "wechat-backend";
  private static final MongoClient mongoClient = new MongoClient("localhost", 27017);

  @SuppressWarnings("deprecated") private static final DB db = mongoClient.getDB(DB_NAME);

  protected final JacksonDBCollection<T, K> coll;

  public BaseDao(String collectionName, Class<T> classType, Class<K> keyType) {
    coll = JacksonDBCollection.wrap(db.getCollection(collectionName), classType, keyType);
  }

  public T findOne() {
    Iterator<T> iter = coll.find().iterator();
    return iter.hasNext() ? iter.next() : null;
  }

  public DBCursor<T> find(Query query) {
    return coll.find(query);
  }

  public DBCursor<T> find(Query query, String[] requiredFields) {
    return coll.find(DBProjection.include(requiredFields));
  }

  public T findById(K id) {
    return coll.findOneById(id);
  }

  public long count() {
    return coll.count();
  }

  public long count(Query query) {
    return coll.getCount(query);
  }

  public long count(Query query, DBObject fields) {
    return coll.getCount(query, fields);
  }

  public WriteResult<T, K> insert(List<T> list) {
    return coll.insert(list);
  }

  public WriteResult<T, K> insert(T t) {
    return coll.insert(t);
  }

  public WriteResult<T, K> save(T t) {
    return coll.save(t);
  }

  public WriteResult<T, K> update(Query query, T object) {
    return coll.update(query, object);
  }

  public WriteResult<T, K> update(K id, T object) {
    return coll.updateById(id, object);
  }

  public WriteResult<T, K> update(K id, Builder update) {
    return coll.updateById(id, update);
  }

  public WriteResult<T, K> updateMulti(Query query, Builder update) {
    return coll.updateMulti(query, update);
  }

  public WriteResult<T, K> removeById(K id) {
    return coll.removeById(id);
  }

  public WriteResult<T, K> remove(Query query) {
    return coll.remove(query);
  }
}
