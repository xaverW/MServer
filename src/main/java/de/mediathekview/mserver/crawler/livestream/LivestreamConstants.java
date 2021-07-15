package de.mediathekview.mserver.crawler.livestream;

public final class LivestreamConstants {

  /** ARD Livestreams */
  public static final String URL_ARD_LIVESTREAMS = "https://appdata.ardmediathek.de/appdata/servlet/tv/live?json";
  /** ARD Livestream details */
  public static final String URL_ARD_LIVESTREAM_DETAIL = "https://appdata.ardmediathek.de/appdata/servlet/play/config/%s";
  /** ZDF Livestreams */
  public static final String URL_ZDF_LIVESTREAMS = "https://zdf-cdn.live.cellular.de/mediathekV2/live-tv/%s";
  /** ORF Livestreams */
  public static final String URL_ORF_LIVESTREAMS = "https://api-tvthek.orf.at/api/v4.1/livestreams";
  /** ORF Livestream details **/
  public static final String URL_ORF_LIVESTREAM_DETAIL = "https://api-tvthek.orf.at/api/v4.1/livestream/%s";
  /** ORF KEY */
  public static final String ORF_API_KEY = "Basic cHNfYW5kcm9pZF92M19uZXc6MDY1MmU0NjZkMTk5MGQxZmRmNDBkYTA4ZTc5MzNlMDY=";
  /** SRF Livestreams */
  public static final String URL_SRF_LIVESTREAMS = "https://www.srf.ch/play/v3/api/srf/production/tv-livestreams";
  /** SRF Livestream details */
  public static final String URL_SRF_LIVESTREAM_DETAIL = "https://il.srgssr.ch/integrationlayer/2.0/mediaComposition/byUrn/urn:srf:video:%s.json";
  
  private LivestreamConstants() {}
}
