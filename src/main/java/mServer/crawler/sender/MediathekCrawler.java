package mServer.crawler.sender;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import mServer.crawler.FilmeSuchen;

// Base class of crawlers using ForkJoinPool
public abstract class MediathekCrawler extends MediathekReader {

  protected final ForkJoinPool forkJoinPool;

  public MediathekCrawler(FilmeSuchen aMSearchFilmeSuchen, String aSendername, int aSenderMaxThread, int aSenderWartenSeiteLaden, int aStartPrio) {
    super(aMSearchFilmeSuchen, aSendername, aSenderMaxThread, aSenderWartenSeiteLaden, aStartPrio);

    forkJoinPool = createForkJoinPool(aSendername);
  }

  public static ForkJoinPool createForkJoinPool(String aSenderName) {
    final ForkJoinPool.ForkJoinWorkerThreadFactory factory = new ForkJoinPool.ForkJoinWorkerThreadFactory() {
      @Override
      public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
        final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
        worker.setName(aSenderName + " Pool Thread-" + worker.getPoolIndex());
        return worker;
      }
    };

    return new ForkJoinPool(Runtime.getRuntime().availableProcessors() * 4, factory, null, false);
  }

  @Override
  protected void addToList() {
    meldungStart();

    try {
      runCrawler();

      Log.sysLog(getSendername() + ": Film einsortieren fertig");
    } catch (Exception e) {
      Log.errorLog(516516521, e);
    } finally {
      //explicitely shutdown the pool
      shutdownAndAwaitTermination(60, TimeUnit.SECONDS);
    }

    Log.sysLog(getSendername() + ": fertig");

    meldungThreadUndFertig();
  }

  protected void runCrawler() {
    RecursiveTask<Set<DatenFilm>> filmTask = createCrawlerTask();
    Set<DatenFilm> films = forkJoinPool.invoke(filmTask);

    Log.sysLog(getSendername() + ": Filme einsortieren...");

    films.forEach(film -> {
      if (!Config.getStop()) {
        addFilm(film);
      }
    });
  }

  protected abstract RecursiveTask<Set<DatenFilm>> createCrawlerTask();

  private void shutdownAndAwaitTermination(long delay, TimeUnit delayUnit) {
    Log.sysLog(getSendername() + ": shutdown pool...");

    try {
      try {
        forkJoinPool.shutdown();
        if (!forkJoinPool.awaitTermination(delay, delayUnit)) {
          forkJoinPool.shutdownNow();
          if (!forkJoinPool.awaitTermination(delay, delayUnit)) {
            Log.sysLog(getSendername() + ": Pool nicht beendet");
          }
        }
      } catch (InterruptedException ie) {
        Log.errorLog(974513454, ie);
        forkJoinPool.shutdownNow();
        Thread.currentThread().interrupt();
      }
    } catch (Exception e) {
      Log.errorLog(974513455, e);
    }
  }
}
