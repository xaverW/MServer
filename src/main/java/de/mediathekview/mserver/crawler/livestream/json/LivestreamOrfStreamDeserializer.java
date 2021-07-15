package de.mediathekview.mserver.crawler.livestream.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Optional;

public class LivestreamOrfStreamDeserializer implements JsonDeserializer<CrawlerUrlDTO> {

  private static final String TAG_SOURCES = "sources";
  private static final String TAG_HLS = "hls";
  private static final String TAG_SRC = "src";


  @Override
  public CrawlerUrlDTO deserialize(final JsonElement aJsonElement, final Type aType,
      final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException {
    CrawlerUrlDTO result = new CrawlerUrlDTO(""); // Return something empty to indicate error
    //
    if (JsonUtils.checkTreePath(aJsonElement, Optional.empty(), TAG_SOURCES)) {
      JsonObject tagSources = aJsonElement.getAsJsonObject().getAsJsonObject(TAG_SOURCES);
      if (JsonUtils.checkTreePath(tagSources, Optional.empty(), TAG_HLS)) {
        ArrayList<String> urls = new ArrayList<>();
        for (JsonElement url : tagSources.get(TAG_HLS).getAsJsonArray()) {
          Optional<String> oUrl = JsonUtils.getAttributeAsString(url.getAsJsonObject(), TAG_SRC);
          if (oUrl.isPresent()) {
            urls.add(oUrl.get());
          }
        }
        if (urls.size() > 0) {
          result = new CrawlerUrlDTO(urls.get(urls.size()-1)); // last one is best quality
        }
      }
    }
    return result;
  }
}
