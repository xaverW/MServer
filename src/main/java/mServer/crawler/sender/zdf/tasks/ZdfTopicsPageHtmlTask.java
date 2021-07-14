package mServer.crawler.sender.zdf.tasks;

import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractDocumentTask;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.zdf.parser.ZdfTopicsPageHtmlDeserializer;
import org.jsoup.nodes.Document;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ZdfTopicsPageHtmlTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private final transient ZdfTopicsPageHtmlDeserializer topicsDeserializer;

  public ZdfTopicsPageHtmlTask(
          final MediathekReader crawler, final ConcurrentLinkedQueue<CrawlerUrlDTO> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs);
    topicsDeserializer = new ZdfTopicsPageHtmlDeserializer();
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    taskResults.addAll(topicsDeserializer.deserialize(aDocument));
  }

  @Override
  protected AbstractRecursivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new ZdfTopicsPageHtmlTask(crawler, aElementsToProcess);
  }
}
