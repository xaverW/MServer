package mServer.crawler.sender.srf.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import mServer.crawler.sender.srf.parser.SrfSendungOverviewJsonDeserializer;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractRestTask;
import mServer.crawler.sender.base.AbstractUrlTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.SendungOverviewDto;
import mServer.crawler.sender.base.UrlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SrfSendungOverviewPageTask extends AbstractRestTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private static final Logger LOG = LogManager.getLogger(SrfSendungOverviewPageTask.class);

  private static final Type OPTIONAL_DTO_TYPE_TOKEN = new TypeToken<Optional<SendungOverviewDto>>() {
  }.getType();

  private final int pageNumber;
  private final int maxSubPages;

  public SrfSendungOverviewPageTask(MediathekReader aCrawler, ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    this(aCrawler, aUrlToCrawlDTOs, 1);
  }

  public SrfSendungOverviewPageTask(MediathekReader aCrawler, ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs, int aPageNumber) {
    super(aCrawler, aUrlToCrawlDTOs, Optional.empty());

    pageNumber = aPageNumber;

    if (CrawlerTool.loadLongMax()) {
      maxSubPages = 3;
    } else {
      maxSubPages = 1;
    }
  }

  @Override
  protected void processRestTarget(CrawlerUrlDTO aDTO, WebTarget aTarget) {
    String baseUrl = UrlUtils.getBaseUrl(aDTO.getUrl());

    final Gson gson = new GsonBuilder().registerTypeAdapter(OPTIONAL_DTO_TYPE_TOKEN, new SrfSendungOverviewJsonDeserializer(baseUrl)).create();
    Invocation.Builder request = aTarget.request();
    final Response response = request.header(HEADER_ACCEPT_ENCODING, ENCODING_GZIP).get();

    if (response.getStatus() == 200) {

      final String jsonOutput = response.readEntity(String.class);

      try {
        Optional<SendungOverviewDto> resultDto = gson.fromJson(jsonOutput, OPTIONAL_DTO_TYPE_TOKEN);
        if (resultDto.isPresent()) {
          SendungOverviewDto dto = resultDto.get();
          taskResults.addAll(dto.getUrls());

          Optional<String> nextPageId = dto.getNextPageId();
          if (nextPageId.isPresent() && pageNumber < maxSubPages) {
            processNextPage(nextPageId.get());
          }
        } else {
          LOG.error("SrfSendungOverviewPageTask: no result dto " + aTarget.getUri().toString());
        }
      } catch (JsonSyntaxException e) {
        LOG.error("SrfSendungOverviewPageTask: Error reading url " + aTarget.getUri().toString(), e);
      }
    } else {
      LOG.error("SrfSendungOverviewPageTask: Error reading url " + aTarget.getUri().toString() + ": " + response.getStatus());
    }
  }

  @Override
  protected AbstractUrlTask createNewOwnInstance(ConcurrentLinkedQueue aURLsToCrawl) {
    return new SrfSendungOverviewPageTask(crawler, aURLsToCrawl);
  }

  private void processNextPage(String aNextPageId) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urlDtos = new ConcurrentLinkedQueue<>();
    urlDtos.add(new CrawlerUrlDTO(aNextPageId));
    Set<CrawlerUrlDTO> x = (Set<CrawlerUrlDTO>) new SrfSendungOverviewPageTask(crawler, urlDtos, pageNumber + 1).invoke();
    taskResults.addAll(x);
  }
}
