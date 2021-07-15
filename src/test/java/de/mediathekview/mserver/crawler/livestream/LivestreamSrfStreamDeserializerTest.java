package de.mediathekview.mserver.crawler.livestream;

import com.google.gson.JsonElement;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.livestream.json.LivestreamSrfStreamDeserializer;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class LivestreamSrfStreamDeserializerTest {

  @Test
  public void test() {
    final CrawlerUrlDTO expectedUrls = new CrawlerUrlDTO("https://srf1-euwe.akamaized.net/dea89424-61c5-40c1-8577-8e04ce1b0ade/srf1.ism/manifest(format=m3u8-aapl,encryption=cbcs-aapl,filter=nodvr)");

    final JsonElement jsonElement = JsonFileReader.readJson("/livestream/livestream_srf_detail.json");

    final LivestreamSrfStreamDeserializer target = new LivestreamSrfStreamDeserializer();
    final CrawlerUrlDTO actual = target.deserialize(jsonElement, null, null);

    assertEquals(actual, expectedUrls);
  }
}
