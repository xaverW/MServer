package de.mediathekview.mserver.crawler.kika.tasks;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.tool.FileSizeDeterminer;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractJsonRestTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.kika.json.KikaApiFilmDto;
import de.mediathekview.mserver.crawler.kika.json.KikaApiVideoInfoDto;
import de.mediathekview.mserver.crawler.kika.json.KikaApiVideoInfoPageDeserializer;

// <T, R, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D>
// return T Class from this task, desirialisation of class R , D , Reasearch in this url
public class KikaApiFilmTask extends AbstractJsonRestTask<Film, KikaApiVideoInfoDto, KikaApiFilmDto> {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(KikaApiFilmTask.class);

  public KikaApiFilmTask(AbstractCrawler crawler, Queue<KikaApiFilmDto> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs, null);
  }

  @Override
  protected JsonDeserializer<KikaApiVideoInfoDto> getParser(KikaApiFilmDto aDTO) {
    return new KikaApiVideoInfoPageDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<Set<KikaApiVideoInfoDto>>() {}.getType();
  }

  @Override
  protected void handleHttpError(URI url, Response response) {
    crawler.printErrorMessage();
    LOG.fatal(
        "A HTTP error {} occurred when getting REST information from: \"{}\".",
        response.getStatus(),
        url);
    
  }

  @Override
  protected void postProcessing(KikaApiVideoInfoDto aResponseObj, KikaApiFilmDto aDTO) {
    //
    if (aResponseObj.getErrorCode().isPresent()) {
      LOG.error("Error {} : {} for target {} ", aResponseObj.getErrorCode().get(), aResponseObj.getErrorMesssage().get(), aDTO.getUrl());
      crawler.incrementAndGetErrorCount();
      return;
    }
    //
    if (aResponseObj.getVideoUrls().size() == 0) {
      LOG.error("No VideoUrls for {}", aDTO.getUrl());
      crawler.incrementAndGetErrorCount();
      return;
    }
    //
    if (aDTO.getTitle().isEmpty() || aDTO.getTopic().isEmpty() || aDTO.getDate().isEmpty() || aDTO.getDuration().isEmpty()) {
      LOG.error("Missing topic, title, date or duration for {}", aDTO.getUrl());
      crawler.incrementAndGetErrorCount();
      return;
    }
    //
    Film aFilm = new Film(
        UUID.randomUUID(),
        crawler.getSender(),
        aDTO.getTitle().get(),
        aDTO.getTopic().get(),
        parseLocalDateTime(aDTO, aDTO.getDate()).get(),
        parseDuration(aDTO, aDTO.getDuration()).get()
        );
    if (aDTO.getDescription().isPresent()) {
      aFilm.setBeschreibung(aDTO.getDescription().get());
    }
    if (aDTO.getGeoProtection().isPresent()) {
      Optional<GeoLocations> geo = parseGeo(aDTO, aDTO.getGeoProtection());
      if (geo.isPresent()) {
        Collection<GeoLocations> collectionOfGeolocations = new ArrayList<GeoLocations>();
        collectionOfGeolocations.add(geo.get());
        aFilm.setGeoLocations(collectionOfGeolocations);
      }
    }
    if (aDTO.getWebsite().isPresent()) {
      try {
        aFilm.setWebsite(new URL(aDTO.getWebsite().get()));
      } catch (MalformedURLException e) {
        LOG.error("Invalid website url {} for {} error {}", aDTO.getWebsite().get(), aDTO.getUrl(), e);
      }
    }
    //
    for (Map.Entry<Resolution,String> element : aResponseObj.getVideoUrls().entrySet()) {
      try {
        final FileSizeDeterminer fsd = new FileSizeDeterminer(element.getValue());
        final FilmUrl filmUrl = new FilmUrl(element.getValue(), fsd.getFileSizeInMiB());
        aFilm.addUrl(element.getKey(), filmUrl);
      } catch (MalformedURLException e) {
        LOG.error("Invalid video url {} for {} error {}", element.getValue(), aDTO.getUrl(), e);
      }
    }
    //
    if (aResponseObj.hasSubtitle()) {
      for (String subtitleUrlAsString : aResponseObj.getSubtitle()) {
        try {
          aFilm.addSubtitle(new URL(UrlUtils.addProtocolIfMissing(subtitleUrlAsString, UrlUtils.PROTOCOL_HTTPS)));
        } catch (MalformedURLException e) {
          LOG.error("Invalid subtitle url {} for {} error {}", subtitleUrlAsString, aDTO.getUrl(), e);
        }
      }
      if (aResponseObj.getSubtitle().size() == 0) {
        LOG.error("Missing subtitle for {}", aDTO.getUrl());
      }
    }
    taskResults.add(aFilm);
    crawler.incrementAndGetActualCount();
    crawler.updateProgress();
  }

  @Override
  protected AbstractRecursiveConverterTask<Film, KikaApiFilmDto> createNewOwnInstance(
      Queue<KikaApiFilmDto> aElementsToProcess) {
    return new KikaApiFilmTask(crawler, aElementsToProcess);
  }

  ///////////////////////////////////////////////////////////////////////////////////

  public Optional<LocalDateTime> parseLocalDateTime(KikaApiFilmDto sourceUrl, Optional<String> text) {
    Optional<LocalDateTime> result = Optional.empty();
    if (text.isPresent()) {
      try {
        DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        result = Optional.of(LocalDateTime.parse(text.get().substring(0, 19), formatter));
      } catch (Exception e) {
        LOG.error("DateTimeFormatter failed for string {} url {} exception {}", text.get(), sourceUrl.getUrl(), e);
      }
    }
    return result;
  }
  //
  public Optional<Duration> parseDuration(KikaApiFilmDto sourceUrl, Optional<String> text) {
    Optional<Duration> result = Optional.empty();
    if (text.isPresent()) {
      try {
        int min = Integer.parseInt(text.get());
        result = Optional.of(Duration.ofSeconds(min));
      } catch (Exception e) {
        LOG.error("Parse duration failed for string {} url {} exception {}", text.get(), sourceUrl.getUrl(), e);
      }
    }
    return result;
  }
  //
  public Optional<GeoLocations> parseGeo(KikaApiFilmDto sourceUrl, Optional<String> text) {
    Optional<GeoLocations> result = Optional.empty();
    if (text.isPresent()) {
      if (text.get().equalsIgnoreCase("germany")) {
        return Optional.of(GeoLocations.GEO_DE);
      } else if (text.get().equalsIgnoreCase("worldwide")) {
        return  Optional.empty();
      } else {
        LOG.error("Unknow GeoLocations {} url {}", text.get(), sourceUrl.getUrl());
      }
    }
    return result;
  }
}
