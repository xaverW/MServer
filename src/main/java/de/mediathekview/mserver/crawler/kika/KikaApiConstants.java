package de.mediathekview.mserver.crawler.kika;

import de.mediathekview.mserver.base.config.CrawlerUrlType;
import de.mediathekview.mserver.base.config.MServerConfigManager;

public final class KikaApiConstants {
  //
  public static int LIMIT = 400;
  //
  public static final String HOST = MServerConfigManager.getInstance().getConfig().getSingleCrawlerURL(CrawlerUrlType.KIKA_API_URL).get().toString();
  //
  public static final String OVERVIEW = HOST + "/api/brands?limit=" + LIMIT;
  //
  public static final String TOPIC = HOST + "/api/brands/%s/videos?limit=" + LIMIT;
  //
  public static final String FILM = HOST + "/api/videos/%s/player-assets";
  //
  private KikaApiConstants() {}
  //
}
