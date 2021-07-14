package mServer.crawler.sender.zdf.tasks;

import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractDocumentTask;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.zdf.parser.ZdfLetterListHtmlDeserializer;
import org.jsoup.nodes.Document;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ZdfLetterListHtmlTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private final transient ZdfLetterListHtmlDeserializer letterListDeserializer;

  public ZdfLetterListHtmlTask(
          final MediathekReader crawler, final ConcurrentLinkedQueue<CrawlerUrlDTO> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs);
    letterListDeserializer = new ZdfLetterListHtmlDeserializer();
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    taskResults.addAll(letterListDeserializer.deserialize(aDocument));
  }

  @Override
  protected AbstractRecursivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new ZdfLetterListHtmlTask(crawler, aElementsToProcess);
  }
}
