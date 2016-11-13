package midtier;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.Word;

import play.api.Application;
import play.api.Environment;
import play.api.Mode;
import play.api.Play;
import play.api.inject.guice.GuiceApplicationBuilder;

public class WordUtils {



  public static void main(String[] args) throws IOException {

    Application application = new GuiceApplicationBuilder()
        .in(Environment.simple(new File("."), Mode.Test()))
        .build();

    URL sentences = Play.application(application).classloader().getResource("blessingSentences");

    Preconditions.checkNotNull(sentences);
    try (BufferedReader in = new BufferedReader(new InputStreamReader(sentences.openStream()))) {
      Stream<String> stream = in.lines();
      stream.forEach(line -> {
        List<Word> words = WordSegmenter.seg(line);
        System.out.println(words);
      });

    }

//    List<Word> words = WordSegmenter.seg("杨尚川是APDPlat应用级产品开发平台的作者");
//    System.out.println(words);
//    words = WordSegmenter.segWithStopWords("杨尚川是APDPlat应用级产品开发平台的作者");
//    System.out.println(words);
  }

}
