package de.mediathekview.mserver.crawler.kika.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import de.mediathekview.mserver.crawler.kika.json.KikaApiTopicDto;
import de.mediathekview.mserver.crawler.kika.json.KikaApiTopicPageDeserializer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;


@RunWith(Parameterized.class)
public class KikaApiTopicDeserializerTest {

  private String jsonFile = "";
  private int numberOfExpectedRecords = 0;
  private boolean hasSubpage = false;
  private boolean hasError = false;
  

  public KikaApiTopicDeserializerTest(String jsonFile, int numberOfExpectedRecords, boolean subpage, boolean error) {
    this.jsonFile = jsonFile;
    this.numberOfExpectedRecords = numberOfExpectedRecords;
    this.hasSubpage = subpage;
    this.hasError = error;
  }

  @Parameterized.Parameters
  public static Object[][] data() {
    return new Object[][] {
          {
            "/kika/KikaApiTopic1.json",
            10,
            true,
            false
          },
          {
            "/kika/KikaApiTopic2.json",
            11,
            false,
            false
          },
          {
            "/kika/KikaApiError.json",
            0,
            false,
            true
          }
        };

  }

  @Test
  public void testDeserializeBrand() throws URISyntaxException, IOException {
    final Type KikaApiTopicDtoType = new TypeToken<Set<KikaApiTopicDto>>() {}.getType();
    //
    final Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(KikaApiTopicDtoType, new KikaApiTopicPageDeserializer())
            .create();
    //
    final KikaApiTopicDto aKikaApiTopicDto =
        gson.fromJson(
            Files.newBufferedReader(Paths.get(getClass().getResource(jsonFile).toURI())),
            KikaApiTopicDtoType);
    //
    assertEquals(numberOfExpectedRecords, aKikaApiTopicDto.getElements().size());
    // has subpages
    assertEquals(hasSubpage, aKikaApiTopicDto.getNextPage().isPresent());
    // error page
    assertEquals(hasError, aKikaApiTopicDto.getErrorCode().isPresent());
    //
  }
  
}
