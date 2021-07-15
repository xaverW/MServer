package de.mediathekview.mserver.crawler.livestream.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
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

public class LivestreamArdDeserializer implements JsonDeserializer<Set<TopicUrlDTO>> {

  private static final String TAG_SECTIONS = "sections";
  private static final String TAG_MODCONS = "modCons";
  private static final String TAG_MODS = "mods";
  private static final String TAG_INHALTE = "inhalte";
  private static final String TAG_INHALTE_TITLE = "ueberschrift";
  private static final String TAG_INHALTE_LINK = "link";
  private static final String TAG_INHALTE_URL = "url";
  private static final String PARAM_BROADCAST_ID = "bcastId";

  @Override
  public Set<TopicUrlDTO> deserialize(final JsonElement aJsonElement, final Type aType,
      final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException {
    HashSet<TopicUrlDTO> livestreamDetailUrls = new HashSet<>();
    if (JsonUtils.checkTreePath(aJsonElement, Optional.empty(), TAG_SECTIONS)) {
      final JsonElement jsonTagSections = aJsonElement.getAsJsonObject().get(TAG_SECTIONS);
      if (jsonTagSections.isJsonArray() && 
          jsonTagSections.getAsJsonArray().size() > 0 &&
          jsonTagSections.getAsJsonArray().get(0).isJsonObject()) {
        final JsonElement jsonTagSection =  jsonTagSections.getAsJsonArray().get(0).getAsJsonObject();
        if (JsonUtils.checkTreePath(jsonTagSection, Optional.empty(), TAG_MODCONS)) {
          final JsonElement jsonTagModCons = jsonTagSection.getAsJsonObject().get(TAG_MODCONS);
          if (jsonTagModCons.isJsonArray() && 
              jsonTagModCons.getAsJsonArray().size() > 0 &&
              jsonTagModCons.getAsJsonArray().get(0).isJsonObject()) {
            final JsonElement jsonTagModCon =  jsonTagModCons.getAsJsonArray().get(0).getAsJsonObject();
            if (JsonUtils.checkTreePath(jsonTagModCon, Optional.empty(), TAG_MODS)) {
              final JsonElement jsonTagMods = jsonTagModCon.getAsJsonObject().get(TAG_MODS);
              if (jsonTagMods.isJsonArray() && 
                  jsonTagMods.getAsJsonArray().size() > 0 &&
                  jsonTagMods.getAsJsonArray().get(0).isJsonObject()) {
                final JsonElement jsonTagMod =  jsonTagMods.getAsJsonArray().get(0).getAsJsonObject();
                if (JsonUtils.checkTreePath(jsonTagMod, Optional.empty(), TAG_INHALTE)) {
                  final JsonElement jsonInhalte = jsonTagMod.getAsJsonObject().get(TAG_INHALTE);
                  if (jsonInhalte.isJsonArray()) {
                    for (JsonElement livestream : jsonInhalte.getAsJsonArray()) {
                      Optional<String> topic = JsonUtils.getAttributeAsString(livestream.getAsJsonObject(), TAG_INHALTE_TITLE);
                      Optional<String> url = Optional.empty();
                      if (JsonUtils.checkTreePath(livestream, Optional.empty(), TAG_INHALTE_LINK)) {
                        JsonElement jsonTagLink = livestream.getAsJsonObject().get(TAG_INHALTE_LINK);
                        if (JsonUtils.checkTreePath(jsonTagLink, Optional.empty(), TAG_INHALTE_URL)) {
                          Optional<String> urlToExtractBid = JsonUtils.getAttributeAsString(jsonTagLink.getAsJsonObject(), TAG_INHALTE_URL);
                          if (urlToExtractBid.isPresent()) {
                            Optional<String> bid = getLivestreamId(urlToExtractBid);
                            url = Optional.of(String.format(LivestreamConstants.URL_ARD_LIVESTREAM_DETAIL, bid.get()));
                          }
                        }
                      }
                      livestreamDetailUrls.add(new TopicUrlDTO(topic.orElse(""), url.orElse("")));
                    }
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

  private Optional<String> getLivestreamId(final Optional<String> aUrl) {
    Optional<String> rs = Optional.empty();
    if (aUrl.isPresent()) {
      final int lastSibling = aUrl.get().lastIndexOf("/");
      final int indexParameterStart = aUrl.get().indexOf('?');
      if (lastSibling > 0 && 
          indexParameterStart > 0 && 
          lastSibling < indexParameterStart) {
        rs = Optional.of(aUrl.get().substring(lastSibling,indexParameterStart));
      }
    }
    return rs;
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
