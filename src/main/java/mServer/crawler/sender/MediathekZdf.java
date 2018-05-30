/*
 * MediathekView
 * Copyright (C) 2008 W. Xaver
 * W.Xaver[at]googlemail.com
 * http://zdfmediathk.sourceforge.net/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package mServer.crawler.sender;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmPoint;
import java.util.Collection;
import java.util.concurrent.*;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.RunSender;
import mServer.crawler.sender.newsearch.*;

public class MediathekZdf extends MediathekReader {

  public final static String SENDERNAME = Const.ZDF;
  private ForkJoinPool forkJoinPool;

  public MediathekZdf(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, SENDERNAME, 0 /* threads */, 150 /* urlWarten */, startPrio);
    setName("MediathekZdf");
  }

  private final Phaser phaser = new Phaser();

  @Override
  public void addToList() {
    meldungStart();
    meldungAddThread();

    int days = CrawlerTool.loadLongMax() ? 300 : 20;

    try {

      final ZDFSearchTask newTask = new ZDFSearchTask(days);
      forkJoinPool = MediathekCrawler.createForkJoinPool(getSendername());
      forkJoinPool.execute(newTask);
      Collection<VideoDTO> filmList = newTask.join();

      convertToDto(filmList);

    } catch (Exception e) {
      Log.errorLog(516516521, e);
    } finally {
      //explicitely shutdown the pool
      shutdownAndAwaitTermination(forkJoinPool, 60, TimeUnit.SECONDS);
    }

    meldungThreadUndFertig();
  }

  void convertToDto(Collection<VideoDTO> filmList) {
    EtmPoint perfPoint = EtmManager.getEtmMonitor().createPoint("MediathekZdf.convertVideoDTO");

    if (!filmList.isEmpty()) {
      // Convert new DTO to old DatenFilm class
      Log.sysLog("convert VideoDTO to DatenFilm started...");
      filmList.parallelStream().forEach((video) -> {
        VideoDtoDatenFilmConverterAction action = new VideoDtoDatenFilmConverterAction(video);
        forkJoinPool.execute(action);
      });

      filmList.clear();

      boolean wasInterrupted = false;
      while (!phaser.isTerminated()) {
        try {
          if (Config.getStop()) {
            wasInterrupted = true;
            phaser.forceTermination();
            shutdownAndAwaitTermination(forkJoinPool, 5, TimeUnit.SECONDS);
          } else {
            TimeUnit.SECONDS.sleep(1);
          }
        } catch (InterruptedException ignored) {
        }
      }

      if (wasInterrupted) {
        Log.sysLog("VideoDTO conversion interrupted.");
      } else {
        Log.sysLog("convert VideoDTO to DatenFilm finished.");
      }
    }

    perfPoint.collect();
  }

  void shutdownAndAwaitTermination(ExecutorService pool, long delay, TimeUnit delayUnit) {
    Log.sysLog("ZDF shutdown pool...");
    try {
      try {
        forkJoinPool.shutdown();
        if (!forkJoinPool.awaitTermination(delay, delayUnit)) {
          forkJoinPool.shutdownNow();
          if (!forkJoinPool.awaitTermination(delay, delayUnit)) {
            Log.sysLog("ZDF: Pool nicht beendet");
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

  @SuppressWarnings("serial")
  private class VideoDtoDatenFilmConverterAction extends RecursiveAction {

    private final VideoDTO video;

    public VideoDtoDatenFilmConverterAction(VideoDTO aVideoDTO) {
      video = aVideoDTO;
      phaser.register();
    }

    @Override
    protected void compute() {
      if (video != null) {
        try {
          DownloadDTO download = video.getDownloadDto();

          for (String language : download.getLanguages()) {
            addFilm(download, language);
          }

        } catch (Exception ex) {
          Log.errorLog(496583211, ex, "add film failed: " + video.getWebsiteUrl());
        }
      }
      phaser.arriveAndDeregister();
    }

    private void addFilm(DownloadDTO download, String language) {

      final String title = determineTitle(video.getTitle(), language);

      DatenFilm film = new ZdfDatenFilm(SENDERNAME, video.getTopic(), video.getWebsiteUrl() /*urlThema*/,
              title, download.getUrl(language, Qualities.NORMAL), "" /*urlRtmp*/,
              video.getDate(), video.getTime(), video.getDuration(), video.getDescription());
      urlTauschen(film, video.getWebsiteUrl(), mlibFilmeSuchen);

      //don´t use addFilm here
      if (mlibFilmeSuchen.listeFilmeNeu.addFilmVomSender(film)) {
        // dann ist er neu
        FilmeSuchen.listeSenderLaufen.inc(film.arr[DatenFilm.FILM_SENDER], RunSender.Count.FILME);
      }

      if (!download.getUrl(language, Qualities.HD).isEmpty()) {
        CrawlerTool.addUrlHd(film, download.getUrl(language, Qualities.HD), "");
      }
      if (!download.getUrl(language, Qualities.SMALL).isEmpty()) {
        CrawlerTool.addUrlKlein(film, download.getUrl(language, Qualities.SMALL), "");
      }
      if (!download.getSubTitleUrl().isEmpty()) {
        CrawlerTool.addUrlSubtitle(film, download.getSubTitleUrl());
      }
      if (download.getGeoLocation() != GeoLocations.GEO_NONE) {
        film.arr[DatenFilm.FILM_GEO] = download.getGeoLocation().getDescription();
      }
    }

    private String determineTitle(String title, String language) {
      switch (language) {
        case DownloadDTO.LANGUAGE_ENGLISH:
          return title + " (Englisch)";
        case DownloadDTO.LANGUAGE_GERMAN:
          return title;
        default:
          return title + "(" + language + ")";
      }
    }
  }

  private static void updateHdStatus(DatenFilm film, String urlSeite) {
    // manuell die Auflösung für HD setzen, 2 Versuche
    updateHd("1456k_p13v12.mp4", "3328k_p36v12.mp4", film, urlSeite);
    updateHd("2256k_p14v12.mp4", "3328k_p36v12.mp4", film, urlSeite);
    updateHd("2328k_p35v12.mp4", "3328k_p36v12.mp4", film, urlSeite);

    updateHd("1456k_p13v12.mp4", "3256k_p15v12.mp4", film, urlSeite);
    updateHd("2256k_p14v12.mp4", "3256k_p15v12.mp4", film, urlSeite);
    updateHd("2328k_p35v12.mp4", "3256k_p15v12.mp4", film, urlSeite);

    updateHd("1496k_p13v13.mp4", "3296k_p15v13.mp4", film, urlSeite);
    updateHd("2296k_p14v13.mp4", "3296k_p15v13.mp4", film, urlSeite);
    updateHd("2328k_p35v13.mp4", "3296k_p15v13.mp4", film, urlSeite);

    updateHd("1496k_p13v13.mp4", "3328k_p36v13.mp4", film, urlSeite);
    updateHd("2296k_p14v13.mp4", "3328k_p36v13.mp4", film, urlSeite);
    updateHd("2328k_p35v13.mp4", "3328k_p36v13.mp4", film, urlSeite);
  }

  private static void modifyUrl(DatenFilm film, String urlSeite, FilmeSuchen mSFilmeSuchen) {
    //große URL verbessern
    changeUrl("2256k_p14v11.mp4", "2328k_p35v11.mp4", film, urlSeite, mSFilmeSuchen);
    changeUrl("2256k_p14v12.mp4", "2328k_p35v12.mp4", film, urlSeite, mSFilmeSuchen);
    changeUrl("2296k_p14v13.mp4", "2328k_p35v13.mp4", film, urlSeite, mSFilmeSuchen);

    //klein nach groß
    changeUrl("1456k_p13v11.mp4", "2328k_p35v11.mp4", film, urlSeite, mSFilmeSuchen);
    changeUrl("1456k_p13v11.mp4", "2256k_p14v11.mp4", film, urlSeite, mSFilmeSuchen); //wenns nicht geht, dann vielleicht so

    changeUrl("1456k_p13v12.mp4", "2328k_p35v12.mp4", film, urlSeite, mSFilmeSuchen);
    changeUrl("1456k_p13v12.mp4", "2256k_p14v12.mp4", film, urlSeite, mSFilmeSuchen); //wenns nicht geht, dann vielleicht so

    changeUrl("1496k_p13v13.mp4", "2328k_p35v13.mp4", film, urlSeite, mSFilmeSuchen);
    changeUrl("1496k_p13v13.mp4", "2296k_p14v13.mp4", film, urlSeite, mSFilmeSuchen); //wenns nicht geht, dann vielleicht so
  }

  public static void urlTauschen(DatenFilm film, String urlSeite, FilmeSuchen mSFilmeSuchen) {
    modifyUrl(film, urlSeite, mSFilmeSuchen);
    updateHdStatus(film, urlSeite);
  }

  private static void changeUrl(String from, String to, DatenFilm film, String urlSeite, FilmeSuchen mSFilmeSuchen) {
    if (film.arr[DatenFilm.FILM_URL].endsWith(from)) {
      String url = film.arr[DatenFilm.FILM_URL].substring(0, film.arr[DatenFilm.FILM_URL].lastIndexOf(from)) + to;
      String l = mSFilmeSuchen.listeFilmeAlt.getFileSizeUrl(url);
      // zum Testen immer machen!!
      if (!l.isEmpty()) {
        film.arr[DatenFilm.FILM_GROESSE] = l;
        film.arr[DatenFilm.FILM_URL] = url;
      } else if (urlExists(url)) {
        // dann wars wohl nur ein "403er"
        film.arr[DatenFilm.FILM_URL] = url;
      } else {
        Log.errorLog(945120369, "urlTauschen: " + urlSeite);
      }
    }
  }

  private static void updateHd(String from, String to, DatenFilm film, String urlSeite) {
    if (film.arr[DatenFilm.FILM_URL_HD].isEmpty() && film.arr[DatenFilm.FILM_URL].endsWith(from)) {
      String url = film.arr[DatenFilm.FILM_URL].substring(0, film.arr[DatenFilm.FILM_URL].lastIndexOf(from)) + to;
      // zum Testen immer machen!!
      if (urlExists(url)) {
        CrawlerTool.addUrlHd(film, url, "");
      } else {
        Log.errorLog(945120147, "urlTauschen: " + urlSeite);
      }
    }
  }
}
