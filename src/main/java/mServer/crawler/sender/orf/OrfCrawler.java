package mServer.crawler.sender.orf;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveTask;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekCrawler;
import mServer.crawler.sender.orf.tasks.OrfDayTask;
import mServer.crawler.sender.orf.tasks.OrfFilmDetailTask;
import mServer.crawler.sender.orf.tasks.OrfLetterPageTask;

public class OrfCrawler extends MediathekCrawler {

  public static final String SENDERNAME = Const.ORF;

  public OrfCrawler(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, SENDERNAME, 0, 1, startPrio);
  }

  private Set<TopicUrlDTO> getDaysEntries() throws InterruptedException, ExecutionException {
    final OrfDayTask dayTask = new OrfDayTask(this, getDayUrls());
    final Set<TopicUrlDTO> shows = forkJoinPool.submit(dayTask).get();

    Log.sysLog("ORF: Anzahl Sendungen aus Verpasst: " + shows.size());

    return shows;
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> getDayUrls() {
    final int maximumDaysForSendungVerpasstSection = 8;
    final int maximumDaysForSendungVerpasstSectionFuture = 0;

    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    for (int i = 0; i < maximumDaysForSendungVerpasstSection
            + maximumDaysForSendungVerpasstSectionFuture; i++) {
      urls.add(new CrawlerUrlDTO(OrfConstants.URL_DAY + LocalDateTime.now()
              .plus(maximumDaysForSendungVerpasstSectionFuture, ChronoUnit.DAYS)
              .minus(i, ChronoUnit.DAYS).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
    }

    return urls;
  }

  private ConcurrentLinkedQueue<TopicUrlDTO> getLetterEntries() throws InterruptedException, ExecutionException {
    final OrfLetterPageTask letterTask = new OrfLetterPageTask();
    final ConcurrentLinkedQueue<TopicUrlDTO> shows = forkJoinPool.submit(letterTask).get();

    Log.sysLog("ORF: Anzahl Sendungen nach Buchstaben: " + shows.size());

    return shows;
  }

  @Override
  protected RecursiveTask<Set<DatenFilm>> createCrawlerTask() {

    final ConcurrentLinkedQueue<TopicUrlDTO> shows = new ConcurrentLinkedQueue<>();
    try {

      if (CrawlerTool.loadLongMax()) {
        shows.addAll(getLetterEntries());
      }

      getDaysEntries().forEach(show -> {
        if (!shows.contains(show)) {
          shows.add(show);
        }
      });

    } catch (InterruptedException | ExecutionException exception) {
      Log.errorLog(56146546, exception);
    }
    Log.sysLog("ORF Anzahl: " + shows.size());

    meldungAddMax(shows.size());

    return new OrfFilmDetailTask(this, shows);
  }

}
