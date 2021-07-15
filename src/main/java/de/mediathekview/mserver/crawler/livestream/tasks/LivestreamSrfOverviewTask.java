package de.mediathekview.mserver.crawler.livestream.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractJsonRestTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.livestream.json.LivestreamSrfDeserializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializer;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Queue;
import java.util.Set;

import javax.ws.rs.core.Response;

//<T, R, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D>
//return T Class from this task, desirialisation of class R , D , Reasearch in this url

public class LivestreamSrfOverviewTask extends AbstractJsonRestTask<TopicUrlDTO, Set<TopicUrlDTO>, CrawlerUrlDTO> {
  private static final long serialVersionUID = -1203126035944523033L;
  private static final Logger LOG = LogManager.getLogger(LivestreamSrfOverviewTask.class);
  
  public LivestreamSrfOverviewTask(AbstractCrawler crawler, Queue<CrawlerUrlDTO> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs, null);
  }

  @Override
  protected JsonDeserializer<Set<TopicUrlDTO>> getParser(CrawlerUrlDTO aDTO) {
    return new LivestreamSrfDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<TopicUrlDTO>(){}.getType();
  }

  @Override
  protected void handleHttpError(CrawlerUrlDTO dto, URI url, Response response) {
    crawler.printErrorMessage();
    LOG.fatal(
        "A HTTP error {} occurred when getting REST information from: \"{}\".",
        response.getStatus(),
        url);
    
  }



  @Override
  protected AbstractRecursiveConverterTask<TopicUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new LivestreamSrfOverviewTask(crawler, aElementsToProcess);
  }

  @Override
  protected void postProcessing(Set<TopicUrlDTO> aResponseObj, CrawlerUrlDTO aDTO) {
    taskResults.addAll(aResponseObj);
    
  }



}
