package de.mediathekview.mserver.crawler.livestream;

import com.google.gson.JsonElement;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.livestream.json.LivestreamArdStreamDeserializer;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class LivestreamArdFilmDeserializerTest {

  @Test
  public void test() {
    final CrawlerUrlDTO[] expectedUrls =
        new CrawlerUrlDTO[] {
            new CrawlerUrlDTO("https://zdf-hls-19.akamaized.net/hls/live/2016502/de/high/master.m3u8")
        };

    final JsonElement jsonElement = JsonFileReader.readJson("/livestream/livestream_ard_config.json");

    final LivestreamArdStreamDeserializer target = new LivestreamArdStreamDeserializer();
    final Set<CrawlerUrlDTO> actual = target.deserialize(jsonElement, null, null);

    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
    assertEquals(actual.size(), expectedUrls.length);
  }
}
