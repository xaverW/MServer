package de.mediathekview.mserver.testhelper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AssertFilm {

  private AssertFilm() {}

  public static void assertEquals(
      final Film aActualFilm,
      final Sender aExpectedSender,
      final String aExpectedTheme,
      final String aExpectedTitle,
      final LocalDateTime aExpectedTime,
      final Duration aExpectedDuration,
      final String aExpectedDescription,
      final String aWebsiteUrl) {

    assertThat(aActualFilm, notNullValue());
    assertThat(aActualFilm.getSender(), equalTo(aExpectedSender));
    assertThat(aActualFilm.getThema(), equalTo(aExpectedTheme));
    assertThat(aActualFilm.getTitel(), equalTo(aExpectedTitle));
    assertThat(aActualFilm.getTime(), equalTo(aExpectedTime));
    assertThat(aActualFilm.getDuration(), equalTo(aExpectedDuration));
    assertThat(aActualFilm.getBeschreibung(), equalTo(aExpectedDescription));
    if (!aWebsiteUrl.isEmpty()) {
      assertThat(aActualFilm.getWebsite().get().toString(), equalTo(aWebsiteUrl));
    } else {
      assertThat(aActualFilm.getWebsite().isPresent(), equalTo(false));
    }
  }

  public static void assertEquals(
      final Film aActualFilm,
      final Sender aExpectedSender,
      final String aExpectedTheme,
      final String aExpectedTitle,
      final LocalDateTime aExpectedTime,
      final Duration aExpectedDuration,
      final String aExpectedDescription,
      final String aWebsiteUrl,
      final GeoLocations[] aExpectedGeo,
      final String aExpectedUrlSmall,
      final String aExpectedUrlNormal,
      final String aExpectedUrlHd,
      final String aExpectedSubtitle) {

    List<URL> subtitleUrls = new ArrayList<>();
    if (!aExpectedSubtitle.isEmpty()) {
      try {
        subtitleUrls.add(new URL(aExpectedSubtitle));
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
    }

    assertEquals(
        aActualFilm,
        aExpectedSender,
        aExpectedTheme,
        aExpectedTitle,
        aExpectedTime,
        aExpectedDuration,
        aExpectedDescription,
        aWebsiteUrl,
        aExpectedGeo,
        aExpectedUrlSmall,
        aExpectedUrlNormal,
        aExpectedUrlHd,
        subtitleUrls.toArray(new URL[]{}));
  }

  public static void assertEquals(
      final Film aActualFilm,
      final Sender aExpectedSender,
      final String aExpectedTheme,
      final String aExpectedTitle,
      final LocalDateTime aExpectedTime,
      final Duration aExpectedDuration,
      final String aExpectedDescription,
      final String aWebsiteUrl,
      final GeoLocations[] aExpectedGeo,
      final String aExpectedUrlSmall,
      final String aExpectedUrlNormal,
      final String aExpectedUrlHd,
      final String[] aExpectedSubtitles) {

    List<URL> subtitleUrls = new ArrayList<>();
    for (String aExpectedSubtitle : aExpectedSubtitles) {
      if (!aExpectedSubtitle.isEmpty()) {
        try {
          subtitleUrls.add(new URL(aExpectedSubtitle));
        } catch (MalformedURLException e) {
          e.printStackTrace();
        }
      }
    }

    assertEquals(
        aActualFilm,
        aExpectedSender,
        aExpectedTheme,
        aExpectedTitle,
        aExpectedTime,
        aExpectedDuration,
        aExpectedDescription,
        aWebsiteUrl,
        aExpectedGeo,
        aExpectedUrlSmall,
        aExpectedUrlNormal,
        aExpectedUrlHd,
        subtitleUrls.toArray(new URL[]{}));
  }

  public static void assertEquals(
      final Film aActualFilm,
      final Sender aExpectedSender,
      final String aExpectedTheme,
      final String aExpectedTitle,
      final LocalDateTime aExpectedTime,
      final Duration aExpectedDuration,
      final String aExpectedDescription,
      final String aWebsiteUrl,
      final GeoLocations[] aExpectedGeo,
      final String aExpectedUrlSmall,
      final String aExpectedUrlNormal,
      final String aExpectedUrlHd,
      final URL[] aExpectedSubtitles) {

    assertEquals(
        aActualFilm,
        aExpectedSender,
        aExpectedTheme,
        aExpectedTitle,
        aExpectedTime,
        aExpectedDuration,
        aExpectedDescription,
        aWebsiteUrl);

    assertThat(aActualFilm.getGeoLocations(), containsInAnyOrder(aExpectedGeo));

    assertUrl(aExpectedUrlSmall, aActualFilm.getUrl(Resolution.SMALL));
    assertUrl(aExpectedUrlNormal, aActualFilm.getUrl(Resolution.NORMAL));
    assertUrl(aExpectedUrlHd, aActualFilm.getUrl(Resolution.HD));

    assertThat(aActualFilm.getSubtitles(), containsInAnyOrder(aExpectedSubtitles));
  }

  public static void assertEquals(
      final Film aActualFilm,
      final Sender aExpectedSender,
      final String aExpectedTheme,
      final String aExpectedTitle,
      final LocalDateTime aExpectedTime,
      final Duration aExpectedDuration,
      final String aExpectedDescription,
      final String aWebsiteUrl,
      final GeoLocations[] aExpectedGeo,
      final String aExpectedUrlSmall,
      final String aExpectedUrlNormal,
      final String aExpectedUrlHd,
      final String aExpectedUrlSignLanguageSmall,
      final String aExpectedUrlSignLanguageNormal,
      final String aExpectedUrlSignLanguageHd,
      final String aExpectedUrlAudioDescriptionSmall,
      final String aExpectedUrlAudioDescriptionNormal,
      final String aExpectedUrlAudioDescriptionHd,
      final String aExpectedSubtitle) {

    assertEquals(
        aActualFilm,
        aExpectedSender,
        aExpectedTheme,
        aExpectedTitle,
        aExpectedTime,
        aExpectedDuration,
        aExpectedDescription,
        aWebsiteUrl,
        aExpectedGeo,
        aExpectedUrlSmall,
        aExpectedUrlNormal,
        aExpectedUrlHd,
        aExpectedSubtitle);
    assertSignLanguages(
        aActualFilm,
        aExpectedUrlSignLanguageSmall,
        aExpectedUrlSignLanguageNormal,
        aExpectedUrlSignLanguageHd);
    assertAudioDescriptions(
        aActualFilm,
        aExpectedUrlAudioDescriptionSmall,
        aExpectedUrlAudioDescriptionNormal,
        aExpectedUrlAudioDescriptionHd);
  }

  public static void assertUrl(final String aExpectedUrl, final FilmUrl aActualUrl) {
    if (aExpectedUrl.isEmpty()) {
      assertThat(aActualUrl, nullValue());
    } else {
      assertThat(aActualUrl.toString(), equalTo(aExpectedUrl));
    }
  }

  public static void assertUrl(final String aExpectedUrl, final Optional<String> aActualUrl) {
    assertThat(aActualUrl.isPresent(), equalTo(!aExpectedUrl.isEmpty()));
    if (aActualUrl.isPresent()) {
      assertThat(aActualUrl.get(), equalTo(aExpectedUrl));
    }
  }

  private static void assertAudioDescriptions(
      final Film aActualFilm,
      final String aExpectedUrlAudioDescriptionSmall,
      final String aExpectedUrlAudioDescriptionNormal,
      final String aExpectedUrlAudioDescriptionHd) {
    assertUrl(aExpectedUrlAudioDescriptionSmall, aActualFilm.getAudioDescription(Resolution.SMALL));
    assertUrl(
        aExpectedUrlAudioDescriptionNormal, aActualFilm.getAudioDescription(Resolution.NORMAL));
    assertUrl(aExpectedUrlAudioDescriptionHd, aActualFilm.getAudioDescription(Resolution.HD));
  }

  private static void assertSignLanguages(
      final Film aActualFilm,
      final String aExpectedUrlSignLanguageSmall,
      final String aExpectedUrlSignLanguageNormal,
      final String aExpectedUrlSignLanguageHd) {
    assertUrl(aExpectedUrlSignLanguageSmall, aActualFilm.getSignLanguage(Resolution.SMALL));
    assertUrl(aExpectedUrlSignLanguageNormal, aActualFilm.getSignLanguage(Resolution.NORMAL));
    assertUrl(aExpectedUrlSignLanguageHd, aActualFilm.getSignLanguage(Resolution.HD));
  }
}
