package de.mediathekview.mserver.base.config;

import de.mediathekview.mlib.config.ConfigDTO;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.filmlisten.FilmlistFormats;

import java.net.URL;
import java.util.*;

/** A POJO with the configs for MServer. */
public class MServerConfigDTO extends MServerBasicConfigDTO implements ConfigDTO {
  private final MServerCopySettings copySettings;
  private final Boolean writeFilmlistHashFileEnabled;
  private final String filmlistHashFilePath;
  private final Boolean writeFilmlistIdFileEnabled;
  private final String filmlistIdFilePath;
  /** The maximum amount of cpu threads to be used. */
  private Integer maximumCpuThreads;
  /**
   * The maximum duration in minutes the server should run.<br>
   * If set to 0 the server runs without a time limit.
   */
  private Integer maximumServerDurationInMinutes;

  private Map<Sender, MServerBasicConfigDTO> senderConfigurations;
  private Set<Sender> senderExcluded;
  private Set<Sender> senderIncluded;
  private Set<FilmlistFormats> filmlistSaveFormats;
  private Map<FilmlistFormats, String> filmlistSavePaths;
  private Map<FilmlistFormats, String> filmlistDiffSavePaths;
  private FilmlistFormats filmlistImportFormat;
  private String filmlistImportLocation;
  private MServerLogSettingsDTO logSettings;
  private Map<CrawlerUrlType, URL> crawlerURLs;
  private Map<CrawlerApiParam, String> crawlerApiParams;
  private Boolean filmlistImporEnabled;

  public MServerConfigDTO() {
    super();
    senderConfigurations = new EnumMap<>(Sender.class);
    senderExcluded = new HashSet<>();
    senderIncluded = new HashSet<>();
    filmlistSaveFormats = new HashSet<>();
    filmlistSavePaths = new EnumMap<>(FilmlistFormats.class);
    filmlistDiffSavePaths = new EnumMap<>(FilmlistFormats.class);
    copySettings = new MServerCopySettings();
    logSettings = new MServerLogSettingsDTO();
    crawlerURLs = new EnumMap<>(CrawlerUrlType.class);

    setMaximumUrlsPerTask(50);
    setMaximumCrawlDurationInMinutes(30);
    setMaximumSubpages(3);
    setMaximumDaysForSendungVerpasstSection(6);
    setMaximumDaysForSendungVerpasstSectionFuture(3);
    setSocketTimeoutInSeconds(60);
    setMaximumRequestsPerSecond(1.0);

    maximumCpuThreads = 80;
    maximumServerDurationInMinutes = 0;
    filmlistSaveFormats.add(FilmlistFormats.JSON);
    filmlistSaveFormats.add(FilmlistFormats.OLD_JSON);
    filmlistSaveFormats.add(FilmlistFormats.JSON_COMPRESSED_XZ);
    filmlistSaveFormats.add(FilmlistFormats.OLD_JSON_COMPRESSED_XZ);
    filmlistSaveFormats.add(FilmlistFormats.JSON_COMPRESSED_GZIP);
    filmlistSaveFormats.add(FilmlistFormats.OLD_JSON_COMPRESSED_GZIP);
    filmlistSaveFormats.add(FilmlistFormats.JSON_COMPRESSED_BZIP);
    filmlistSaveFormats.add(FilmlistFormats.OLD_JSON_COMPRESSED_BZIP);

    filmlistSavePaths.put(FilmlistFormats.JSON, "filmliste.json");
    filmlistSavePaths.put(FilmlistFormats.OLD_JSON, "filmliste_old.json");
    filmlistSavePaths.put(FilmlistFormats.JSON_COMPRESSED_XZ, "filmliste.json.xz");
    filmlistSavePaths.put(FilmlistFormats.OLD_JSON_COMPRESSED_XZ, "filmliste_old.json.xz");
    filmlistSavePaths.put(FilmlistFormats.JSON_COMPRESSED_GZIP, "filmliste.json.gz");
    filmlistSavePaths.put(FilmlistFormats.OLD_JSON_COMPRESSED_GZIP, "filmliste_old.json.gz");
    filmlistSavePaths.put(FilmlistFormats.JSON_COMPRESSED_BZIP, "filmliste.json.bz");
    filmlistSavePaths.put(FilmlistFormats.OLD_JSON_COMPRESSED_BZIP, "filmliste_old.json.bz");

    filmlistImporEnabled = true;
    filmlistImportFormat = FilmlistFormats.OLD_JSON_COMPRESSED_XZ;
    filmlistImportLocation = "https://verteiler1.mediathekview.de/Filmliste-akt.xz";

    writeFilmlistHashFileEnabled = false;
    filmlistHashFilePath = "filmlist.hash";
    writeFilmlistIdFileEnabled = true;
    filmlistIdFilePath = "filmlist.id";

    Arrays.stream(Sender.values())
        .forEach(sender -> senderConfigurations.put(sender, new MServerBasicConfigDTO(this)));
  }

