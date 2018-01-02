package de.mediathekview.mserver.crawler.srf.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.tool.MVHttpClient;
import de.mediathekview.mserver.crawler.basic.M3U8Dto;
import de.mediathekview.mserver.crawler.basic.M3U8Parser;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import mServer.crawler.CrawlerTool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.logging.log4j.LogManager;

public class SrfFilmJsonDeserializer implements JsonDeserializer<Optional<Film>> {

  private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(SrfFilmJsonDeserializer.class);

  private static final String ATTRIBUTE_DESCRIPTION = "description";
  private static final String ATTRIBUTE_DURATION = "duration";
  private static final String ATTRIBUTE_FORMAT = "format";
  private static final String ATTRIBUTE_MIMETYPE = "mimeType";
  private static final String ATTRIBUTE_PUBLISHED_DATE = "publishedDate";
  private static final String ATTRIBUTE_TITLE = "title";
  private static final String ATTRIBUTE_URL = "url";

  private static final String ELEMENT_CHAPTER_LIST = "chapterList";
  private static final String ELEMENT_EPISODE = "episode";
  private static final String ELEMENT_RESOURCE_LIST = "resourceList";
  private static final String ELEMENT_SHOW = "show";
  private static final String ELEMENT_SUBTITLE_LIST = "subtitleList";

  private static final String SUBTITLE_FORMAT = "TTML";
  
