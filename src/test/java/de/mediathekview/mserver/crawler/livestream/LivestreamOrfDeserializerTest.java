package de.mediathekview.mserver.crawler.livestream;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.livestream.json.LivestreamOrfDeserializer;
import de.mediathekview.mserver.crawler.livestream.json.LivestreamZdfDeserializer;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class LivestreamOrfDeserializerTest {

  @Test
  public void test() {
    final TopicUrlDTO[] expectedUrls =
        new TopicUrlDTO[] {
            new TopicUrlDTO(
                "ORF 1",
                "https://api-tvthek.orf.at/api/v4.1/livestream/14115065"),
            new TopicUrlDTO(
                "ORF 2",
                "https://api-tvthek.orf.at/api/v4.1/livestream/14115105"),
            new TopicUrlDTO(
                "ORF 3",
                "https://api-tvthek.orf.at/api/v4.1/livestream/14115136"),
            new TopicUrlDTO(
                "ORF Sport",
                "https://api-tvthek.orf.at/api/v4.1/livestream/14115154")
        };

    final JsonElement jsonElement = JsonFileReader.readJson("/livestream/livestream_orf_overview.json");

    final LivestreamOrfDeserializer target = new LivestreamOrfDeserializer();
    final Set<TopicUrlDTO> actual = target.deserialize(jsonElement, null, null);

    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
    assertEquals(actual.size(), expectedUrls.length);
  }
}
