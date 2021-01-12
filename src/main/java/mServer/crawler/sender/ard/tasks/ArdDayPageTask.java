package mServer.crawler.sender.ard.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.Config;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import jakarta.ws.rs.client.WebTarget;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.ard.ArdFilmInfoDto;
import mServer.crawler.sender.ard.json.ArdDayPageDeserializer;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;

public class ArdDayPageTask extends ArdTaskBase<ArdFilmInfoDto, CrawlerUrlDTO> {

  private static final Type SET_FILMINFO_TYPE_TOKEN = new TypeToken<Set<ArdFilmInfoDto>>() {
  }.getType();

  public ArdDayPageTask(MediathekReader aCrawler,
          ConcurrentLinkedQueue aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);

    registerJsonDeserializer(SET_FILMINFO_TYPE_TOKEN, new ArdDayPageDeserializer());
  }

  @Override
  protected void processRestTarget(CrawlerUrlDTO aDTO, WebTarget aTarget) {
    if (Config.getStop()) {
      return;
    }

    Set<ArdFilmInfoDto> filmUrls = deserialize(aTarget, SET_FILMINFO_TYPE_TOKEN);

    if (filmUrls != null && !filmUrls.isEmpty()) {
      taskResults.addAll(filmUrls);
    }
  }

  @Override
  protected AbstractRecursivConverterTask createNewOwnInstance(ConcurrentLinkedQueue aElementsToProcess) {
    return new ArdDayPageTask(crawler, aElementsToProcess);
  }
}
