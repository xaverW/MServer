package de.mediathekview.mserver.crawler.livestream;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.livestream.json.LivestreamArdDeserializer;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class LivestreamArdDeserializerTest {

  @Test
  public void test() {
    final TopicUrlDTO[] expectedUrls =
        new TopicUrlDTO[] {
            new TopicUrlDTO(
                "NDR Fernsehen",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//68586116"),
                new TopicUrlDTO(
                "SWR Fernsehen Rheinland-Pfalz",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//22709652"),
                new TopicUrlDTO(
                "hr-fernsehen",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//17301408"),
                new TopicUrlDTO(
                "BR Fernsehen Nord",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//21536390"),
                new TopicUrlDTO(
                "ONE im Livestream",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//36089960"),
                new TopicUrlDTO(
                "SR Livestream",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//22496146"),
                new TopicUrlDTO(
                "MDR Fernsehen Thüringen",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//79556522"),
                new TopicUrlDTO(
                "WDR Fernsehen im Livestream",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//43854900"),
                new TopicUrlDTO(
                "ARD-alpha Livestream",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//21542408"),
                new TopicUrlDTO(
                "3sat Livestream",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//24127206"),
                new TopicUrlDTO(
                "phoenix live",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//83051238"),
                new TopicUrlDTO(
                "BR Fernsehen Süd",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//21536446"),
                new TopicUrlDTO(
                "Das Erste",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//21520924"),
                new TopicUrlDTO(
                "rbb Fernsehen Berlin",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//34050018"),
                new TopicUrlDTO(
                "MDR Fernsehen Sachsen-Anhalt",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//79556536"),
                new TopicUrlDTO(
                "Deutsche Welle (DW) - Die mediale Stimme Deutschlands",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//35283076"),
                new TopicUrlDTO(
                "KiKA Livestream",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//29026312"),
                new TopicUrlDTO(
                "rbb Fernsehen Brandenburg",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//34934242"),
                new TopicUrlDTO(
                "tagesschau24-Livestream",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//31493188"),
                new TopicUrlDTO(
                "MDR Fernsehen Sachsen",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//79556550"),
                new TopicUrlDTO(
                "NDR Fernsehen Mecklenburg-Vorpommern",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//80794094"),
                new TopicUrlDTO(
                "SWR Fernsehen Baden-Württemberg",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//22709680"),
                new TopicUrlDTO(
                "ARTE Livestream",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//21200504"),
                new TopicUrlDTO(
                "NDR Fernsehen Schleswig-Holstein",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//68615978"),
                new TopicUrlDTO(
                "NDR Fernsehen Hamburg",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//74351900"),
                new TopicUrlDTO(
                "ARTE Livestream französisch",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config//36040754")
        };

    final JsonElement jsonElement = JsonFileReader.readJson("/livestream/livestream_ard_overview.json");

    final LivestreamArdDeserializer target = new LivestreamArdDeserializer();
    final Set<TopicUrlDTO> actual = target.deserialize(jsonElement, null, null);
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
    assertEquals(actual.size(), expectedUrls.length);
  }
}
