package mServer.crawler.sender.srf.tasks;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import jakarta.ws.rs.client.WebTarget;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.ard.tasks.ArdTaskBase;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.PagedElementListDTO;
import mServer.crawler.sender.orf.TopicUrlDTO;
import mServer.crawler.sender.srf.SrfConstants;
import mServer.crawler.sender.srf.parser.SrfTopicDeserializer;

public class SrfTopicOverviewTask extends ArdTaskBase<CrawlerUrlDTO, TopicUrlDTO> {

  private static final Type PAGED_ELEMENT_LIST_URL_TYPE_TOKEN
          = new TypeToken<PagedElementListDTO<CrawlerUrlDTO>>() {
          }.getType();

  private final String baseUrl;
  private int pageNumber;
  private int maxSubPages;

  public SrfTopicOverviewTask(
          MediathekReader aCrawler, ConcurrentLinkedQueue<TopicUrlDTO> aURLsToCrawl, String baseUrl, int maxSubPages) {
    this(aCrawler, aURLsToCrawl, baseUrl, maxSubPages, 1);
  }

  public SrfTopicOverviewTask(
          MediathekReader aCrawler,
          ConcurrentLinkedQueue<TopicUrlDTO> aURLsToCrawl,
          String baseUrl,
          int maxSubPages,
          int aPageNumber) {
    super(aCrawler, aURLsToCrawl);
    this.baseUrl = baseUrl;
    this.maxSubPages = maxSubPages;

    pageNumber = aPageNumber;
    registerJsonDeserializer(PAGED_ELEMENT_LIST_URL_TYPE_TOKEN, new SrfTopicDeserializer());
  }

  @Override
  protected AbstractRecursivConverterTask<CrawlerUrlDTO, TopicUrlDTO> createNewOwnInstance(
          ConcurrentLinkedQueue aElementsToProcess) {
    return createNewOwnInstance(aElementsToProcess, 1);
  }

  private AbstractRecursivConverterTask<CrawlerUrlDTO, TopicUrlDTO> createNewOwnInstance(
          ConcurrentLinkedQueue aElementsToProcess, int pageNumber) {
    return new SrfTopicOverviewTask(crawler, aElementsToProcess, baseUrl, maxSubPages, pageNumber);
  }

  @Override
  protected void processRestTarget(TopicUrlDTO aDTO, WebTarget aTarget) {
    PagedElementListDTO<CrawlerUrlDTO> results
            = deserialize(aTarget, PAGED_ELEMENT_LIST_URL_TYPE_TOKEN);
    taskResults.addAll(results.getElements());

    Optional<String> nextPageId = results.getNextPage();
    if (nextPageId.isPresent() && pageNumber < maxSubPages) {
      processNextPage(aDTO.getTopic(), nextPageId.get());
    }
  }

  private void processNextPage(final String aTopic, final String aNextPageId) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urlDtos = new ConcurrentLinkedQueue<>();
    urlDtos.add(
            new TopicUrlDTO(
                    aTopic,
                    String.format(SrfConstants.SHOW_OVERVIEW_NEXT_PAGE_URL, baseUrl, aTopic, aNextPageId)));
    Set<CrawlerUrlDTO> results = createNewOwnInstance(urlDtos, pageNumber + 1).invoke();
    taskResults.addAll(results);
  }
}
