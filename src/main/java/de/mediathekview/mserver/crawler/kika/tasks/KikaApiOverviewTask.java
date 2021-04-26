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
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.kika.json.KikaApiBrandsDto;
import de.mediathekview.mserver.crawler.kika.json.KikaApiOverviewPageDeserializer;

// <T, R, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D>
// return T Class from this task, desirialisation out class R , D , Reasearch in this url
public class KikaApiOverviewTask extends AbstractJsonRestTask<TopicUrlDTO, KikaApiBrandsDto, CrawlerUrlDTO> {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(KikaApiOverviewTask.class);
  private int subPageIndex;

  public KikaApiOverviewTask(AbstractCrawler crawler, Queue<CrawlerUrlDTO> urlToCrawlDTOs, int subPageIndex) {
    super(crawler, urlToCrawlDTOs, null);
    this.subPageIndex = subPageIndex;
  }

  @Override
  protected JsonDeserializer<KikaApiBrandsDto> getParser(CrawlerUrlDTO aDTO) {
    return new KikaApiOverviewPageDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<Set<KikaApiBrandsDto>>() {}.getType();
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
  protected void postProcessing(KikaApiBrandsDto aResponseObj, CrawlerUrlDTO aDTO) {
    //
    if (aResponseObj.getErrorCode().isPresent()) {
      LOG.error("Error {} : {} for target {} ", aResponseObj.getErrorCode().get(), aResponseObj.getErrorMesssage().get(), aDTO.getUrl());
      crawler.incrementAndGetErrorCount();
      return;
    }
    //
    final Optional<CrawlerUrlDTO> nextPageLink = aResponseObj.getNextPage();
    Optional<AbstractRecursiveConverterTask<TopicUrlDTO, CrawlerUrlDTO>> subpageCrawler = Optional.empty();
    //
    if (nextPageLink.isPresent() && config.getMaximumSubpages() > subPageIndex) {
      final Queue<CrawlerUrlDTO> nextPageLinks = new ConcurrentLinkedQueue<>();
      nextPageLinks.add(nextPageLink.get());
      subpageCrawler = Optional.of(createNewOwnInstance(nextPageLinks));
      subpageCrawler.get().fork();
    }
    //
    taskResults.addAll(aResponseObj.getElements());
    //
    subpageCrawler.ifPresent(sTask -> taskResults.addAll(sTask.join()));    
    //
  }

  @Override
  protected AbstractRecursiveConverterTask<TopicUrlDTO, CrawlerUrlDTO> createNewOwnInstance(Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new KikaApiOverviewTask(crawler, aElementsToProcess, subPageIndex+1);
  }
  
}
