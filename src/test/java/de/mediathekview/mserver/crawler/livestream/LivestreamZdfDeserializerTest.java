package de.mediathekview.mserver.crawler.livestream;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.livestream.json.LivestreamZdfDeserializer;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class LivestreamZdfDeserializerTest {

  @Test
  public void test() {
    final TopicUrlDTO[] expectedUrls =
        new TopicUrlDTO[] {
            new TopicUrlDTO(
                "3sat im Livestream",
                "https://zdf-hls-18.akamaized.net/hls/live/2016501/dach/high/master.m3u8"),
            new TopicUrlDTO(
                "KiKA im Livestream",
                "https://kikageohls.akamaized.net/hls/live/2022693/livetvkika_de/master.m3u8"),
            new TopicUrlDTO(
                "ZDFinfo im Livestream",
                "https://zdf-hls-17.akamaized.net/hls/live/2016500/de/high/master.m3u8"),
            new TopicUrlDTO(
                "Phoenix im Livestream",
                "https://zdf-hls-19.akamaized.net/hls/live/2016502/de/high/master.m3u8"),
            new TopicUrlDTO(
                "Das ZDF im Livestream",
                "https://zdf-hls-15.akamaized.net/hls/live/2016498/de/high/master.m3u8"),
            new TopicUrlDTO(
                "ZDFneo im Livestream",
                "https://zdf-hls-16.akamaized.net/hls/live/2016499/de/high/master.m3u8"),
            new TopicUrlDTO(
                "arte im Livestream",
                "https://artelive-lh.akamaihd.net/i/artelive_de@393591/master.m3u8?set-segment-duration=quality"),

        };

    final JsonElement jsonElement = JsonFileReader.readJson("/livestream/livestream_zdf_overview.json");

    final LivestreamZdfDeserializer target = new LivestreamZdfDeserializer();
    final Set<TopicUrlDTO> actual = target.deserialize(jsonElement, null, null);

    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
    assertEquals(actual.size(), expectedUrls.length);
  }
}
