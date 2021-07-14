package mServer.crawler.sender.zdf;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.JsoupConnection;
import mServer.crawler.sender.zdf.tasks.ZdfDayPageHtmlTask;
import mServer.crawler.sender.zdf.tasks.ZdfLetterListHtmlTask;
import mServer.crawler.sender.zdf.tasks.ZdfTopicPageHtmlTask;
import mServer.crawler.sender.zdf.tasks.ZdfTopicsPageHtmlTask;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

public class ZdfCrawler extends AbstractZdfCrawler {

  private static final int MAXIMUM_DAYS_HTML_PAST = 7;

  public ZdfCrawler(FilmeSuchen ssearch, int startPrio) {
    super(Const.ZDF, ssearch, startPrio);
  }

  @Override
  protected @NotNull String getUrlBase() {
    return ZdfConstants.URL_BASE;
  }

  @Override
  protected String getApiUrlBase() {
    return ZdfConstants.URL_API_BASE;
  }

  @Override
  protected @NotNull String getUrlDay() {
    return ZdfConstants.URL_DAY;
  }

  @Override
  public Queue<CrawlerUrlDTO> getTopicsEntries() throws ExecutionException, InterruptedException {

    final ConcurrentLinkedQueue<CrawlerUrlDTO> shows = new ConcurrentLinkedQueue<>();

    final ConcurrentLinkedQueue<CrawlerUrlDTO> letterListUrl = new ConcurrentLinkedQueue<>();
    letterListUrl.add(new CrawlerUrlDTO(ZdfConstants.URL_TOPICS));

    final ZdfLetterListHtmlTask letterTask = new ZdfLetterListHtmlTask(this, letterListUrl);
    final Set<CrawlerUrlDTO> letterUrls = forkJoinPool.submit(letterTask).get();

    Log.sysLog("ZDF: letters: " + letterUrls.size());

    if (Config.getStop()) {
      return shows;
    }

    final ZdfTopicsPageHtmlTask topicsTask =
            new ZdfTopicsPageHtmlTask(this, new ConcurrentLinkedQueue<>(letterUrls));
    final Set<CrawlerUrlDTO> topicsUrls = forkJoinPool.submit(topicsTask).get();

    Log.sysLog("ZDF: topics: " + topicsUrls.size());

    if (Config.getStop()) {
      return shows;
    }

    final ZdfTopicPageHtmlTask topicTask =
            new ZdfTopicPageHtmlTask(this, new ConcurrentLinkedQueue<>(topicsUrls));
    shows.addAll(forkJoinPool.submit(topicTask).get());

    return shows;
  }

  @Override
  protected Collection<CrawlerUrlDTO> getExtraDaysEntries()
    throws ExecutionException, InterruptedException {

    final ZdfDayPageHtmlTask dayTask =
      new ZdfDayPageHtmlTask(getApiUrlBase(), this, getExtraDayUrls(), new JsoupConnection());
    return forkJoinPool.submit(dayTask).get();
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> getExtraDayUrls() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    for (int i = 0; i <= MAXIMUM_DAYS_HTML_PAST; i++) {

      final LocalDateTime local = LocalDateTime.now().minus(i, ChronoUnit.DAYS);
      final String date = local.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
      final String url = String.format(ZdfConstants.URL_HTML_DAY, date);
      urls.add(new CrawlerUrlDTO(url));
    }

    return urls;
  }
}
