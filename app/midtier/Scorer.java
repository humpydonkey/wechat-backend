package midtier;

import java.util.Set;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import midtier.message.BlessingDictionary;

import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.Word;

import com.google.common.base.Preconditions;

@Slf4j
public class Scorer {

  public static int score(String raw) {
    int finalScore = 0;

    Set<String> words = WordSegmenter.seg(raw).stream().map(Word::getText).collect(Collectors.toSet());
    log.debug("Segmented words: {}", words);

    finalScore += scoreStopWords(raw, words);
    finalScore += scoreByDictionary(words);

    return finalScore;
  }

  private static int scoreStopWords(@NonNull String raw, @NonNull Set<String> words) {
    int score = raw.length() - words.stream().mapToInt(String::length).sum();
    Preconditions.checkState(score >= 0);
    log.info("Get {} from StopWords", score);
    return score;
  }

  private static int scoreByDictionary(Set<String> words) {
    int score = 0;
    for (BlessingDictionary dict : BlessingDictionary.values()) {
      int currScore = dict.getScoresAndRemoveWords(words);
      log.info("Get {} score from {} dictionary", currScore, dict.name());
      score += currScore;
    }
    return score;
  }

}