  public MServerCopySettings getCopySettings() {
    return copySettings;
  }

  public Map<CrawlerUrlType, URL> getCrawlerURLs() {
    return crawlerURLs;
  }

  public Map<CrawlerApiParam, String> getCrawlerApiParams() {
    return crawlerApiParams;
  }

  public void setCrawlerURLs(final Map<CrawlerUrlType, URL> crawlerURLs) {
    this.crawlerURLs = crawlerURLs;
  }

  public void setCrawlerApiParams(final Map<CrawlerApiParam, String> pCrawlerApiParams) {
    this.crawlerApiParams = pCrawlerApiParams;
  }

  public Map<FilmlistFormats, String> getFilmlistDiffSavePaths() {
    return filmlistDiffSavePaths;
  }

  public void setFilmlistDiffSavePaths(final Map<FilmlistFormats, String> filmlistDiffSavePaths) {
    this.filmlistDiffSavePaths = filmlistDiffSavePaths;
  }

  public FilmlistFormats getFilmlistImportFormat() {
    return filmlistImportFormat;
  }

  public void setFilmlistImportFormat(final FilmlistFormats filmlistImportFormat) {
    this.filmlistImportFormat = filmlistImportFormat;
  }

  public String getFilmlistImportLocation() {
    return filmlistImportLocation;
  }

  public void setFilmlistImportLocation(final String filmlistImportLocation) {
    this.filmlistImportLocation = filmlistImportLocation;
  }

  public Set<FilmlistFormats> getFilmlistSaveFormats() {
    return filmlistSaveFormats;
  }

  public void setFilmlistSaveFormats(final Set<FilmlistFormats> filmlistSaveFormats) {
    this.filmlistSaveFormats = filmlistSaveFormats;
  }

  public Map<FilmlistFormats, String> getFilmlistSavePaths() {
    return filmlistSavePaths;
  }

  public void setFilmlistSavePaths(final Map<FilmlistFormats, String> filmlistSavePaths) {
    this.filmlistSavePaths = filmlistSavePaths;
  }

  public MServerLogSettingsDTO getLogSettings() {
    return logSettings;
  }

  public void setLogSettings(final MServerLogSettingsDTO logSettings) {
    this.logSettings = logSettings;
  }

  public Integer getMaximumCpuThreads() {
    return maximumCpuThreads;
  }

  public void setMaximumCpuThreads(final Integer aMaximumCpuThreads) {
    maximumCpuThreads = aMaximumCpuThreads;
  }

  public Integer getMaximumServerDurationInMinutes() {
    return maximumServerDurationInMinutes;
  }

  public void setMaximumServerDurationInMinutes(final Integer aMaximumServerDurationInMinutes) {
    maximumServerDurationInMinutes = aMaximumServerDurationInMinutes;
  }

  public Map<Sender, MServerBasicConfigDTO> getSenderConfigurations() {
    return senderConfigurations;
  }

  public void setSenderConfigurations(
      final Map<Sender, MServerBasicConfigDTO> aSenderConfigurations) {
    senderConfigurations = aSenderConfigurations;
  }

  public Set<Sender> getSenderExcluded() {
    return senderExcluded;
  }

  public void setSenderExcluded(final Set<Sender> senderExcluded) {
    this.senderExcluded = senderExcluded;
  }

  public Set<Sender> getSenderIncluded() {
    return senderIncluded;
  }

  public void setSenderIncluded(final Set<Sender> senderIncluded) {
    this.senderIncluded = senderIncluded;
  }

  public Optional<URL> getSingleCrawlerURL(final CrawlerUrlType urlType) {
    if (crawlerURLs.containsKey(urlType)) {
      return Optional.of(crawlerURLs.get(urlType));
    }

    return urlType.getDefaultUrl();
  }

