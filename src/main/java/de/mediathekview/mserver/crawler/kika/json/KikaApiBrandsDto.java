package de.mediathekview.mserver.crawler.kika.json;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;

public class KikaApiBrandsDto {
  private Optional<String> errorMesssage = Optional.empty();
  private Optional<String> errorCode = Optional.empty();
  private Optional<CrawlerUrlDTO> nextUrl = Optional.empty();
  private Set<TopicUrlDTO> elements = new HashSet<TopicUrlDTO>();
  
  public void add(TopicUrlDTO aTopicUrlDTO) {
    elements.add(aTopicUrlDTO);
  }
  
  public Set<TopicUrlDTO> getElements() {
    return elements;
  }
  
  public Optional<CrawlerUrlDTO> getNextPage() {
    return nextUrl;
  }
  
  public void setNextPage(CrawlerUrlDTO aCrawlerUrlDTO) {
    nextUrl = Optional.of(aCrawlerUrlDTO);
  }

  public void setError(Optional<String> aErrorCode, Optional<String> aErrorMesssage) {
    errorCode = aErrorCode;
    errorMesssage = aErrorMesssage;
  }
  
  public Optional<String> getErrorMesssage() {
    return errorMesssage;
  }

  public Optional<String> getErrorCode() {
    return errorCode;
  }

}
