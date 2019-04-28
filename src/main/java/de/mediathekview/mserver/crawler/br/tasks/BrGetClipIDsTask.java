/*
 * BrGetClipIDsTask.java
 *
 * Projekt    : MServer
 * erstellt am: 12.12.2017
 * Autor      : Sascha
 *
 */
package de.mediathekview.mserver.crawler.br.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mlib.communication.WebAccessHelper;
import de.mediathekview.mserver.base.config.CrawlerUrlType;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.br.BrGraphQLQueries;
import de.mediathekview.mserver.crawler.br.data.BrClipCollectIDResult;
import de.mediathekview.mserver.crawler.br.data.BrID;
import de.mediathekview.mserver.crawler.br.json.BrClipIdsDeserializer;
import de.mediathekview.mserver.crawler.br.json.BrIdsDTO;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BrGetClipIDsTask implements Callable<Set<BrID>> {

  private static final Logger LOG = LogManager.getLogger(BrGetClipIDsTask.class);
  private static final int DEFAULT_NUMBER_IDS_PER_REQUEST = 10000;

  private final AbstractCrawler crawler;
  private BrClipCollectIDResult idCollectResult;

  public BrGetClipIDsTask(final AbstractCrawler crawler) {
    this.crawler = crawler;
  }

  @Override
  public Set<BrID> call() {

    idCollectResult = new BrClipCollectIDResult();
    idCollectResult.setClipList(new BrIdsDTO());

    ZonedDateTime minimumAvailableUntil = ZonedDateTime.now();

    BrWebAccessHelper.handleWebAccessExecution(
        LOG,
        crawler,
        () -> {
          final Gson gson =
              new GsonBuilder()
                  .registerTypeAdapter(
                      BrClipCollectIDResult.class,
                      new BrClipIdsDeserializer(crawler, idCollectResult))
                  .create();

          int startingRequestSize = DEFAULT_NUMBER_IDS_PER_REQUEST;

          do {
            final Optional<URL> apiUrl =
                crawler.getRuntimeConfig().getSingleCrawlerURL(CrawlerUrlType.BR_API_URL);
            if (apiUrl.isPresent()) {
              do {

                final String response =
                    WebAccessHelper.getJsonResultFromPostAccess(
                        apiUrl.get(),
                        BrGraphQLQueries.getQuery2GetAllClipIds(
                            minimumAvailableUntil,
                            startingRequestSize,
                            idCollectResult.getCursor()),
                        crawler.getCrawlerConfig().getSocketTimeoutInSeconds());

                idCollectResult = gson.fromJson(response, BrClipCollectIDResult.class);

                LOG.debug(
                    "Zwischenstand: "
                        + idCollectResult.getClipList().getIds().size()
                        + " Cursor: "
                        + idCollectResult.getCursor());

                if (idCollectResult.getClipList().getIds().size()
                    >= idCollectResult.getResultSize()) {
                  break;
                }

              } while (idCollectResult.hasNextPage());
            } else {
              crawler.printErrorMessage();
              LOG.error("The BR Api URL wasn't set right.");
            }
            idCollectResult.setCursor(null);

            startingRequestSize = startingRequestSize * 4 / 5; // 80%

            LOG.debug("Anzahl Elemente gemeldet:" + idCollectResult.getResultSize());

          } while (idCollectResult.getClipList().getIds().size() < idCollectResult.getResultSize());
        });

    final Set<BrID> result = idCollectResult.getClipList().getIds();

    LOG.debug("Elemente gefunden: " + result.size());

    return result;
  }
}
