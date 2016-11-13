package midtier.message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.Word;

@Slf4j
@AllArgsConstructor
public enum BlessingDictionary {

  PERSONAL(new HashSet<>(), 20),
  BIRTHDAY_ELDER(new HashSet<>(), 6),
  BIRTHDAY_GENERAL(new HashSet<>(), 3),
  GENERAL_GOOD(new HashSet<>(), 2);

  private static final String SOURCE_FILE = "birthdayGeneralSentences";
  private static final String TARGET_FILE = "birthdayGeneralWords";

  private static final String PERSONAL_FILE = "birthday/personalWords";
  private static final String BIRTHDAY_ELDER_FILE = "birthday/birthdayElderWords";
  private static final String BIRTHDAY_GENERAL_FILE = "birthday/birthdayGeneralWords";
  private static final String GENERAL_GOOD_FILE = "birthday/blessingWords";

  private Set<String> words;
  private int weight;

  public static void initialize() {
    readWords(PERSONAL, PERSONAL_FILE);
    readWords(BIRTHDAY_ELDER, BIRTHDAY_ELDER_FILE);
    readWords(BIRTHDAY_GENERAL, BIRTHDAY_GENERAL_FILE);
    readWords(GENERAL_GOOD, GENERAL_GOOD_FILE);
    log.info("BlessingDictionary initialization finished");
  }

  public static void initialize(String type) {
    if (type == null) {
      initialize();
      return;
    }
    BlessingDictionary dictType = BlessingDictionary.valueOf(type);
    switch (dictType) {
      case PERSONAL:
        readWords(PERSONAL, PERSONAL_FILE);
        break;
      case BIRTHDAY_ELDER:
        readWords(BIRTHDAY_ELDER, PERSONAL_FILE);
        break;
      case BIRTHDAY_GENERAL:
        readWords(BIRTHDAY_GENERAL, PERSONAL_FILE);
        break;
      case GENERAL_GOOD:
        readWords(GENERAL_GOOD, PERSONAL_FILE);
        break;
      default:
        throw new IllegalArgumentException("Unknown type " + type);
    }
  }

  @SneakyThrows(IOException.class)
  private static void readWords(BlessingDictionary dict, String file) {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(BlessingDictionary.class.getClassLoader().getResourceAsStream(file)))) {
      in.lines().forEach(line -> dict.words.add(line));
    }
  }

  @SuppressWarnings("unused")
  @SneakyThrows(IOException.class)
  public static void initializeSourceFiles() {
    Set<String> wordSet = new HashSet<>();
    try (BufferedReader in = new BufferedReader(new InputStreamReader(BlessingDictionary.class.getClassLoader().getResourceAsStream(SOURCE_FILE)))) {
      Stream<String> stream = in.lines();
      stream.forEach(line -> {
        Set<String> currWords = WordSegmenter.seg(line).stream().map(Word::getText).collect(Collectors.toSet());
        wordSet.addAll(currWords);
      });
    }

    List<String> sortedWords = new ArrayList<>(wordSet);
    Collections.sort(sortedWords, (o1, o2) -> {
      if (o1.length() == o2.length()) {
        return o1.compareTo(o2);
      }
      return o1.length() - o2.length();
    });

    Files.write(Paths.get(TARGET_FILE), sortedWords, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    log.info("BlessingDictionary source file {} initialization finished", TARGET_FILE);
  }

  public int getScoresAndRemoveWords(Set<String> words) {
    int score = 0;
    Set<String> contains = new HashSet<>();
    for (String word : words) {
      if (this.words.contains(word)) {
        score += weight * word.length();
        contains.add(word);
      }
    }
    words.removeAll(contains);
    return score;
  }
}
