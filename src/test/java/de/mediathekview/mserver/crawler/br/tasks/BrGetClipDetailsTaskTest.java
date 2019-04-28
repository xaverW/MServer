package de.mediathekview.mserver.crawler.br.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.br.BrCrawler;
import de.mediathekview.mserver.testhelper.AssertFilm;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class BrGetClipDetailsTaskTest extends BrTaskTestBase {

  private final String jsonFile;
  private final String id;
  private final String expectedTopic;
  private final String expectedTitle;
  private final LocalDateTime expectedTime;
  private final Duration expectedDuration;
  private final String expectedDescription;
  private final String expectedWebsite;
  private final String expectedUrlSmall;
  private final String expectedUrlNormal;
  private final String expectedUrlHd;
  private final String[] expectedSubtitles;
  private final GeoLocations expectedGeo;
  private final String requestUrl;

  public BrGetClipDetailsTaskTest(
      final String aRequestUrl,
      final String aJsonFile,
      final String aId,
      final String aExpectedTopic,
      final String aExpectedTitle,
      final LocalDateTime aExpectedTime,
      final Duration aExpectedDuration,
      final String aExpectedDescription,
      final String aExpectedWebsite,
      final String aExpectedUrlSmall,
      final String aExpectedUrlNormal,
      final String aExpectedUrlHd,
      final String[] aExpectedSubtitles,
      final GeoLocations aExpectedGeo) {
    requestUrl = aRequestUrl;
    jsonFile = aJsonFile;
    id = aId;
    expectedTopic = aExpectedTopic;
    expectedTitle = aExpectedTitle;
    expectedTime = aExpectedTime;
    expectedDuration = aExpectedDuration;
    expectedDescription = aExpectedDescription;
    expectedWebsite = aExpectedWebsite;
    expectedUrlSmall = aExpectedUrlSmall;
    expectedUrlNormal = aExpectedUrlNormal;
    expectedUrlHd = aExpectedUrlHd;
    expectedSubtitles = aExpectedSubtitles;
    expectedGeo = aExpectedGeo;
  }

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "/myBrRequets",
            "/br/br_film_with_subtitle.json",
            "av:5acf4af2830ea00017630009",
            "Frei Schnauze",
            "Erziehungstipp Hund springt Mensch an",
            LocalDateTime.of(2018, 5, 26, 17, 5, 0),
            Duration.ofMinutes(9).plusSeconds(23),
            "Eines von Lisas Pferden befindet sich auf Abwegen, und die trächtige Stute Linn ist jetzt schon über Termin. Kommt das Fohlen jetzt endlich mal?  Im Beitrag lernen die Zuschauer Ruby kennen. Er ist ein sehr verspielter Hund, der beim Gassigehen fast jeden Menschen auf seine Art begrüßen will. Doch kaum ein Passant versteht Rubys Freude. Immer wieder gibt es deshalb Stress. Nun sucht sein Frauchen \n.....",
            "https://www.br.de/mediathek/video/frei-schnauze-das-tiermagazin-26052018-erziehungstipp-hund-springt-mensch-an-av:5acf4af2830ea00017630009",
            "https://cdn-storage.br.de/MUJIuUOVBwQIbtC2uKJDM6OhuLnC_2rc9U1S/_-OS/_AFP9-kG571S/22c9ef56-eeb3-4d2a-a603-9eddf63b6303_E.mp4",
            "https://cdn-storage.br.de/MUJIuUOVBwQIbtC2uKJDM6OhuLnC_2rc9U1S/_-OS/_AFP9-kG571S/22c9ef56-eeb3-4d2a-a603-9eddf63b6303_C.mp4",
            "https://cdn-storage.br.de/MUJIuUOVBwQIbtC2uKJDM6OhuLnC_2rc9U1S/_-OS/_AFP9-kG571S/22c9ef56-eeb3-4d2a-a603-9eddf63b6303_X.mp4",
            new String[] {
              "https://www.br.de/untertitel/97a833c9-4d78-43d6-af06-f6ac1bedf5d0.ttml",
              "https://www.br.de/untertitel/97a833c9-4d78-43d6-af06-f6ac1bedf5d0.vtt"
            },
            GeoLocations.GEO_NONE
          },
          {
            "/myBrRequets",
            "/br/br_film_with_geo.json",
            "av:5c92671b4823a30013753fb6",
            "Es war einmal ... der Mensch",
            "Peter der Große",
            LocalDateTime.of(2019, 4, 27, 15, 0, 0),
            Duration.ofMinutes(25).plusSeconds(1),
            "Eine Zeitreise durch die Geschichte der Menschheit in 26 Folgen - das ist die Zeichentrickserie \"Es war einmal … der Mensch\" aus dem Jahr 1978. Unterhaltsam, humorvoll und lehrreich zugleich gibt sie Antworten auf Fragen, wie sie Kinder stellen. Antworten, die, pfiffig präsentiert, auch Erwachsene ansprechen. Erfunden und produziert von dem französischen Regisseur Albert Barillé, war die Serie in \n.....",
            "https://www.br.de/mediathek/video/es-war-einmal-der-mensch-peter-der-grosse-av:5c92671b4823a30013753fb6",
            "https://cdn-storage.br.de/geo/b7/2019-04/27/2a4fee2c68ef11e9a0b0984be10adece_E.mp4",
            "https://cdn-storage.br.de/geo/b7/2019-04/27/2a4fee2c68ef11e9a0b0984be10adece_C.mp4",
            "https://cdn-storage.br.de/geo/b7/2019-04/27/2a4fee2c68ef11e9a0b0984be10adece_X.mp4",
            new String[0],
            GeoLocations.GEO_DE
          }
        });
  }

  @Test
  public void test() {

    setupSuccessfulJsonPostResponse(requestUrl, jsonFile);

    BrCrawler crawler = createCrawler();

    BrGetClipDetailsTask clipDetails = new BrGetClipDetailsTask(crawler, createClipQueue(id));

    Set<Film> resultSet = clipDetails.compute();

    assertThat(resultSet.size(), equalTo(1));
    final Film actual = resultSet.iterator().next();

    AssertFilm.assertEquals(
        actual,
        Sender.BR,
        expectedTopic,
        expectedTitle,
        expectedTime,
        expectedDuration,
        expectedDescription,
        expectedWebsite,
        new GeoLocations[] {expectedGeo},
        expectedUrlSmall,
        expectedUrlNormal,
        expectedUrlHd,
        expectedSubtitles);
  }
}
