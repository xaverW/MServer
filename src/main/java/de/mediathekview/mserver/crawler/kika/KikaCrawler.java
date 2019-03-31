package de.mediathekview.mserver.crawler.kika;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.kika.tasks.KikaLetterPageTask;
import de.mediathekview.mserver.crawler.kika.tasks.KikaLetterPageUrlTask;
import de.mediathekview.mserver.crawler.kika.tasks.KikaSendungVerpasstOverviewUrlTask;
import de.mediathekview.mserver.crawler.kika.tasks.KikaSendungVerpasstTask;
import de.mediathekview.mserver.crawler.kika.tasks.KikaSendungsfolgeVideoDetailsTask;
import de.mediathekview.mserver.crawler.kika.tasks.KikaSendungsfolgeVideoUrlTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KikaCrawler extends AbstractCrawler {
  private static final Logger LOG = LogManager.getLogger(KikaCrawler.class);
  // http://www.kika.de/sendungen/ipg/ipg102.html#date-22122017
  public static final String BASE_URL = "http://www.kika.de/";
  public static final String SENDUNGEN_OVERVIEW_PAGE_URL =
      BASE_URL + "sendungen/sendungenabisz100.html";


  public KikaCrawler(final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager aRootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, aRootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.KIKA;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> sendungsfolgenUrls = new ConcurrentLinkedQueue<>();

    try {
      sendungsfolgenUrls.addAll(getDaysEntries());
    } catch (ExecutionException | InterruptedException ex) {
      LOG.fatal("Exception in KIKA crawler.", ex);
    }

    /*
    // Gathers all Sendungen
    final Set<CrawlerUrlDTO> sendungenOverviewPageUrls = new HashSet<>();
    sendungenOverviewPageUrls.add(new CrawlerUrlDTO(SENDUNGEN_OVERVIEW_PAGE_URL));
    final KikaPagedOverviewPageTask sendungenOverviewPageTask =
        new KikaPagedOverviewPageTask(this, new ConcurrentLinkedQueue<>(sendungenOverviewPageUrls));

    // Gathers the URLs for the Sendungsfolgen overview page from the Sendungen pages.
    final Set<CrawlerUrlDTO> sendungOverviewPageUrls =
        forkJoinPool.invoke(sendungenOverviewPageTask);
    printMessage(ServerMessages.DEBUG_KIKA_SENDUNGSFOLGEN_OVERVIEWPAGES,
        sendungOverviewPageUrls.size(), getSender().getName());
    final KikaSendungOverviewPageTask sendungOverviewPageTask =
        new KikaSendungOverviewPageTask(this, new ConcurrentLinkedQueue<>(sendungOverviewPageUrls));
    // Gathers the Sendungsfolgen URLs from the Sendungsfolgen overview page.
    final Set<CrawlerUrlDTO> sendungsfolgenOverviewPageUrls =
        forkJoinPool.invoke(sendungOverviewPageTask);
    printMessage(ServerMessages.DEBUG_KIKA_SENDUNGSFOLGEN_URLS,
        sendungsfolgenOverviewPageUrls.size(), getSender().getName());
    final KikaPagedOverviewPageTask sendungsfolgenOverviewPageTask = new KikaPagedOverviewPageTask(
        this, new ConcurrentLinkedQueue<>(sendungsfolgenOverviewPageUrls));

    final ForkJoinTask<Set<CrawlerUrlDTO>> featureSendungsFolgenUrls =
        forkJoinPool.submit(sendungsfolgenOverviewPageTask);
    printMessage(ServerMessages.DEBUG_KIKA_SENDUNG_VERPASST_OVERVIEWPAGES, getSender().getName());
    final KikaSendungVerpasstOverviewUrlTask sendungVerpasstOverviewUrlTask =
        new KikaSendungVerpasstOverviewUrlTask(this);
    try {
      final Set<CrawlerUrlDTO> sendungVerpasstOverviewUrls =
          forkJoinPool.submit(sendungVerpasstOverviewUrlTask).get();
      printMessage(ServerMessages.DEBUG_KIKA_SENDUNG_VERPASST_PAGES,
          sendungVerpasstOverviewUrls.size(), getSender().getName());
      final KikaSendungVerpasstTask sendungVerpasstTask = new KikaSendungVerpasstTask(this,
          new ConcurrentLinkedQueue<>(sendungVerpasstOverviewUrls));
      sendungsfolgenUrls.addAll(forkJoinPool.invoke(sendungVerpasstTask));
    } catch (InterruptedException | ExecutionException exception) {
      LOG.fatal(
          "Something wen't terrible wrong on gathering the \"verpasste Sendungen\" overview page URLs.");
      printErrorMessage();
    }
    try {
      sendungsfolgenUrls.addAll(featureSendungsFolgenUrls.get());
    } catch (InterruptedException | ExecutionException exception) {
      LOG.fatal("Something wen't terrible wrong on gathering the Sendungsfolgen.");
      printErrorMessage();
    }*/
    printMessage(ServerMessages.DEBUG_KIKA_SENDUNGSFOLGEN_URL_CONVERTING, getSender().getName());
    final KikaSendungsfolgeVideoUrlTask sendungsfolgeVideoUrlsTask =
        new KikaSendungsfolgeVideoUrlTask(this, sendungsfolgenUrls);
    final Set<CrawlerUrlDTO> sendungsfolgeVideoUrls =
        forkJoinPool.invoke(sendungsfolgeVideoUrlsTask);
    printMessage(ServerMessages.DEBUG_KIKA_CONVERTING_FINISHED, getSender().getName());
    getAndSetMaxCount(sendungsfolgeVideoUrls.size());
    return new KikaSendungsfolgeVideoDetailsTask(this,
        new ConcurrentLinkedQueue<>(sendungsfolgeVideoUrls));
  }

  private Set<CrawlerUrlDTO> getLetterEntries() throws InterruptedException, ExecutionException {
    ConcurrentLinkedQueue<CrawlerUrlDTO> letterPageUrls = new ConcurrentLinkedQueue<>();
    letterPageUrls.add(new CrawlerUrlDTO(KikaConstants.URL_TOPICS_PAGE));
    final KikaLetterPageUrlTask letterUrlTask = new KikaLetterPageUrlTask(this, letterPageUrls, KikaConstants.BASE_URL);
    final Set<CrawlerUrlDTO> letterUrls = forkJoinPool.submit(letterUrlTask).get();

    final KikaLetterPageTask letterTask = new KikaLetterPageTask(this, new ConcurrentLinkedQueue<>(letterUrls), KikaConstants.BASE_URL);
    final Set<CrawlerUrlDTO> topicUrls = forkJoinPool.submit(letterTask).get();
    
    return null;
  }
  
  private Set<CrawlerUrlDTO> getDaysEntries() throws ExecutionException, InterruptedException {
    final Set<CrawlerUrlDTO> filmUrls = new HashSet<>();

    final KikaSendungVerpasstOverviewUrlTask daysOverviewUrlTask =
        new KikaSendungVerpasstOverviewUrlTask(this, LocalDateTime.now());

    final Set<CrawlerUrlDTO> daysUrls =
        forkJoinPool.submit(daysOverviewUrlTask).get();
    printMessage(ServerMessages.DEBUG_KIKA_SENDUNG_VERPASST_PAGES, daysUrls.size(), getSender().getName());

    final KikaSendungVerpasstTask dayTask = new KikaSendungVerpasstTask(this,
        new ConcurrentLinkedQueue<>(daysUrls), KikaConstants.BASE_URL);
    filmUrls.addAll(forkJoinPool.invoke(dayTask));

    printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), filmUrls.size());

    return filmUrls;
  }
}
