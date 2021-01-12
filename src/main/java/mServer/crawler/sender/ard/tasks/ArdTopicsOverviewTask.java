package mServer.crawler.sender.ard.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.Config;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import jakarta.ws.rs.client.WebTarget;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.ard.json.ArdTopicsOverviewDeserializer;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;

public class ArdTopicsOverviewTask extends ArdTaskBase<CrawlerUrlDTO, CrawlerUrlDTO> {

  private static final Type SET_CRAWLER_URL_TYPE_TOKEN = new TypeToken<Set<CrawlerUrlDTO>>() {
  }.getType();

  public ArdTopicsOverviewTask(MediathekReader aCrawler,
          ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);

    registerJsonDeserializer(SET_CRAWLER_URL_TYPE_TOKEN, new ArdTopicsOverviewDeserializer());
  }

  @Override
  protected AbstractRecursivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
          ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new ArdTopicsOverviewTask(crawler, aElementsToProcess);
  }

  @Override
  protected void processRestTarget(CrawlerUrlDTO aDTO, WebTarget aTarget) {
    if (Config.getStop()) {
      return;
    }

    Set<CrawlerUrlDTO> results = deserialize(aTarget, SET_CRAWLER_URL_TYPE_TOKEN);
    taskResults.addAll(results);
  }
}
