package modules;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;

import data.db.DBConfig;
import data.db.MongoIndexCreator;

/**
 * Base Module, including the caller module itself when getInstance()
 *
 * Created by yazhoucao on 8/7/16.
 */
@Slf4j
public class BaseModule extends AbstractModule {
  @SuppressWarnings("unused")
  public <T> T getInstance(Class<T> type, Module... modules) {
    Injector injector = Guice.createInjector(Lists.asList(this, modules));
    return injector.getInstance(type);
  }

  @SuppressWarnings("unused")
  public <T> T getInstance(Class<T> type) {
    Injector injector = Guice.createInjector(this);
    return injector.getInstance(type);
  }

  @Override
  protected void configure() {
    bind(DB.class).toInstance(provideDatabase(provideDBConfig()));
  }

//  @Provides @Singleton
  private DBConfig provideDBConfig() {
    return new DBConfig() {
      @Override
      public String getDBName() {
        return "wechat";
      }

      @Override
      public String getIP() {
        return "localhost";
      }

      @Override
      public int getPort() {
        return 27017;
      }

    };
  }

//  @Provides @Singleton
  @SuppressWarnings("deprecation")
  private DB provideDatabase(DBConfig config) {

    Preconditions.checkNotNull(config.getDBName());
    Preconditions.checkNotNull(config.getIP());
    Preconditions.checkNotNull(config.getPort());

    MongoClientOptions options = MongoClientOptions.builder()
//        .maxConnectionIdleTime()
//        .maxConnectionLifeTime()
//        .socketTimeout();
        .connectTimeout(config.getConnectTime())
        .maxWaitTime(config.getMaxWaitTime())
        .socketKeepAlive(true)
        .build();

//    MongoCredential credential = MongoCredential.createCredential(null, config.getDBName(), null);
    String dbHost = config.getIP() + ":" + config.getPort();
    log.debug("Database host is " + dbHost);
    MongoClient client = new MongoClient(dbHost, options);

    DB db = client.getDB(config.getDBName());
    // Build index
    new MongoIndexCreator(db).createIndexes();
    return db;
  }
}
