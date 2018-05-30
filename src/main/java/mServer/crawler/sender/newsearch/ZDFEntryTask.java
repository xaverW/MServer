package mServer.crawler.sender.newsearch;

import java.util.concurrent.RecursiveTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.tool.Log;

/**
 * Searches all information required for a film
 */
public class ZDFEntryTask extends RecursiveTask<VideoDTO> {

  private static final long serialVersionUID = 1L;

  private final ZDFClient client;
  private final ZDFEntryDTO zdfEntryDTO;
  private final Gson gson;

  public ZDFEntryTask(ZDFEntryDTO aEntryDto) {
    this(aEntryDto, new ZDFClient());
  }

  public ZDFEntryTask(ZDFEntryDTO aEntryDto, ZDFClient zdfClient) {
    client = zdfClient;
    zdfEntryDTO = aEntryDto;
    gson = new GsonBuilder()
            .registerTypeAdapter(VideoDTO.class, new ZDFVideoDTODeserializer())
            .registerTypeAdapter(DownloadDTO.class, new ZDFDownloadDTODeserializer())
            .create();
  }

  @Override
  protected VideoDTO compute() {
    Thread.currentThread().setName("ZDF Pool EntryTask");

    if (zdfEntryDTO == null) {
      return null;
    }

    VideoDTO dto = null;

    if (!Config.getStop()) {
      try {
        // read film details
        String infoUrl = zdfEntryDTO.getEntryGeneralInformationUrl();
        JsonObject baseObjectInfo = client.execute(infoUrl);
        if (baseObjectInfo != null) {
          dto = gson.fromJson(baseObjectInfo, VideoDTO.class);
          if (dto != null) {
            // read download details
            String downloadUrl = zdfEntryDTO.getEntryDownloadInformationUrl();
            JsonObject baseObjectDownload = client.execute(downloadUrl);
            if (baseObjectDownload != null) {
              DownloadDTO downloadDto = gson.fromJson(baseObjectDownload, DownloadDTO.class);
              dto.setDownloadDto(downloadDto);
            } else {
              // entry without download infos is not relevant
              dto = null;
            }
          }
        }
      } catch (Exception ex) {
        Log.errorLog(496583202, ex, "Exception parsing " + (zdfEntryDTO != null ? zdfEntryDTO.getEntryGeneralInformationUrl() : ""));
        dto = null;
      }
    }

    return dto;
  }
}
