package de.mediathekview.mserver.crawler.livestream.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractJsonRestTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.livestream.json.LivestreamArdStreamDeserializer;

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

public class LivestreamArdStreamTask extends AbstractJsonRestTask<TopicUrlDTO, Set<CrawlerUrlDTO>, TopicUrlDTO> {
  private static final long serialVersionUID = -1203126035944523033L;
  private static final Logger LOG = LogManager.getLogger(LivestreamArdStreamTask.class);
  
  public LivestreamArdStreamTask(AbstractCrawler crawler, Queue<TopicUrlDTO> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs, null);
  }

  @Override
  protected JsonDeserializer<Set<CrawlerUrlDTO>> getParser(TopicUrlDTO aDTO) {
    return new LivestreamArdStreamDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<CrawlerUrlDTO>(){}.getType();
  }

  @Override
  protected void postProcessing(Set<CrawlerUrlDTO> aResponseObj, TopicUrlDTO aDTO) {
    aResponseObj.forEach(url -> {
      taskResults.add(new TopicUrlDTO(aDTO.getTopic(), url.getUrl()));  
    });
    
  }

  @Override
  protected AbstractRecursiveConverterTask<TopicUrlDTO, TopicUrlDTO> createNewOwnInstance(
      Queue<TopicUrlDTO> aElementsToProcess) {
    return new LivestreamArdStreamTask(crawler, aElementsToProcess);
  }

  @Override
  protected void handleHttpError(TopicUrlDTO dto, URI url, Response response) {
    crawler.printErrorMessage();
    LOG.fatal(
        "A HTTP error {} occurred when getting REST information from: \"{}\".",
        response.getStatus(),
        url);
  }





}
