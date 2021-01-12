package mServer.crawler.sender.ard.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.tool.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.ard.json.ArdErrorDeserializer;
import mServer.crawler.sender.ard.json.ArdErrorInfoDto;
import mServer.crawler.sender.base.AbstractRestTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;

public abstract class ArdTaskBase<T, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D> {

  private static final String HEADER_AUTHENTIFICATION = "Api-Auth";

  private static final Logger LOG = LogManager.getLogger(ArdTaskBase.class);

  private static final Type OPTIONAL_ERROR_DTO
          = new TypeToken<Optional<ArdErrorInfoDto>>() {
          }.getType();

  private final GsonBuilder gsonBuilder;

  public ArdTaskBase(
          final MediathekReader aCrawler, final ConcurrentLinkedQueue<D> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos, Optional.empty());
    gsonBuilder = new GsonBuilder();
    registerJsonDeserializer(OPTIONAL_ERROR_DTO, new ArdErrorDeserializer());
  }

  protected void registerJsonDeserializer(final Type aType, final Object aDeserializer) {
    gsonBuilder.registerTypeAdapter(aType, aDeserializer);
  }

  protected <A> Optional<A> deserializeOptional(final WebTarget target, final Type type) {
    return this.<Optional<A>>deserializeUnsafe(target, type).orElse(Optional.empty());
  }

  private <A> Optional<A> deserializeUnsafe(final WebTarget target, final Type type) {
    final Gson gson = gsonBuilder.create();
    final Response response = executeRequest(target);
    traceRequest(response.getLength());
    if (response.getStatus() == 200) {
      final String jsonOutput = response.readEntity(String.class);
      if (isSuccessResponse(jsonOutput, gson, target.getUri().toString())) {
        return Optional.of(gson.fromJson(jsonOutput, type));
      }
    } else {
      final String logText = "ArdTaskBase: request of url "
              + target.getUri().toString()
              + " failed: "
              + response.getStatus();
      Log.errorLog(23646387, logText);
      LOG.warn(logText);
    }
    return Optional.empty();
  }

  protected <A> A deserialize(final WebTarget target, final Type type) {
    return this.<A>deserializeUnsafe(target, type).orElse(null);
  }

  private boolean isSuccessResponse(
          final String jsonOutput, final Gson gson, final String targetUrl) {
    final Optional<ArdErrorInfoDto> error = gson.fromJson(jsonOutput, OPTIONAL_ERROR_DTO);
    error.ifPresent(
            ardErrorInfoDto
            -> {
      final String logText
              = "ArdTaskBase: request of url "
              + targetUrl
              + " contains error: "
              + ardErrorInfoDto.getCode()
              + ", "
              + ardErrorInfoDto.getMessage();
      Log.errorLog(238423092, logText);
      LOG.error(logText);
    });

    return !error.isPresent();
  }

  private Response executeRequest(final WebTarget aTarget) {
    Builder request = aTarget.request();
    if (authKey.isPresent()) {
      request
              = request.header(
                      HEADER_AUTHENTIFICATION, AUTHORIZATION_BEARER + authKey.get());
    }

    return request
            // .header(HEADER_ACCEPT_ENCODING, ENCODING_GZIP)
            .header(AbstractRestTask.HEADER_ACCEPT, AbstractRestTask.APPLICATION_JSON)
            .header(AbstractRestTask.HEADER_CONTENT_TYPE, AbstractRestTask.APPLICATION_JSON)
            .get();
  }
}