  @Override
  public Optional<Film> deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) throws JsonParseException {

    JsonObject object = aJsonElement.getAsJsonObject();
    String theme = parseShow(object);
    EpisodeData episodeData = parseEpisode(object);
    ChapterListData chapterList = parseChapterList(object);

    if (chapterList.videoUrl.equals("")) {
      return Optional.empty();
    }

    Map<Resolution, String> videoUrls = readUrls(chapterList.videoUrl);

    Film film = new Film(UUID.randomUUID(), Sender.SRF, episodeData.title, theme, episodeData.publishDate, chapterList.duration);
    film.setBeschreibung(chapterList.description);
    addUrls(videoUrls, film);
    addSubtitle(chapterList.subtitleUrl, film);    

    return Optional.of(film);
  }

  private static void addSubtitle(String aSubtitleUrl, Film aFilm) {
    if (!aSubtitleUrl.isEmpty()) {
      try {
        aFilm.addSubtitle(new URL(aSubtitleUrl));
      } catch (MalformedURLException ex) {
        LOG.error(String.format("A subtitle URL \"%s\" isn't valid.", aSubtitleUrl), ex);
      }
    }
  }
  
  private static void addUrls(Map<Resolution, String> aVideoUrls, Film aFilm) {
    aVideoUrls.entrySet().forEach(urlEntry -> {
      try {
        aFilm.addUrl(urlEntry.getKey(), CrawlerTool.uriToFilmUrl(new URL(urlEntry.getValue())));
      } catch (MalformedURLException ex) {
        LOG.error(String.format("A found download URL \"%s\" isn't valid.", urlEntry.getValue()), ex);
      }
    });
  }

  private static String parseShow(JsonObject aJsonObject) {
    if (aJsonObject.has(ELEMENT_SHOW)) {
      JsonElement showElement = aJsonObject.get(ELEMENT_SHOW);
      if (!showElement.isJsonNull()) {
        JsonObject showObject = showElement.getAsJsonObject();
        if (showObject.has(ATTRIBUTE_TITLE)) {
          return showObject.get(ATTRIBUTE_TITLE).getAsString();
        }
      }
    }

    return "";
  }

  private EpisodeData parseEpisode(JsonObject aJsonObject) {
    EpisodeData result = new EpisodeData();

    if (aJsonObject.has(ELEMENT_EPISODE)) {
      JsonElement showElement = aJsonObject.get(ELEMENT_EPISODE);

      if (!showElement.isJsonNull()) {
        JsonObject showObject = showElement.getAsJsonObject();

        if (showObject.has(ATTRIBUTE_TITLE)) {
          result.title = showObject.get(ATTRIBUTE_TITLE).getAsString();
        }

        if (showObject.has(ATTRIBUTE_PUBLISHED_DATE)) {
          String date = showObject.get(ATTRIBUTE_PUBLISHED_DATE).getAsString();
          result.publishDate = LocalDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
      }
    }

    return result;
  }

  private ChapterListData parseChapterList(JsonObject aJsonObject) {
    ChapterListData result = new ChapterListData();

    if (aJsonObject.has(ELEMENT_CHAPTER_LIST)) {
      JsonElement chapterListElement = aJsonObject.get(ELEMENT_CHAPTER_LIST);

      if (!chapterListElement.isJsonNull()) {
        JsonArray chapterListArray = chapterListElement.getAsJsonArray();

        if (chapterListArray.size() == 1) {
          JsonObject chapterListEntry = chapterListArray.get(0).getAsJsonObject();

          if (chapterListEntry.has(ATTRIBUTE_DURATION)) {
            long duration = chapterListEntry.get(ATTRIBUTE_DURATION).getAsLong();
            result.duration = Duration.of(duration, ChronoUnit.MILLIS);
          }

          if (chapterListEntry.has(ATTRIBUTE_DESCRIPTION)) {
            result.description = chapterListEntry.get(ATTRIBUTE_DESCRIPTION).getAsString();
          }

          if (chapterListEntry.has(ELEMENT_RESOURCE_LIST)) {
            result.videoUrl = parseResourceList(chapterListEntry.get(ELEMENT_RESOURCE_LIST));
          }
          
          if (chapterListEntry.has(ELEMENT_SUBTITLE_LIST)) {
            result.subtitleUrl = parseSubtitleList(chapterListEntry.get(ELEMENT_SUBTITLE_LIST));
          }
        }
      }
    }

    return result;
  }
  
  private static String parseSubtitleList(JsonElement aSubtitleListElement) {
    if (!aSubtitleListElement.isJsonArray()) {
      return "";
    }

    JsonArray subtitleArray = aSubtitleListElement.getAsJsonArray();
    for (JsonElement arrayItemElement : subtitleArray) {
      if (!arrayItemElement.isJsonNull()) {
        JsonObject arrayItemObject = arrayItemElement.getAsJsonObject();

        if (arrayItemObject.has(ATTRIBUTE_FORMAT) && arrayItemObject.has(ATTRIBUTE_URL)) {
          if (arrayItemObject.get(ATTRIBUTE_FORMAT).getAsString().equals(SUBTITLE_FORMAT)) {
            return arrayItemObject.get(ATTRIBUTE_URL).getAsString();
          }
        }
      }
    }

    return "";
  }

  private static String parseResourceList(JsonElement aResourceListElement) {
    if (!aResourceListElement.isJsonArray()) {
      return "";
    }

    JsonArray resourceArray = aResourceListElement.getAsJsonArray();
    for (JsonElement arrayItemElement : resourceArray) {
      if (!arrayItemElement.isJsonNull()) {
        JsonObject arrayItemObject = arrayItemElement.getAsJsonObject();

        if (arrayItemObject.has(ATTRIBUTE_MIMETYPE) && arrayItemObject.has(ATTRIBUTE_URL)) {
          if (arrayItemObject.get(ATTRIBUTE_MIMETYPE).getAsString().contains("x-mpegURL")) {
            return arrayItemObject.get(ATTRIBUTE_URL).getAsString();
          }
        }
      }
    }

    return "";
  }

  private static Map<Resolution, String> readUrls(String aM3U8Url) {
    Map<Resolution, String> urls = new HashMap<>();

    MVHttpClient mvhttpClient = MVHttpClient.getInstance();
    OkHttpClient httpClient = mvhttpClient.getHttpClient();
    Request request = new Request.Builder()
            .url(aM3U8Url).build();
    try (okhttp3.Response response = httpClient.newCall(request).execute()) {
      if (response.isSuccessful()) {
        String content = response.body().string();

        M3U8Parser parser = new M3U8Parser();
        List<M3U8Dto> m3u8Data = parser.parse(content);
        m3u8Data.forEach((entry) -> {
          Optional<Resolution> resolution = getResolution(entry);
          if (resolution.isPresent()) {
            urls.put(resolution.get(), entry.getUrl());
          }
        });

      } else {
        LOG.error(String.format("Request '%s' failed: %s", aM3U8Url, response.code()));
      }
    } catch (Exception e) {
      LOG.error(e);
    }

    return urls;
  }

  private static Optional<Resolution> getResolution(M3U8Dto aDto) {
    Optional<String> widthMeta = aDto.getMeta("BANDWIDTH");
    Optional<String> codecMeta = aDto.getMeta("CODECS");

    // Codec muss "avcl" beinhalten, sonst ist es kein Video
    if (widthMeta.isPresent() && codecMeta.isPresent() && codecMeta.get().contains("avc1")) {
      int width = Integer.parseInt(widthMeta.get());
      
      if (width <= 118000) {
        return Optional.of(Resolution.SMALL);
      } else {
        return Optional.of(Resolution.NORMAL);
      }
    }

    return Optional.empty();
  }

  private class EpisodeData {

    String title;
    LocalDateTime publishDate;
  }

  private class ChapterListData {

    Duration duration;
    String description = "";
    String videoUrl;
    String subtitleUrl = "";
  }
}