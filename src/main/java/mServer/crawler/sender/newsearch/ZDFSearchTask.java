package mServer.crawler.sender.newsearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveTask;

import com.google.gson.JsonObject;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.tool.Log;
import java.time.ZonedDateTime;
import mServer.tool.MserverDaten;

public class ZDFSearchTask extends RecursiveTask<Collection<VideoDTO>> {

  private static final String JSON_ELEMENT_NEXT = "next";
  private static final String JSON_ELEMENT_RESULT = "http://zdf.de/rels/search/results";

  private final ZonedDateTime today = ZonedDateTime.now().withHour(0).withMinute(0);

  private static final int OFFSET_SEARCH_REQUST = 7;
  private static final long serialVersionUID = 1L;

  private final Collection<VideoDTO> filmList;
  private final ZDFClient client;

  private int page;
  private final int daysPast;
  private final int daysFuture;

  public ZDFSearchTask(int aDaysPast, int aDaysFuture) {

    super();

    filmList = new ArrayList<>();
    client = new ZDFClient();
    page = 1;
    daysPast = aDaysPast;
    daysFuture = aDaysFuture;
  }

  @Override
  protected Collection<VideoDTO> compute() {
    Thread.currentThread().setName("ZDF Pool SearchTask");
    if (!Config.getStop()) {
      try {
        Collection<ZDFSearchPageTask> subTasks = ConcurrentHashMap.newKeySet();

        final ZonedDateTime minDate = today.minusDays(daysPast);
        final ZonedDateTime maxDate = today.plusDays(daysFuture);

        ZonedDateTime startDate = minDate;
        ZonedDateTime endDate = minDate.plusDays(OFFSET_SEARCH_REQUST);
        if (endDate.isAfter(maxDate)) {
          endDate = maxDate;
        }

        while (!startDate.isAfter(maxDate)) {
          computeSearchRequest(subTasks, startDate, endDate);
          startDate = endDate;
          endDate = endDate.plusDays(OFFSET_SEARCH_REQUST);
        }

        subTasks.forEach(t -> filmList.addAll(t.join()));
        if (MserverDaten.debug) {
          Log.sysLog("All SearchTasks finished.");
        }
      } catch (Exception ex) {
        Log.errorLog(496583201, ex);
      }
    }
    return filmList;
  }

  private void computeSearchRequest(Collection<ZDFSearchPageTask> subTasks,
          final ZonedDateTime startDate, final ZonedDateTime endDate) {
    JsonObject baseObject;
    page = 1;
    do {
      baseObject = client.executeSearch(page, startDate, endDate);

      if (baseObject != null) {
        ZDFSearchPageTask task = new ZDFSearchPageTask(baseObject);
        task.fork();
        subTasks.add(task);
        if (MserverDaten.debug) {
          Log.sysLog("SearchTask " + task.hashCode() + " added.");
        }
      }

      page++;
    } while (!Config.getStop() && hasNextPage(baseObject));
  }

  private static boolean hasNextPage(final JsonObject baseObject) {
    return baseObject != null
            && baseObject.has(JSON_ELEMENT_NEXT)
            && baseObject.has(JSON_ELEMENT_RESULT)
            && baseObject.getAsJsonArray(JSON_ELEMENT_RESULT).size() > 0;
  }
}
