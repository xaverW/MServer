package de.mediathekview.mserver.crawler.livestream.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

import java.lang.reflect.Type;
import java.util.Optional;

public class LivestreamSrfStreamDeserializer implements JsonDeserializer<CrawlerUrlDTO> {

  private static final String TAG_LIVESTREAM_CHAPTER = "chapterList";
  private static final String TAG_LIVESTREAM_RESOURCE = "resourceList";
  private static final String TAG_LIVESTREAM_URL = "url";
  private static final String TAG_PROTOCOL = "protocol";

  @Override
  public CrawlerUrlDTO deserialize(final JsonElement aJsonElement, final Type aType,
      final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException {
    CrawlerUrlDTO livestreamDetailUrls = new CrawlerUrlDTO(""); // dummy
    if (JsonUtils.checkTreePath(aJsonElement, Optional.empty(), TAG_LIVESTREAM_CHAPTER)) {
      final JsonElement jsonTagLivestream = aJsonElement.getAsJsonObject().get(TAG_LIVESTREAM_CHAPTER);
      if (jsonTagLivestream.isJsonArray() && 
          jsonTagLivestream.getAsJsonArray().size() > 0) {
        final JsonElement jsonElement = jsonTagLivestream.getAsJsonArray().get(0);
        if (jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has(TAG_LIVESTREAM_RESOURCE) &&
            jsonElement.getAsJsonObject().get(TAG_LIVESTREAM_RESOURCE).isJsonArray()) {
          for (JsonElement resourceElement : jsonElement.getAsJsonObject().get(TAG_LIVESTREAM_RESOURCE).getAsJsonArray()) {
            Optional<String> protocol = JsonUtils.getAttributeAsString(resourceElement.getAsJsonObject(), TAG_PROTOCOL);
            if (protocol.isPresent() && protocol.get().equalsIgnoreCase("HLS")) {
              Optional<String> url = JsonUtils.getAttributeAsString(resourceElement.getAsJsonObject(), TAG_LIVESTREAM_URL);
              if (url.isPresent()) {
                livestreamDetailUrls = new CrawlerUrlDTO(url.get());
              }
            }
          }
        }
      }
    }
    return livestreamDetailUrls;
  }
}
