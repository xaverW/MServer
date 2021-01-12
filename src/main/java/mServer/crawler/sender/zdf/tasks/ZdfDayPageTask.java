package mServer.crawler.sender.zdf.tasks;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import jakarta.ws.rs.client.WebTarget;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.zdf.json.ZdfDayPageDeserializer;
import mServer.crawler.sender.zdf.json.ZdfDayPageDto;

public class ZdfDayPageTask extends ZdfTaskBase<CrawlerUrlDTO, CrawlerUrlDTO> {

  private final String apiUrlBase;

  public ZdfDayPageTask(
          final MediathekReader aCrawler,
          final String aApiUrlBase,
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos,
          final Optional<String> aAuthKey) {
    super(aCrawler, aUrlToCrawlDtos, aAuthKey);
    apiUrlBase = aApiUrlBase;
    registerJsonDeserializer(ZdfDayPageDto.class, new ZdfDayPageDeserializer(apiUrlBase));
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDto, final WebTarget aTarget) {
    final ZdfDayPageDto dto = deserialize(aTarget, ZdfDayPageDto.class);
    if (dto != null) {
      taskResults.addAll(dto.getEntries());
      processNextPage(dto);
    }
  }

  @Override
  protected AbstractRecursivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new ZdfDayPageTask(crawler, apiUrlBase, aElementsToProcess, authKey);
  }

  private void processNextPage(final ZdfDayPageDto entries) {
    if (entries.getNextPageUrl().isPresent() && !entries.getEntries().isEmpty()) {
      final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
      urls.add(new CrawlerUrlDTO(entries.getNextPageUrl().get()));
      taskResults.addAll(createNewOwnInstance(urls).invoke());
    }
  }
}
