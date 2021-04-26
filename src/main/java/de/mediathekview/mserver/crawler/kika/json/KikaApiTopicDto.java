package de.mediathekview.mserver.crawler.kika.json;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;

public class KikaApiTopicDto {
  private Optional<String> errorMesssage = Optional.empty();
  private Optional<String> errorCode = Optional.empty();
  private Optional<TopicUrlDTO> nextUrl = Optional.empty();
  private Set<KikaApiFilmDto> elements = new HashSet<KikaApiFilmDto>();
  
  public void add(KikaApiFilmDto aKikaApiFilmDto) {
    elements.add(aKikaApiFilmDto);
  }
  
  public Set<KikaApiFilmDto> getElements() {
    return elements;
  }
  
  public Optional<TopicUrlDTO> getNextPage() {
    return nextUrl;
  }
  
  public void setNextPage(TopicUrlDTO aCrawlerUrlDTO) {
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
