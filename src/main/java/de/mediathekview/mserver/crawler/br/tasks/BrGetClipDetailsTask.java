package de.mediathekview.mserver.crawler.br.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.communication.WebAccessHelper;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.base.config.CrawlerUrlType;
import de.mediathekview.mserver.base.config.MServerBasicConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.br.BrGraphQLQueries;
import de.mediathekview.mserver.crawler.br.data.BrID;
import de.mediathekview.mserver.crawler.br.json.BrClipDetailsDeserializer;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BrGetClipDetailsTask extends RecursiveTask<Set<Film>> {

  private static final int MAX_RETRIES = 5;
  private static final long serialVersionUID = 5682617879894452978L;
  private static final Logger LOG = LogManager.getLogger(BrGetClipDetailsTask.class);

  private final transient AbstractCrawler crawler;
  private final ConcurrentLinkedQueue<BrID> clipQueue;
  private final transient Set<Film> convertedFilms;
  private final transient MServerBasicConfigDTO config;

  public BrGetClipDetailsTask(
      final AbstractCrawler crawler, final ConcurrentLinkedQueue<BrID> clipQueue) {
    this.crawler = crawler;
    this.clipQueue = clipQueue;
    this.config = MServerConfigManager.getInstance().getSenderConfig(crawler.getSender());
    this.convertedFilms = ConcurrentHashMap.newKeySet();
  }

  private ConcurrentLinkedQueue<BrID> consumeHalfQueueToReturningOne(
      final ConcurrentLinkedQueue<BrID> fullQueue) {
    final int halfSize = fullQueue.size() / 2;
    final ConcurrentLinkedQueue<BrID> urlsToCrawlSubset = new ConcurrentLinkedQueue<>();
    for (int i = 0; i < halfSize; i++) {
      urlsToCrawlSubset.offer(clipQueue.poll());
    }
    return urlsToCrawlSubset;
  }

  private void filmIdsToFilms(final ConcurrentLinkedQueue<BrID> clipListe) {
    for (final BrID singleClipId : clipListe) {
      getClipDetailsForClipId(singleClipId);
    }
  }

  private void getClipDetailsForClipId(final BrID singleClipId) {

    BrWebAccessHelper.handleWebAccessExecution(
        LOG,
        crawler,
        () -> {
          try {
            final Type optionalFilmType = new TypeToken<Optional<Film>>() {}.getType();
            final Gson gson =
                new GsonBuilder()
                    .registerTypeAdapter(
                        optionalFilmType, new BrClipDetailsDeserializer(crawler, singleClipId))
                    .create();

            final Optional<URL> apiUrl =
                crawler.getRuntimeConfig().getSingleCrawlerURL(CrawlerUrlType.BR_API_URL);
            if (apiUrl.isPresent()) {
              final String response =
                  WebAccessHelper.getJsonResultFromPostAccess(
                      apiUrl.get(),
                      BrGraphQLQueries.getQuery2GetClipDetails(singleClipId),
                      config.getSocketTimeoutInSeconds());

              if (!tryBrApiCall(gson, response, optionalFilmType)) {
                crawler.incrementAndGetErrorCount();
                crawler.updateProgress();
              }

            } else {
              crawler.printErrorMessage();
              LOG.error("The BR Api URL wasn't set right.");
            }
          } catch (Exception e) {
            LOG.error("BR Error: ", e);
          }
        });
  }

  private boolean tryBrApiCall(
      final Gson gson, final String response, final Type optionalFilmType) {
    int countRetries = 0;
    do {
      try {
        final Optional<Film> film = gson.fromJson(response, optionalFilmType);
        if (film.isPresent()) {
          convertedFilms.add(film.get());
          crawler.incrementAndGetActualCount();
          crawler.updateProgress();
          return true;
        }
        break;
      } catch (final IllegalStateException ise) {
        try {
          Thread.sleep(259);
        } catch (final InterruptedException interruptedException) {
          LOG.error("The BR API retires had been interrupted.", interruptedException);
          Thread.currentThread().interrupt();
        }
        countRetries++;
      }
    } while (countRetries < MAX_RETRIES);
    return false;
  }

  @Override
  protected Set<Film> compute() {
    if (clipQueue.size() <= config.getMaximumUrlsPerTask()) {
      filmIdsToFilms(clipQueue);
    } else {
      final BrGetClipDetailsTask rightTask =
          new BrGetClipDetailsTask(crawler, consumeHalfQueueToReturningOne(clipQueue));
      final BrGetClipDetailsTask leftTask = new BrGetClipDetailsTask(crawler, clipQueue);
      leftTask.fork();
      convertedFilms.addAll(rightTask.compute());
      convertedFilms.addAll(leftTask.join());
    }
    return convertedFilms;
  }
}
