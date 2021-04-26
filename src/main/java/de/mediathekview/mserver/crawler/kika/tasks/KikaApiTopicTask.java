package de.mediathekview.mserver.crawler.kika.tasks;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractJsonRestTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.kika.json.KikaApiFilmDto;
import de.mediathekview.mserver.crawler.kika.json.KikaApiTopicDto;
import de.mediathekview.mserver.crawler.kika.json.KikaApiTopicPageDeserializer;

// <T, R, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D>
// return T Class from this task, desirialisation of class R , D , Reasearch in this url
public class KikaApiTopicTask extends AbstractJsonRestTask<KikaApiFilmDto, KikaApiTopicDto, TopicUrlDTO> {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(KikaApiTopicTask.class);
  private int subPageIndex = 0;

  public KikaApiTopicTask(AbstractCrawler crawler, Queue<TopicUrlDTO> urlToCrawlDTOs, int subPageIndex) {
    super(crawler, urlToCrawlDTOs, null);
    this.subPageIndex = subPageIndex;
  }

  @Override
  protected JsonDeserializer<KikaApiTopicDto> getParser(TopicUrlDTO aDTO) {
    return new KikaApiTopicPageDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<Set<KikaApiTopicDto>>() {}.getType();
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
  protected void postProcessing(KikaApiTopicDto aResponseObj, TopicUrlDTO aDTO) {
    //
    if (aResponseObj.getErrorCode().isPresent()) {
      LOG.error("Error {} : {} for target {} ", aResponseObj.getErrorCode().get(), aResponseObj.getErrorMesssage().get(), aDTO.getUrl());
      crawler.incrementAndGetErrorCount();
      return;
    }
    //
    final Optional<AbstractRecursiveConverterTask<KikaApiFilmDto, TopicUrlDTO>> subpageCrawler;
    final Optional<TopicUrlDTO> nextPageLink = aResponseObj.getNextPage();
    //
    if (nextPageLink.isPresent() && config.getMaximumSubpages() > subPageIndex) {
      final Queue<TopicUrlDTO> nextPageLinks = new ConcurrentLinkedQueue<>();
      nextPageLinks.add(new TopicUrlDTO(aDTO.getTopic(), nextPageLink.get().getUrl())); // repack next page link to keep topic name
      subpageCrawler = Optional.of(createNewOwnInstance(nextPageLinks));
      subpageCrawler.get().fork();
    } else {
      subpageCrawler = Optional.empty();
    }
    for (KikaApiFilmDto aFilm : aResponseObj.getElements()) {
      if (aFilm.getTopic().isEmpty()) {
        aFilm.setTopic(Optional.of(aDTO.getTopic()));
      }
      taskResults.add(aFilm);
    }
    //
    subpageCrawler.ifPresent(nextPageCrawler -> taskResults.addAll(nextPageCrawler.join()));    
  }

  @Override
  protected AbstractRecursiveConverterTask<KikaApiFilmDto, TopicUrlDTO> createNewOwnInstance(
      Queue<TopicUrlDTO> aElementsToProcess) {
    return new KikaApiTopicTask(crawler, aElementsToProcess, subPageIndex+1);
  }

  
}
