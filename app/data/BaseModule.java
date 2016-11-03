package data;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;

import data.db.DBConfig;

/**
 * Base Module, including the caller module itself when getInstance()
 *
 * Created by yazhoucao on 8/7/16.
 */
public abstract class BaseModule extends AbstractModule {
  public <T> T getInstance(Class<T> type, Module... modules) {
    Injector injector = Guice.createInjector(Lists.asList(this, modules));
    return injector.getInstance(type);
  }

  public <T> T getInstance(Class<T> type) {
    Injector injector = Guice.createInjector(this);
    return injector.getInstance(type);
  }

  public abstract DBConfig provideDBConfig();

  @Provides
  @Singleton
  protected DB provideDatabase(DBConfig config) {

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
    MongoClient client = new MongoClient(config.getIP(), config.getPort());
    return client.getDB(config.getDBName());
  }
}
