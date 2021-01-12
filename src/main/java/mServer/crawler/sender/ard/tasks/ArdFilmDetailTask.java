package mServer.crawler.sender.ard.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import jakarta.ws.rs.client.WebTarget;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.ard.ArdConstants;
import mServer.crawler.sender.ard.ArdFilmDto;
import mServer.crawler.sender.ard.ArdFilmInfoDto;
import mServer.crawler.sender.ard.json.ArdFilmDeserializer;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArdFilmDetailTask extends ArdTaskBase<DatenFilm, ArdFilmInfoDto> {

  private static final Logger LOG = LogManager.getLogger(ArdFilmDetailTask.class);

  private static final Type LIST_FILM_TYPE_TOKEN = new TypeToken<List<ArdFilmDto>>() {
  }.getType();

  public ArdFilmDetailTask(MediathekReader aCrawler, ConcurrentLinkedQueue aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);

    registerJsonDeserializer(LIST_FILM_TYPE_TOKEN, new ArdFilmDeserializer());
  }

  @Override
  protected void processRestTarget(ArdFilmInfoDto aDTO, WebTarget aTarget) {
    if (Config.getStop()) {
      return;
    }

    try {
      final List<ArdFilmDto> filmDtos = deserialize(aTarget, LIST_FILM_TYPE_TOKEN);

      if (filmDtos != null && filmDtos.size() > 0) {
        for (ArdFilmDto filmDto : filmDtos) {

          final DatenFilm result = filmDto.getFilm();
          result.arr[DatenFilm.FILM_WEBSEITE] = getWebsiteUrl(aDTO);
          taskResults.add(result);

          if (aDTO.getNumberOfClips() > 1) {
            processRelatedFilms(filmDto.getRelatedFilms());
          }
        }
      } else {
        LOG.debug("no film: " + aDTO.getUrl());
      }
    } catch (Exception e) {
      LOG.error("exception: " + aDTO.getUrl(), e);
      Log.errorLog(453455465, e);
    }
  }

  private void processRelatedFilms(final Set<ArdFilmInfoDto> relatedFilms) {
    if (relatedFilms != null && !relatedFilms.isEmpty()) {
      ConcurrentLinkedQueue<ArdFilmInfoDto> queue = new ConcurrentLinkedQueue<>(relatedFilms);
      ArdFilmDetailTask task = (ArdFilmDetailTask) createNewOwnInstance(queue);
      task.fork();
      taskResults.addAll(task.join());
    }
  }

  private String getWebsiteUrl(final ArdFilmInfoDto aDTO) {
    return String.format(ArdConstants.WEBSITE_URL, aDTO.getId());
  }

  @Override
  protected AbstractRecursivConverterTask createNewOwnInstance(
          ConcurrentLinkedQueue aElementsToProcess) {
    return new ArdFilmDetailTask(crawler, aElementsToProcess);
  }
}
