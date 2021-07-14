package mServer.crawler.sender.zdf.tasks;

import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractDocumentTask;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.zdf.ZdfConstants;
import mServer.crawler.sender.zdf.parser.ZdfTopicPageHtmlDeserializer;
import org.jsoup.nodes.Document;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ZdfTopicPageHtmlTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private final transient ZdfTopicPageHtmlDeserializer topicsDeserializer;

  public ZdfTopicPageHtmlTask(
          final MediathekReader crawler, final ConcurrentLinkedQueue<CrawlerUrlDTO> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs);
    topicsDeserializer = new ZdfTopicPageHtmlDeserializer(ZdfConstants.URL_API_BASE);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    taskResults.addAll(topicsDeserializer.deserialize(aDocument));
  }

  @Override
  protected AbstractRecursivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new ZdfTopicPageHtmlTask(crawler, aElementsToProcess);
  }
}
