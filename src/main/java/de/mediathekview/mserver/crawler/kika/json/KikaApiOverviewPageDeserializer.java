package de.mediathekview.mserver.crawler.kika.json;

import com.google.gson.*;

import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.kika.KikaApiConstants;

import java.lang.reflect.Type;
import java.util.Optional;


public class KikaApiOverviewPageDeserializer implements JsonDeserializer<KikaApiBrandsDto> {
  private static final String[] TAG_ERROR_CODE = new String[] {"error", "code"};
  private static final String[] TAG_ERROR_MESSAGE = new String[] {"error", "message"};
  private static final String[] TAG_NEXT_PAGE = new String[] {"_links", "next", "href"};
  private static final String[] TAG_TOPIC_ARRAY = new String[] {"_embedded","items"};
  private static final String TAG_TOPIC_NAME = "title";
  private static final String TAG_TOPIC_ID = "id";

  public KikaApiOverviewPageDeserializer() {
  }

  @Override
  public KikaApiBrandsDto deserialize(
      final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    KikaApiBrandsDto aKikaApiBrandsDto = new KikaApiBrandsDto();
    //
    // catch error
    Optional<String> errorCode = JsonUtils.getElementValueAsString(jsonElement, TAG_ERROR_MESSAGE);
    Optional<String> errorMessage = JsonUtils.getElementValueAsString(jsonElement, TAG_ERROR_CODE);
    if (errorCode.isPresent()) {
      aKikaApiBrandsDto.setError(errorCode, errorMessage);
      return aKikaApiBrandsDto;
    }
    // next page
    Optional<String> nextPage = JsonUtils.getElementValueAsString(jsonElement, TAG_NEXT_PAGE);
    if (nextPage.isPresent()) {
      aKikaApiBrandsDto.setNextPage(new CrawlerUrlDTO(UrlUtils.addProtocolIfMissing(KikaApiConstants.HOST + nextPage.get(), UrlUtils.PROTOCOL_HTTPS)));
    }
    // all topics
    final JsonObject searchElement = jsonElement.getAsJsonObject();
    if (searchElement.has(TAG_TOPIC_ARRAY[0])) {
      final JsonObject embeddedElement = searchElement.getAsJsonObject(TAG_TOPIC_ARRAY[0]);
      if (embeddedElement.has(TAG_TOPIC_ARRAY[1])) {
        final JsonArray itemArray = embeddedElement.getAsJsonArray(TAG_TOPIC_ARRAY[1]);
        for (JsonElement arrayElement : itemArray) {
          Optional<String> title = JsonUtils.getElementValueAsString(arrayElement, TAG_TOPIC_NAME);
          Optional<String> id = JsonUtils.getElementValueAsString(arrayElement, TAG_TOPIC_ID);
          if (title.isPresent() && id.isPresent()) {
            aKikaApiBrandsDto.add(new TopicUrlDTO(title.get(), String.format(KikaApiConstants.TOPIC, id.get())));
          }
        }
      }
    }
    return aKikaApiBrandsDto;
  }

}
