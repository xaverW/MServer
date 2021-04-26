package de.mediathekview.mserver.crawler.kika.json;

import com.google.gson.*;

import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.base.utils.UrlUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Optional;


public class KikaApiVideoInfoPageDeserializer implements JsonDeserializer<KikaApiVideoInfoDto> {
  //
  private static final String[] TAG_ERROR_CODE = new String[] {"error", "code"};
  private static final String[] TAG_ERROR_MESSAGE = new String[] {"error", "message"};
  private static final String TAG_MP4_ASSETS_ARRAY = "hbbtvAssets";
  private static final String TAG_MP4_URL_TAG = "url";
  private static final String TAG_M3U8_ASSETS_ARRAY = "assets";
  private static final String TAG_M3U8_URL_TAG = "url";
  private static final String[] TAG_HAS_SUB = {"additional", "subtitle"};
  private static final String[] TAG_SUB_XML = {"additional", "closedCaption", "EBU-TT"};
  private static final String[] TAG_SUB_VTT = {"additional", "closedCaption", "WebVTT"};
  //

  @Override
  public KikaApiVideoInfoDto deserialize(
      final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    KikaApiVideoInfoDto aKikaApiVideoInfoDto = new KikaApiVideoInfoDto();
    // catch error
    Optional<String> errorCode = JsonUtils.getElementValueAsString(jsonElement, TAG_ERROR_MESSAGE);
    Optional<String> errorMessage = JsonUtils.getElementValueAsString(jsonElement, TAG_ERROR_CODE);
    if (errorCode.isPresent()) {
      aKikaApiVideoInfoDto.setError(errorCode, errorMessage);
      return aKikaApiVideoInfoDto;
    }
    // search for urls
    final ArrayList<String> urls = new ArrayList<String>();
    // we will use the mp4 but in case missing - we will take the play list m3u8
    final JsonObject root = jsonElement.getAsJsonObject();
    if (root.has(TAG_MP4_ASSETS_ARRAY)) {
      final JsonArray mp4Array = root.get(TAG_MP4_ASSETS_ARRAY).getAsJsonArray();
      //
      for (JsonElement arrayElement : mp4Array) {
        final Optional<String> url = JsonUtils.getElementValueAsString(arrayElement, TAG_MP4_URL_TAG);
        if (url.isPresent() && url.get().endsWith("mp4")) {
          urls.add(UrlUtils.addProtocolIfMissing(url.get(),UrlUtils.PROTOCOL_HTTPS));
        }
      }
    }
    // no mp4 url found, lets check for m3u8 playlist entries
    if (urls.size() == 0 && root.has(TAG_M3U8_ASSETS_ARRAY)) {
      final JsonArray m3u8Array = root.get(TAG_M3U8_ASSETS_ARRAY).getAsJsonArray();
      //
      for (JsonElement arrayElement : m3u8Array) {
        final Optional<String> url = JsonUtils.getElementValueAsString(arrayElement, TAG_M3U8_URL_TAG);
        if (url.isPresent()) {
          urls.add(UrlUtils.addProtocolIfMissing(url.get(),UrlUtils.PROTOCOL_HTTPS));
        }
      }
    }
    
    // FIND BEST URL
    // the last url in the list contain the highest quality, we will use it for HD
    // the first url in the list contain the lowest quality, we will use it for SMALL
    // gap into the middle (size/2) to take one of the medium quality urls
    if (urls.size() > 0) {
      if (urls.size() > 2) {
        aKikaApiVideoInfoDto.addUrl(Resolution.HD, urls.get(urls.size()-1));
      }
      if (urls.size() > 1) {
        aKikaApiVideoInfoDto.addUrl(Resolution.NORMAL, urls.get(urls.size()/2));
      }
      aKikaApiVideoInfoDto.addUrl(Resolution.SMALL, urls.get(0));
    }
    // SUBTITLE
    Optional<String> hasSubtitles = JsonUtils.getElementValueAsString(jsonElement, TAG_HAS_SUB);
    if (hasSubtitles.isPresent() && hasSubtitles.get().equalsIgnoreCase("true")) {
      //
      aKikaApiVideoInfoDto.setSubtitle(true);
      //
      Optional<String> subXml = JsonUtils.getElementValueAsString(jsonElement, TAG_SUB_XML);          
      if (subXml.isPresent()) {
        aKikaApiVideoInfoDto.addSubtitle(subXml.get());
      }
      //
      Optional<String> subvtt = JsonUtils.getElementValueAsString(jsonElement, TAG_SUB_VTT);
      if (subvtt.isPresent()) {
        aKikaApiVideoInfoDto.addSubtitle(subvtt.get());
      }
      //
    }
    return aKikaApiVideoInfoDto;
  }

}
