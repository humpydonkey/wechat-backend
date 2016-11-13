package controllers;

import data.model.GeneratedName;
import lombok.NonNull;
import midtier.message.BlessingDictionary;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

public class DataBootstrapController extends Controller {

  @Inject GeneratedName.Dao nameDao;

  public Result initializeDictionary(@NonNull String dictType) {
    BlessingDictionary.initialize(dictType);
    return ok("BlessingDictionary." + dictType + " initialization finished");
  }

  public Result initializeAllDictionaries() {
    BlessingDictionary.initialize(null);
    return ok("All BlessingDictionary initializations finished");
  }



  public Result initializeGeneratedNames() {

    return ok("All BlessingDictionary initializations finished");
  }
}