  public Optional<String> getCrawlerApiParam(final CrawlerApiParam apiParam) {
    if (crawlerApiParams.containsKey(apiParam)) {
      return Optional.of(crawlerApiParams.get(apiParam));
    }
    return Optional.empty();
  }

  public Boolean getWriteFilmlistHashFileEnabled() {
    return writeFilmlistHashFileEnabled;
  }

  public String getFilmlistHashFilePath() {
    return filmlistHashFilePath;
  }

  public Boolean getWriteFilmlistIdFileEnabled() {
    return writeFilmlistIdFileEnabled;
  }

  public String getFilmlistIdFilePath() {
    return filmlistIdFilePath;
  }

  /**
   * Loads the {@link Sender} specific configuration and if it not exist creates one.
   *
   * @param aSender The {@link Sender} for which to load the configuration.
   * @return The {@link Sender} specific configuration and if it not exist the default
   *     configuration.
   */
  public MServerBasicConfigDTO getSenderConfig(final Sender aSender) {
    senderConfigurations.putIfAbsent(aSender, new MServerBasicConfigDTO(this));
    return senderConfigurations.get(aSender);
  }

  public Boolean getFilmlistImporEnabled() {
    return filmlistImporEnabled;
  }

  public void setFilmlistImporEnabled(final Boolean filmlistImporEnabled) {
    this.filmlistImporEnabled = filmlistImporEnabled;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MServerConfigDTO)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    final MServerConfigDTO that = (MServerConfigDTO) o;
    return Objects.equals(getCopySettings(), that.getCopySettings())
        && Objects.equals(getMaximumCpuThreads(), that.getMaximumCpuThreads())
        && Objects.equals(
            getMaximumServerDurationInMinutes(), that.getMaximumServerDurationInMinutes())
        && Objects.equals(senderConfigurations, that.senderConfigurations)
        && Objects.equals(getSenderExcluded(), that.getSenderExcluded())
        && Objects.equals(getSenderIncluded(), that.getSenderIncluded())
        && Objects.equals(getFilmlistSaveFormats(), that.getFilmlistSaveFormats())
        && Objects.equals(getFilmlistSavePaths(), that.getFilmlistSavePaths())
        && Objects.equals(getFilmlistDiffSavePaths(), that.getFilmlistDiffSavePaths())
        && getFilmlistImportFormat() == that.getFilmlistImportFormat()
        && Objects.equals(getFilmlistImportLocation(), that.getFilmlistImportLocation())
        && Objects.equals(getLogSettings(), that.getLogSettings())
        && Objects.equals(getCrawlerURLs(), that.getCrawlerURLs())
        && Objects.equals(getFilmlistImporEnabled(), that.getFilmlistImporEnabled())
        && Objects.equals(getWriteFilmlistHashFileEnabled(), that.getWriteFilmlistHashFileEnabled())
        && Objects.equals(getFilmlistHashFilePath(), that.getFilmlistHashFilePath())
        && Objects.equals(getWriteFilmlistIdFileEnabled(), that.getWriteFilmlistIdFileEnabled())
        && Objects.equals(getFilmlistIdFilePath(), that.getFilmlistIdFilePath());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(),
        getCopySettings(),
        getMaximumCpuThreads(),
        getMaximumServerDurationInMinutes(),
        senderConfigurations,
        getSenderExcluded(),
        getSenderIncluded(),
        getFilmlistSaveFormats(),
        getFilmlistSavePaths(),
        getFilmlistDiffSavePaths(),
        getFilmlistImportFormat(),
        getFilmlistImportLocation(),
        getLogSettings(),
        getCrawlerURLs(),
        getFilmlistImporEnabled(),
        getWriteFilmlistHashFileEnabled(),
        getFilmlistHashFilePath(),
        getWriteFilmlistIdFileEnabled(),
        getFilmlistIdFilePath());
  }

  public void initializeSenderConfigurations() {
    senderConfigurations
        .values()
        .forEach(senderConfig -> senderConfig.setParentConfig(Optional.of(this)));
  }

  public URL putCrawlerUrl(final CrawlerUrlType key, final URL value) {
    return crawlerURLs.put(key, value);
  }

  public String putCrawlerApiParam(final CrawlerApiParam key, final String value) {
    return crawlerApiParams.put(key, value);
  }

}
