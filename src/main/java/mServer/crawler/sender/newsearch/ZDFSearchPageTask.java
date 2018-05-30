package mServer.crawler.sender.newsearch;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.tool.Log;
import mServer.tool.MserverDaten;

/**
 * Task to parse the response of a search page.
 */
public class ZDFSearchPageTask extends RecursiveTask<Collection<VideoDTO>> {

  private static final long serialVersionUID = 1L;

  private static final String JSON_ELEMENT_RESULTS = "http://zdf.de/rels/search/results";

  private final JsonObject searchResult;
  private final Gson gson;

  private static final Type ZDFENTRYDTO_COLLECTION_TYPE = new TypeToken<Collection<ZDFEntryDTO>>() {
  }.getType();

  public ZDFSearchPageTask(JsonObject aSearchResult) {
    searchResult = aSearchResult;

    gson = new GsonBuilder()
            .registerTypeAdapter(ZDFEntryDTO.class, new ZDFEntryDTODeserializer())
            .create();
  }

  @Override
  protected Collection<VideoDTO> compute() {
    Thread.currentThread().setName("ZDF Pool SearchPageTask");

    Collection<VideoDTO> filmList = new ArrayList<>();
    if (!Config.getStop()) {
      Collection<ZDFEntryTask> subTasks = new ArrayList<>();

      Collection<ZDFEntryDTO> zdfEntryDTOList = gson.fromJson(searchResult.getAsJsonArray(JSON_ELEMENT_RESULTS), ZDFENTRYDTO_COLLECTION_TYPE);
      zdfEntryDTOList.forEach(zdfEntryDTO -> {
        if (zdfEntryDTO != null) {
          final ZDFEntryTask entryTask = new ZDFEntryTask(zdfEntryDTO);
          entryTask.fork();
          subTasks.add(entryTask);

          if (MserverDaten.debug) {
            Log.sysLog("EntryTask " + entryTask.hashCode() + " added.");
          }
        }
      });

      // wait till entry tasks are finished
      subTasks.forEach(t -> filmList.add(t.join()));

      if (MserverDaten.debug) {
        Log.sysLog("All EntryTasks finished.");
      }
    }

    return filmList;
  }
}
