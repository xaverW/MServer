package mServer.crawler.sender.srf.tasks;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import jakarta.ws.rs.client.WebTarget;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.ard.tasks.ArdTaskBase;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.orf.TopicUrlDTO;
import mServer.crawler.sender.srf.SrfConstants;
import mServer.crawler.sender.srf.parser.SrfTopicsDeserializer;

public class SrfTopicsOverviewTask extends ArdTaskBase<TopicUrlDTO, CrawlerUrlDTO> {

  private static final Type SET_CRAWLER_URL_TYPE_TOKEN
          = new TypeToken<Set<TopicUrlDTO>>() {
          }.getType();

  public SrfTopicsOverviewTask(
          MediathekReader aCrawler, ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    super(aCrawler, aURLsToCrawl);

    registerJsonDeserializer(SET_CRAWLER_URL_TYPE_TOKEN, new SrfTopicsDeserializer());
  }

  @Override
  protected AbstractRecursivConverterTask createNewOwnInstance(
          ConcurrentLinkedQueue aElementsToProcess) {
    return new SrfTopicsOverviewTask(crawler, aElementsToProcess);
  }

  @Override
  protected void processRestTarget(CrawlerUrlDTO aDTO, WebTarget aTarget) {
    Set<TopicUrlDTO> results = deserialize(aTarget, SET_CRAWLER_URL_TYPE_TOKEN);
    taskResults.addAll(results);
    taskResults.addAll(addSpecialShows());
  }

  private Set<TopicUrlDTO> addSpecialShows() {
    Set<TopicUrlDTO> shows = new HashSet<>();
    shows.add(
            new TopicUrlDTO(
                    SrfConstants.ID_SHOW_SPORT_CLIP,
                    String.format(
                            SrfConstants.SHOW_OVERVIEW_PAGE_URL,
                            SrfConstants.BASE_URL,
                            SrfConstants.ID_SHOW_SPORT_CLIP)));

    return shows;
  }
}
