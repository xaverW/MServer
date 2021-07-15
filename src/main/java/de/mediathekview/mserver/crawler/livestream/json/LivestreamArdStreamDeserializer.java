package de.mediathekview.mserver.crawler.livestream.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;


import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class LivestreamArdStreamDeserializer implements JsonDeserializer<Set<CrawlerUrlDTO>> {

  private static final String TAG_PIXEL_CONFIG = "_pixelConfig";
  private static final String TAG_CLIPURL = "clipUrl";
  private static final String TAG_AGF_META = "agfMetaDataSDK";
  private static final String TAG_AGF_META_ASSETID = "assetid";
    

  @Override
  public Set<CrawlerUrlDTO> deserialize(final JsonElement aJsonElement, final Type aType,
      final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException {
    HashSet<CrawlerUrlDTO> livestreamDetailUrls = new HashSet<>();
    if (JsonUtils.checkTreePath(aJsonElement, Optional.empty(), TAG_PIXEL_CONFIG)) {
      final JsonElement jsonTagPixelConfig = aJsonElement.getAsJsonObject().get(TAG_PIXEL_CONFIG);
      if (jsonTagPixelConfig.isJsonArray() && 
          jsonTagPixelConfig.getAsJsonArray().size() > 0 &&
          jsonTagPixelConfig.getAsJsonArray().get(0).isJsonObject()) {
        final JsonObject jsonTagFirstPixelConfig =  jsonTagPixelConfig.getAsJsonArray().get(0).getAsJsonObject();
        Optional<String> url = JsonUtils.getAttributeAsString(jsonTagFirstPixelConfig, TAG_CLIPURL);
        if (url.isPresent()) {
          livestreamDetailUrls.add(new CrawlerUrlDTO(url.get()));
        }
      }
    }
    return livestreamDetailUrls;
  }


}

