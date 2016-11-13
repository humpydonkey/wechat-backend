package data.db;

import static org.mongojack.internal.util.SerializationUtils.serializeFields;

import lombok.extern.slf4j.Slf4j;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoCommandException;

import data.model.User;

@Slf4j
public class MongoIndexCreator {

  private final DB db;

  public MongoIndexCreator(DB db) {
    this.db = db;
  }

  public void createIndexes() {
    // Person-specific indexes for deduplication / PPM
    createUniqueIndex(User.Dao.COLLECTION_NAME, "wxUser");
    createUniqueIndex(User.Dao.COLLECTION_NAME, "name");
  }

  private static BasicDBObject indexFields(String[] fieldNames) {
    BasicDBObject fields = new BasicDBObject(fieldNames.length);
    for (String field : fieldNames) {
      if (field.startsWith("-")) {
        fields.append(field.substring(1), -1);
      } else {
        fields.append(field, 1);
      }
    }
    return fields;
  }

  private static String indexName(String[] fields) {
    StringBuilder name = new StringBuilder();
    for (int i = 0; i < fields.length; ++i) {
      if (i > 0) {
        name.append('_');
      }
      String field = fields[i];
      if (field.startsWith("-")) {
        name.append(field.substring(1));
        name.append("_-1");
      } else {
        name.append(field);
        name.append("_1");
      }
    }
    return name.toString();
  }

  private static BasicDBObject indexOptions(boolean background, boolean sparse, boolean unique) {
    BasicDBObject options = new BasicDBObject();
    if (background)
      options.append("background", true);
    if (sparse)
      options.append("sparse", true);
    if (unique)
      options.append("unique", true);
    return options;
  }

  private static boolean indexExists(DBCollection collection, String indexName, boolean sparse, boolean unique) {
    for (DBObject indexInfo : collection.getIndexInfo()) {
      if (indexName.equals(indexInfo.get("name"))) {
        boolean indexSparse = indexInfo.containsField("sparse") && (boolean) indexInfo.get("sparse");
        boolean indexUnique = indexInfo.containsField("unique") && (boolean) indexInfo.get("unique");
        if (indexSparse != sparse) {
          log.warn("Index {} on {} is {}sparse but should be {}sparse", indexName, collection.getName(), indexSparse ? "" : "non-", sparse ? "" : "non-");
        }
        if (indexUnique != unique) {
          log.warn("Index {} on {} is {}unique but should be {}unique", indexName, collection.getName(), indexUnique ? "" : "non-", unique ? "" : "non-");
        }
        return true;
      }
    }
    return false;
  }

  private void createIndex(String collectionName, String[] fields, boolean sparse, boolean unique) {
    createIndex(collectionName, fields, sparse, unique, null);
  }

  private void createIndex(String collectionName, String[] fields, boolean sparse, boolean unique, BasicDBObject additionalOptions) {
    long nanos = System.nanoTime();
    final String indexName = indexName(fields);
    try {
      DBCollection collection = db.getCollection(collectionName);
      if (unique) {
        if (indexExists(collection, indexName, sparse, unique)) {
          log.debug("Skipping unique index creation of {} on {} (already exists).", indexName, collectionName);
          return;
        }
        if (collection.findOne() != null) {
          log.error(
              "Cannot create unique index {} on {} collection: TokuMX does not support creating unique indexes in the background, and collection is not empty.",
              indexName, collectionName);
          return;
        }
        // Creating the index in the foreground should be fast because it's an empty collection.
        BasicDBObject options = indexOptions(false, sparse, unique /* = true */);
        if (additionalOptions != null) {
          additionalOptions.forEach(options::append);
        }
        collection.createIndex(indexFields(fields), options);
      } else {
        BasicDBObject options = indexOptions(true, sparse, unique);
        if (additionalOptions != null) {
          additionalOptions.forEach(options::append);
        }
        collection.createIndex(indexFields(fields), options);
      }
    } catch (MongoCommandException e) {
      log.error("Failed to create index {}{}{} on collection {}",
          sparse ? "sparse " : "", unique ? "unique " : "", fields, collectionName, e);
    } finally {
      nanos = System.nanoTime() - nanos;
      log.debug("Created {}{}index on {} in {}ms: {}",
                sparse ? "sparse " : "",
                unique ? "unique " : "",
                collectionName, (nanos+500_000)/1_000_000, indexName);
    }
  }

  @SuppressWarnings("unused")
  private void createIndex(String collectionName, String... fieldNames) {
    createIndex(collectionName, fieldNames, false, false);
  }

  @SuppressWarnings("unused")
  private void createSparseIndex(String collectionName, String... fieldNames) {
    createIndex(collectionName, fieldNames, true, false);
  }

  private void createUniqueIndex(String collectionName, String... fieldNames) {
    createIndex(collectionName, fieldNames, false, true);
  }

  @SuppressWarnings("unused")
  private void createSparseUniqueIndex(String collectionName, String... fieldNames) {
    createIndex(collectionName, fieldNames, true, true);
  }

  @SuppressWarnings("unused")
  // If supplied with more than one date in fieldNames, it will default to minimum date value (will ignore non-dates)
  private void createTTLIndex(String collectionName, int secondsTTL, String... fieldNames) {
    try {
      DBCollection collection = db.getCollection(collectionName);
      BasicDBObject ttlOption = new BasicDBObject();
      ttlOption.append("expireAfterSeconds", secondsTTL);
      collection.createIndex(indexFields(fieldNames), ttlOption);
    } catch (MongoCommandException e) {
      log.error("Failed to create TTL index {} on {}", fieldNames, collectionName, e);
    }
  }

  @SuppressWarnings("unused")
  private void createPartialIndex(String collectionName, boolean unique, BasicDBObject filter, String... fieldNames) {
    BasicDBObject options = new BasicDBObject();
    options.append("partialFilterExpression", serializeFields(ModelSerializer.MONGO_MAPPER, filter));
    createIndex(collectionName, fieldNames, false, unique, options);
  }
}
