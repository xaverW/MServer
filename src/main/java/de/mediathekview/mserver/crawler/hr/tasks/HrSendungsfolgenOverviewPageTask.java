package de.mediathekview.mserver.crawler.hr.tasks;

import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.logging.log4j.Level;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

public class HrSendungsfolgenOverviewPageTask
    extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {
  private static final String SENDUNGSFOLGEN_URL_SELECTOR = ".c-teaser__headlineLink.link";
  private static final long serialVersionUID = -6727831751148817578L;

  public HrSendungsfolgenOverviewPageTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
    // Some HR entries for "Programm" don't have a "sendungen" sub page which will be tried to load
    // because this sub page usually contains the "Sendungsfolgen".
    setIncrementErrorCounterOnHttpErrors(false);
    setHttpErrorLogLevel(Level.DEBUG);
    setPrintErrorMessage(false);
  }

  @Override
  protected AbstractUrlTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new HrSendungsfolgenOverviewPageTask(crawler, aURLsToCrawl);
  }

  protected String getSendungsfoleUrlSelector() {
    return SENDUNGSFOLGEN_URL_SELECTOR;
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    for (final Element filmUrlElement : aDocument.select(getSendungsfoleUrlSelector())) {
      if (filmUrlElement.hasAttr(Consts.ATTRIBUTE_HREF)) {
        crawler.incrementAndGetMaxCount();
        crawler.updateProgress();
        taskResults.add(new CrawlerUrlDTO(filmUrlElement.absUrl(Consts.ATTRIBUTE_HREF)));
      }
    }

  }

}