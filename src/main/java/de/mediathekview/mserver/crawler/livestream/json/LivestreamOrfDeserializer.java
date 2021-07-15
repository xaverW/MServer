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

public class LivestreamOrfDeserializer implements JsonDeserializer<Set<TopicUrlDTO>> {

  private static final String TAG_ORF1 = "orf1";
  private static final String TAG_ORF2 = "orf2";
  private static final String TAG_ORF3 = "orf3";
  private static final String TAG_ORFS = "orfs";
  private static final String TAG_ITEMS = "items";
  private static final String TAG_ITEM_ID = "id";

  @Override
  public Set<TopicUrlDTO> deserialize(final JsonElement aJsonElement, final Type aType,
      final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException {
    HashSet<TopicUrlDTO> livestreamDetailUrls = new HashSet<>();
    //
    Optional<String> orf1 = getSampleLivestreamId(aJsonElement,TAG_ORF1);
    if (orf1.isPresent()) {
      livestreamDetailUrls.add(new TopicUrlDTO("ORF 1", String.format(LivestreamConstants.URL_ORF_LIVESTREAM_DETAIL, orf1.get())));
    }
    //
    Optional<String> orf2 = getSampleLivestreamId(aJsonElement,TAG_ORF2);
    if (orf2.isPresent()) {
      livestreamDetailUrls.add(new TopicUrlDTO("ORF 2", String.format(LivestreamConstants.URL_ORF_LIVESTREAM_DETAIL, orf2.get())));
    }
    //
    Optional<String> orf3 = getSampleLivestreamId(aJsonElement,TAG_ORF3);
    if (orf3.isPresent()) {
      livestreamDetailUrls.add(new TopicUrlDTO("ORF 3", String.format(LivestreamConstants.URL_ORF_LIVESTREAM_DETAIL, orf3.get())));
    }
    //
    Optional<String> orfs = getSampleLivestreamId(aJsonElement,TAG_ORFS);
    if (orfs.isPresent()) {
      livestreamDetailUrls.add(new TopicUrlDTO("ORF Sport", String.format(LivestreamConstants.URL_ORF_LIVESTREAM_DETAIL, orfs.get())));
    }
    //
    return livestreamDetailUrls;
  }

  private Optional<String> getSampleLivestreamId(JsonElement aJsonElement, String channelTag) {
    if (JsonUtils.checkTreePath(aJsonElement, Optional.empty(), channelTag)) {
      final JsonElement jsonTagOrf1 = aJsonElement.getAsJsonObject().get(channelTag);
      if (JsonUtils.checkTreePath(jsonTagOrf1, Optional.empty(), TAG_ITEMS) &&
          jsonTagOrf1.getAsJsonObject().get(TAG_ITEMS).isJsonArray() &&
          jsonTagOrf1.getAsJsonObject().get(TAG_ITEMS).getAsJsonArray().size() > 0) {
        final JsonElement justOneSample =  jsonTagOrf1.getAsJsonObject().get(TAG_ITEMS).getAsJsonArray().get(0);
        return JsonUtils.getAttributeAsString(justOneSample.getAsJsonObject(), TAG_ITEM_ID);
      }
    }
    return Optional.empty();
  }

}
