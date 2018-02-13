package de.mediathekview.mserver.crawler.wdr.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.wdr.parser.WdrFilmDeserializer;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

public class WdrFilmDetailTask extends AbstractDocumentTask<Film, TopicUrlDTO>  {

  private static final Logger LOG = LogManager.getLogger(WdrFilmDetailTask.class);
  
  public WdrFilmDetailTask(AbstractCrawler aCrawler, ConcurrentLinkedQueue<TopicUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);    
  }

  @Override
  protected void processDocument(TopicUrlDTO aUrlDTO, Document aDocument) {
    WdrFilmDeserializer deserializer = new WdrFilmDeserializer(crawler, getProtocol(aUrlDTO));
    
    Optional<Film> film = deserializer.deserialize(aUrlDTO, aDocument);
    if (film.isPresent()) {
      taskResults.add(film.get());
    } else {
      // TODO???
    }
  }

  @Override
  protected AbstractUrlTask<Film, TopicUrlDTO> createNewOwnInstance(ConcurrentLinkedQueue<TopicUrlDTO> aURLsToCrawl) {
    return new WdrFilmDetailTask(crawler, aURLsToCrawl);
  }  
  
  private String getProtocol(TopicUrlDTO aUrlDTO) {
    String protocol = "https:";
    
    Optional<String> usedProtocol = UrlUtils.getProtocol(aUrlDTO.getUrl());
    if (usedProtocol.isPresent()) {
      protocol = usedProtocol.get();
    }
    
    return protocol;
  }
  

}
