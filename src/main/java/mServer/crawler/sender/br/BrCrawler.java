package mServer.crawler.sender.br;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekCrawler;

public class BrCrawler extends MediathekCrawler {

  public static final String SENDERNAME = Const.BR;
  public static final String BASE_URL = "https://www.br.de/mediathek/";

  public BrCrawler(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, SENDERNAME, 0, 100, startPrio);
  }

  private RecursiveTask<Set<String>> createAllSendungenOverviewCrawler() {
    return new BrAllSendungenTask(this, forkJoinPool);
  }

  private Callable<Set<String>> createMissedFilmsCrawler() {
    int maximumDays;
    if (CrawlerTool.loadLongMax()) {
      maximumDays = 21;
    } else {
      maximumDays = 7;
    }

    return new BrMissedSendungsFolgenTask(this, maximumDays);
  }

  @Override
  protected RecursiveTask<Set<DatenFilm>> createCrawlerTask() {
    final Callable<Set<String>> missedFilmsTask = createMissedFilmsCrawler();
    final RecursiveTask<Set<String>> sendungenFilmsTask = createAllSendungenOverviewCrawler();
    final Future<Set<String>> missedFilmIds = forkJoinPool.submit(missedFilmsTask);
    forkJoinPool.execute(sendungenFilmsTask);

    final ConcurrentLinkedQueue<String> brFilmIds = new ConcurrentLinkedQueue<>();
    try {
      brFilmIds.addAll(missedFilmIds.get());
      Log.sysLog("BR Anzahl verpasste Sendungen: " + missedFilmIds.get().size());
    } catch (Exception exception) {
      Log.errorLog(782346382, exception);
    }

    try {
      brFilmIds.addAll(sendungenFilmsTask.join());
      Log.sysLog("BR Anzahl: " + sendungenFilmsTask.join().size());
    } catch (Exception exception) {
      Log.errorLog(782346383, exception);
    }
    int max = (brFilmIds.size() / BrSendungDetailsTask.MAXIMUM_URLS_PER_TASK) + 1;
    meldungAddMax(max);

    return new BrSendungDetailsTask(this, brFilmIds);
  }

}
