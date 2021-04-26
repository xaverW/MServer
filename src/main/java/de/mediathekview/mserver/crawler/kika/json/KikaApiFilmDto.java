package de.mediathekview.mserver.crawler.kika.json;

import java.util.Optional;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

public class KikaApiFilmDto extends CrawlerUrlDTO {
  private Optional<String> errorMesssage = Optional.empty();
  private Optional<String> errorCode = Optional.empty();
  private Optional<String> topic = Optional.empty();
  private Optional<String> title = Optional.empty();
  private Optional<String> id = Optional.empty();
  private Optional<String> description = Optional.empty();
  private Optional<String> date = Optional.empty();
  private Optional<String> duration = Optional.empty();
  private Optional<String> geoProtection = Optional.empty();
  private Optional<String> website = Optional.empty();
  //
  private Optional<String> expirationDate = Optional.empty();
  private Optional<String> appearDate = Optional.empty();

  public KikaApiFilmDto(String aUrl, Optional<String> topic, Optional<String> title, Optional<String> id,
      Optional<String> description, Optional<String> date, Optional<String> duration,
      Optional<String> geoProtection, Optional<String> expirationDate, Optional<String> appearDate,
      Optional<String> website) {
    super(aUrl);
    this.topic = topic;
    this.title = title;
    this.id = id;
    this.expirationDate = expirationDate;
    this.appearDate = appearDate;
    this.description = description;
    this.date = date;
    this.duration = duration;
    this.geoProtection = geoProtection;
    this.website = website;
  }

  public void setTopic(Optional<String> aTopic) {
    topic = aTopic;
  }
  public Optional<String> getTopic() {
    return topic;
  }

  public Optional<String> getTitle() {
    return title;
  }

  public Optional<String> getId() {
    return id;
  }

  public Optional<String> getDescription() {
    return description;
  }

  public Optional<String> getDate() {
    return date;
  }

  public Optional<String> getDuration() {
    return duration;
  }

  public Optional<String> getGeoProtection() {
    return geoProtection;
  }

  public Optional<String> getExpirationDate() {
    return expirationDate;
  }

  public Optional<String> getAppearDate() {
    return appearDate;
  }

  public Optional<String> getWebsite() {
    return website;
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
