package de.mediathekview.mserver.crawler.livestream.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.livestream.LivestreamConstants;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class LivestreamSrfDeserializer implements JsonDeserializer<Set<TopicUrlDTO>> {

  private static final String TAG_LIVESTREAM = "data";
  private static final String TAG_LIVESTREAM_TITLE = "title";
  private static final String TAG_LIVESTREAM_ID = "id";

  @Override
  public Set<TopicUrlDTO> deserialize(final JsonElement aJsonElement, final Type aType,
      final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException {
    HashSet<TopicUrlDTO> livestreamDetailUrls = new HashSet<>();
    if (JsonUtils.checkTreePath(aJsonElement, Optional.empty(), TAG_LIVESTREAM)) {
      final JsonElement jsonTagLivestream = aJsonElement.getAsJsonObject().get(TAG_LIVESTREAM);
      if (jsonTagLivestream.isJsonArray()) {
        for (JsonElement livestream : jsonTagLivestream.getAsJsonArray()) {
          if (JsonUtils.checkTreePath(livestream, Optional.empty(), TAG_LIVESTREAM_TITLE) &&
              JsonUtils.checkTreePath(livestream, Optional.empty(), TAG_LIVESTREAM_ID)) {
            //
            Optional<String> title = JsonUtils.getAttributeAsString(livestream.getAsJsonObject(), TAG_LIVESTREAM_TITLE);
            Optional<String> id = JsonUtils.getAttributeAsString(livestream.getAsJsonObject(), TAG_LIVESTREAM_ID);
            if (title.isPresent() && id.isPresent()) {
              livestreamDetailUrls.add(new TopicUrlDTO(
                  title.orElse(""), 
                  String.format(LivestreamConstants.URL_SRF_LIVESTREAM_DETAIL, id.orElse(""))));
            }
            //
          }
        }
      }
    }
    return livestreamDetailUrls;
  }
}
