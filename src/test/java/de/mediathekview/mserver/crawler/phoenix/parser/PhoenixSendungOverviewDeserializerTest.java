package de.mediathekview.mserver.crawler.phoenix.parser;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.SendungOverviewDto;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(Parameterized.class)
public class PhoenixSendungOverviewDeserializerTest {

    private final String jsonFile;
    private final Optional<String> expectedNextPageId;
    private final CrawlerUrlDTO[] expectedUrls;

    public PhoenixSendungOverviewDeserializerTest(
            final String aJsonFile,
            final Optional<String> aExpectedNextPageId,
            final CrawlerUrlDTO[] aExpectedUrls) {
        jsonFile = aJsonFile;
        expectedNextPageId = aExpectedNextPageId;
        expectedUrls = aExpectedUrls;
    }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
      return Arrays.asList(
              new Object[][]{
                      {
            "/phoenix/phoenix_topic1_overview.json",
            Optional.of("/response/id/121346/counter/9/skip/8"),
                              new CrawlerUrlDTO[]{
                                      new CrawlerUrlDTO("/response/id/281502"),
                                      new CrawlerUrlDTO("/response/id/303357"),
                                      new CrawlerUrlDTO("/response/id/302357"),
                                      new CrawlerUrlDTO("/response/id/301182"),
                                      new CrawlerUrlDTO("/response/id/277547"),
                                      new CrawlerUrlDTO("/response/id/277362"),
                                      new CrawlerUrlDTO("/response/id/294727"),
                                      new CrawlerUrlDTO("/response/id/265037")
            }
                      }
              });
  }

  @Test
  public void test() {
      final JsonElement jsonElement = JsonFileReader.readJson(jsonFile);

      final PhoenixSendungOverviewDeserializer target = new PhoenixSendungOverviewDeserializer();
      final Optional<SendungOverviewDto> actual = target.deserialize(jsonElement, null, null);

    assertThat(actual.isPresent(), equalTo(true));
      final SendungOverviewDto actualDto = actual.get();

    assertThat(actualDto.getNextPageId(), equalTo(expectedNextPageId));
    assertThat(actualDto.getUrls().size(), equalTo(expectedUrls.length));
    assertThat(actualDto.getUrls(), Matchers.containsInAnyOrder(expectedUrls));
  }
}