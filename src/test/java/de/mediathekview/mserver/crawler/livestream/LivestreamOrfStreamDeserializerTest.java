package de.mediathekview.mserver.crawler.livestream;

import com.google.gson.JsonElement;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.livestream.json.LivestreamOrfStreamDeserializer;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.junit.Test;


import static org.junit.Assert.assertEquals;

public class LivestreamOrfStreamDeserializerTest {

  @Test
  public void test() {
    final CrawlerUrlDTO expectedUrls = new CrawlerUrlDTO("https://orf2.mdn.ors.at/out/u/orf2/qxb/manifest.m3u8");
    
    final JsonElement jsonElement = JsonFileReader.readJson("/livestream/livestream_orf_detail.json");

    final LivestreamOrfStreamDeserializer target = new LivestreamOrfStreamDeserializer();
    final CrawlerUrlDTO actual = target.deserialize(jsonElement, null, null);

    assertEquals(actual, expectedUrls);
  }
}
