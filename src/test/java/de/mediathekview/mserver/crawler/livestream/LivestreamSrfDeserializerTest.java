package de.mediathekview.mserver.crawler.livestream;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.livestream.json.LivestreamSrfDeserializer;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class LivestreamSrfDeserializerTest {

  @Test
  public void test() {
    final TopicUrlDTO[] expectedUrls =
        new TopicUrlDTO[] {
            new TopicUrlDTO(
                "SRF 1",
                "https://il.srgssr.ch/integrationlayer/2.0/mediaComposition/byUrn/urn:srf:video:c4927fcf-e1a0-0001-7edd-1ef01d441651.json"),
            new TopicUrlDTO(
                "SRF zwei",
                "https://il.srgssr.ch/integrationlayer/2.0/mediaComposition/byUrn/urn:srf:video:c49c1d64-9f60-0001-1c36-43c288c01a10.json"),
            new TopicUrlDTO(
                "SRF info",
                "https://il.srgssr.ch/integrationlayer/2.0/mediaComposition/byUrn/urn:srf:video:c49c1d73-2f70-0001-138a-15e0c4ccd3d0.json"),
        };

    final JsonElement jsonElement = JsonFileReader.readJson("/livestream/livestream_srf_overview.json");

    final LivestreamSrfDeserializer target = new LivestreamSrfDeserializer();
    final Set<TopicUrlDTO> actual = target.deserialize(jsonElement, null, null);

    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
    assertEquals(actual.size(), expectedUrls.length);
  }
}
