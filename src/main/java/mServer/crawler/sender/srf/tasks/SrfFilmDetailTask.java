package mServer.crawler.sender.srf.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.DatenFilm;
import mServer.crawler.sender.srf.parser.SrfFilmJsonDeserializer;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractRestTask;
import mServer.crawler.sender.base.AbstractUrlTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SrfFilmDetailTask extends AbstractRestTask<DatenFilm, CrawlerUrlDTO> {

  private static final Logger LOG = LogManager.getLogger(SrfFilmDetailTask.class);

  public SrfFilmDetailTask(MediathekReader aCrawler, ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    super(aCrawler, aURLsToCrawl, Optional.empty());
  }

  @Override
  protected void processRestTarget(CrawlerUrlDTO aDTO, WebTarget aTarget) {
    Invocation.Builder request = aTarget.request();
    final Response response = request.header(HEADER_ACCEPT_ENCODING, ENCODING_GZIP).get();

    switch (response.getStatus()) {
      case 200:
      case 203:
        parseFilm(response, aTarget.getUri());
        break;
      case 403:
        // Geo-Blocking für Crawler-Standort
        LOG.error("SrfFilmDetailTask: Not authorized to access url " + aTarget.getUri().toString());
        break;
      default:
        LOG.error("SrfFilmDetailTask: Error reading url " + aTarget.getUri().toString() + ": " + response.getStatus());
    }
  }

  @Override
  protected AbstractUrlTask<DatenFilm, CrawlerUrlDTO> createNewOwnInstance(ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new SrfFilmDetailTask(crawler, aURLsToCrawl);
  }

  private void parseFilm(Response response, URI uri) {
    final String jsonOutput = response.readEntity(String.class);

    try {
      Type type = new TypeToken<Optional<DatenFilm>>() {
      }.getType();
      final Gson gson = new GsonBuilder().registerTypeAdapter(type, new SrfFilmJsonDeserializer()).create();

      Optional<DatenFilm> film = gson.fromJson(jsonOutput, type);
      if (film.isPresent()) {
        taskResults.add(film.get());
      }
    } catch (JsonSyntaxException e) {
      LOG.error("SrfFilmDetailTask: Error reading url " + uri.toString(), e);
    }
  }
}
