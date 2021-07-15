package de.mediathekview.mserver.crawler.livestream.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.base.utils.UrlParseException;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.livestream.LivestreamConstants;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class LivestreamZdfDeserializer implements JsonDeserializer<Set<TopicUrlDTO>> {

  private static final String TAG_EPG = "epgCluster";
  private static final String TAG_LIVESTREAM = "liveStream";
  private static final String TAG_LIVESTREAM_TYPE = "type";
  private static final String TAG_LIVESTREAM_TITLE = "titel";
  private static final String TAG_LIVESTREAM_FORMITAETEN = "formitaeten";
  private static final String TAG_LIVESTREAM_FORMITAETEN_URL = "url";
  private static final String TAG_LIVESTREAM_FORMITAETEN_QUALITY = "quality";

  @Override
  public Set<TopicUrlDTO> deserialize(final JsonElement aJsonElement, final Type aType,
      final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException {
    HashSet<TopicUrlDTO> livestreamDetailUrls = new HashSet<>();
    if (JsonUtils.checkTreePath(aJsonElement, Optional.empty(), TAG_EPG)) {
      final JsonElement jsonTagEpg = aJsonElement.getAsJsonObject().get(TAG_EPG);
      if (jsonTagEpg.isJsonArray() && 
          jsonTagEpg.getAsJsonArray().size() > 0) {
        for (JsonElement epgClusterElement : jsonTagEpg.getAsJsonArray()) {
          if (JsonUtils.checkTreePath(epgClusterElement, Optional.empty(), TAG_LIVESTREAM)) {
            final JsonObject jsonTagLivestream = epgClusterElement.getAsJsonObject().get(TAG_LIVESTREAM).getAsJsonObject();
            Optional<String> title = JsonUtils.getAttributeAsString(jsonTagLivestream, TAG_LIVESTREAM_TITLE);
            Optional<String> type = JsonUtils.getAttributeAsString(jsonTagLivestream, TAG_LIVESTREAM_TYPE);
            if (type.isPresent() && type.get().equalsIgnoreCase("livevideo")) {
              if (JsonUtils.checkTreePath(jsonTagLivestream, Optional.empty(), TAG_LIVESTREAM_FORMITAETEN)) {
                final JsonArray jsonTagFormitaetenArray = jsonTagLivestream.get(TAG_LIVESTREAM_FORMITAETEN).getAsJsonArray();
                for (JsonElement formitaetenElement : jsonTagFormitaetenArray) {
                  Optional<String> quality = JsonUtils.getAttributeAsString(formitaetenElement.getAsJsonObject(), TAG_LIVESTREAM_FORMITAETEN_QUALITY);
                  Optional<String> url = JsonUtils.getAttributeAsString(formitaetenElement.getAsJsonObject(), TAG_LIVESTREAM_FORMITAETEN_URL);
                  if (quality.isPresent() && quality.get().equalsIgnoreCase("auto")) {
                    livestreamDetailUrls.add(new TopicUrlDTO(title.orElse(""), url.orElse("")));
                  }
                }
              }
            }
          }
        }
      }
    }
    return livestreamDetailUrls;
  }

  // we need to reimplement this due to parameter without value
  // e.g. ...?abc=123&something&more=456
  
  public Optional<String> getUrlParameterValue(
      final String aUrl, final String aParameterName) throws UrlParseException {
    if (aUrl != null) {
      final Map<String, String> parameters = getUrlParameters(aUrl);
      if (parameters.containsKey(aParameterName)) {
        return Optional.of(parameters.get(aParameterName));
      }
    }

    return Optional.empty();
  }
  
  private Map<String, String> getUrlParameters(final String aUrl) throws UrlParseException {
    final Map<String, String> parameters = new HashMap<>();

    final int indexParameterStart = aUrl.indexOf('?');
    if (indexParameterStart > 0) {
      final String parameterPart = aUrl.substring(indexParameterStart + 1);
      final String[] parameterArray = parameterPart.split("&");

      for (final String parameter : parameterArray) {
        final String[] parts = parameter.split("=");
        if (parts.length == 2) {
          parameters.put(parts[0], parts[1]);
        } else {
          parameters.put(parts[0], null);
        }
      }
    }

    return parameters;
  }
}
