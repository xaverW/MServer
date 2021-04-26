package de.mediathekview.mserver.crawler;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.filmlisten.FilmlistFormats;
import de.mediathekview.mlib.filmlisten.FilmlistManager;
import de.mediathekview.mlib.progress.ProgressListener;
import de.mediathekview.mserver.base.config.MServerConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.config.MServerCopySettings;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.base.progress.AbstractManager;
import de.mediathekview.mserver.base.uploader.copy.FileCopyTarget;
import de.mediathekview.mserver.base.uploader.copy.FileCopyTask;
import de.mediathekview.mserver.crawler.ard.ArdCrawler;
import de.mediathekview.mserver.crawler.arte.*;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.TimeoutTask;
import de.mediathekview.mserver.crawler.br.BrCrawler;
import de.mediathekview.mserver.crawler.dreisat.DreiSatCrawler;
import de.mediathekview.mserver.crawler.dw.DwCrawler;
import de.mediathekview.mserver.crawler.funk.FunkCrawler;
import de.mediathekview.mserver.crawler.hr.HrCrawler;
import de.mediathekview.mserver.crawler.kika.KikaApiCrawler;
import de.mediathekview.mserver.crawler.kika.KikaCrawler;
import de.mediathekview.mserver.crawler.orf.OrfCrawler;
import de.mediathekview.mserver.crawler.phoenix.PhoenixCrawler;
import de.mediathekview.mserver.crawler.sr.SrCrawler;
import de.mediathekview.mserver.crawler.srf.SrfCrawler;
import de.mediathekview.mserver.crawler.zdf.ZdfCrawler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/** A manager to control the crawler. */
public class CrawlerManager extends AbstractManager {

  private static final String HOME_PATTERN = "^~";
  private static final String USER_HOME_PATH = System.getProperty("user.home");
  private static final String FILMLIST_IMPORT_ERROR_TEMPLATE =
      "Something went terrible wrong on importing the film list with the following location: \"%s\"";
  private static final String HTTP = "http";
  private static final String FILMLIST_JSON_DEFAULT_NAME = "filmliste.json";
  private static final String FILMLIST_JSON_COMPRESSED_DEFAULT_NAME =
      FILMLIST_JSON_DEFAULT_NAME + ".xz";
  private static final Logger LOG = LogManager.getLogger(CrawlerManager.class);
  private static CrawlerManager instance;
  private final MServerConfigDTO config;
  private final ForkJoinPool forkJoinPool;
  private final Filmlist filmlist;
  private final ExecutorService executorService;

  private final Map<Sender, AbstractCrawler> crawlerMap;

  private final FilmlistManager filmlistManager;
  private final Collection<ProgressListener> copyProgressListeners;
  private Filmlist differenceList;

  private CrawlerManager() {
    super();
    final MServerConfigManager rootConfig = new MServerConfigManager();
    config = rootConfig.getConfig();
    executorService = Executors.newFixedThreadPool(config.getMaximumCpuThreads());
    forkJoinPool = new ForkJoinPool(config.getMaximumCpuThreads());

    crawlerMap = new EnumMap<>(Sender.class);
    filmlist = new Filmlist();
    filmlistManager = FilmlistManager.getInstance();
    copyProgressListeners = new ArrayList<>();
    initializeCrawler(rootConfig);
  }

  public static CrawlerManager getInstance() {
    if (instance == null) {
      instance = new CrawlerManager();
    }
    return instance;
  }

  public void copyFilmlist() {
    final MServerCopySettings copySettings = config.getCopySettings();
    if (copySettings.getCopyEnabled() != null && copySettings.getCopyEnabled()) {
      for (final Entry<FilmlistFormats, String> copyEntry :
          copySettings.getCopyTargetFilePaths().entrySet()) {
        copyFilmlist(
            copyEntry.getKey(), new FileCopyTarget(Paths.get(copyEntry.getValue())), false);
      }
      for (final Entry<FilmlistFormats, String> copyEntry :
          copySettings.getCopyTargetDiffFilePaths().entrySet()) {
        copyFilmlist(copyEntry.getKey(), new FileCopyTarget(Paths.get(copyEntry.getValue())), true);
      }
    }
  }

  public void copyFilmlist(
      final FilmlistFormats aFilmlistFormat,
      final FileCopyTarget aFileCopyTarget,
      final boolean isDiffList) {
    final Set<FilmlistFormats> formats;
    final Map<FilmlistFormats, String> paths;
    if (isDiffList) {
      formats = config.getFilmlistDiffSavePaths().keySet();
      paths = config.getFilmlistDiffSavePaths();
    } else {
      formats = config.getFilmlistSaveFormats();
      paths = config.getFilmlistSavePaths();
    }

    if (formats.contains(aFilmlistFormat)) {
      copyFilmlist(Paths.get(paths.get(aFilmlistFormat)), aFileCopyTarget);
    } else {
      printMessage(ServerMessages.FORMAT_NOT_IN_SAVE_FORMATS, aFilmlistFormat);
    }
  }

  public void copyFilmlist(final Path aFilmlistPath, final FileCopyTarget aFileCopyTarget) {
    final FileCopyTask copyUploadTask = new FileCopyTask(aFilmlistPath, aFileCopyTarget);
    copyUploadTask.addAllMessageListener(messageListeners);
    copyUploadTask.addAllProgressListener(copyProgressListeners);
    executorService.execute(copyUploadTask);
  }

  public Set<Sender> getAviableSenderToCrawl() {
    return new HashSet<>(crawlerMap.keySet());
  }

  public ExecutorService getExecutorService() {
    return executorService;
  }

  /**
   * Imports the film list with the given {@link MServerConfigDTO#getFilmlistImportFormat()} of
   * {@link MServerConfigDTO#getFilmlistImportLocation()}.
   */
  public void importFilmlist() {
    if (checkConfigForFilmlistImport()) {
      importFilmlist(config.getFilmlistImportFormat(), config.getFilmlistImportLocation());
    }
  }

  /**
   * Imports the film list with the given {@link FilmlistFormats} and the given location.
   *
   * @param aFormat The{@link FilmlistFormats} to import.
   * @param aFilmlistLocation The given location from which to import. If it starts with <code>http
   *     </code> or <code>https</code> it tries to import from URL. Otherwise it tries to import
   *     from the given Location as a file path.
   */
  public void importFilmlist(final FilmlistFormats aFormat, final String aFilmlistLocation) {
    try {
      final Optional<Filmlist> importedFilmlist;
      if (aFilmlistLocation.startsWith(HTTP)) {
        importedFilmlist = importFilmListFromURl(aFormat, aFilmlistLocation);
      } else {
        importedFilmlist = importFilmlistFromFile(aFormat, aFilmlistLocation);
      }

      importedFilmlist.ifPresent(value -> differenceList = filmlist.merge(value));
    } catch (final IOException ioException) {
      LOG.fatal(String.format(FILMLIST_IMPORT_ERROR_TEMPLATE, aFilmlistLocation), ioException);
    }
  }

  /**
   * Saves the actual difference film list for each {@link FilmlistFormats} of {@link
   * MServerConfigDTO#getFilmlistSaveFormats()} in a file with the path of {@link
   * MServerConfigDTO#getFilmlistSavePaths()}.
   */
  public void saveDifferenceFilmlist() {
    config.getFilmlistDiffSavePaths().forEach((key, value) -> saveFilmlist(Paths.get(value), key));
  }

  /**
   * Saves the actual film list for each {@link FilmlistFormats} of {@link
   * MServerConfigDTO#getFilmlistSaveFormats()} in a file with the path of {@link
   * MServerConfigDTO#getFilmlistSavePaths()}.
   */
  public void saveFilmlist() {
    if (checkConfigForFilmlistSave()) {
      config
          .getFilmlistSaveFormats()
          .forEach(f -> saveFilmlist(Paths.get(config.getFilmlistSavePaths().get(f)), f));
    }
  }

  /**
   * Saves the actual film list with the given {@link FilmlistFormats} to the given path.
   *
   * @param aSavePath The path where to save the film list.
   * @param aFormat The {@link FilmlistFormats} in which the film list should be saved to.
   */
  public void saveFilmlist(final Path aSavePath, final FilmlistFormats aFormat) {
    saveFilmlist(aSavePath, aFormat, false);
  }

  /**
   * Saves the actual film list with the given {@link FilmlistFormats} to the given path.
   *
   * @param aSavePath The path where to save the film list.
   * @param aFormat The {@link FilmlistFormats} in which the film list should be saved to.
   * @param aIsDiff When true the diff list will be saved instead of the full film list.
   */
  public void saveFilmlist(
      final Path aSavePath, final FilmlistFormats aFormat, final boolean aIsDiff) {
    final Path filteredSavePath = filterPath(aSavePath);

    final Path filmlistFilePath = getFilmlistFilePath(aFormat, filteredSavePath).toAbsolutePath();

    final Path parentDir = filmlistFilePath.getParent();
    if (parentDir != null) {
      try {
        Files.createDirectories(parentDir);
      } catch (final IOException ioException) {
        LOG.debug("Can't create the parent directories!");
        printMessage(ServerMessages.FILMLIST_SAVE_PATH_INVALID, filmlistFilePath.toString());
        return;
      }
    }

    filmlistManager.addAllMessageListener(messageListeners);
    if (aIsDiff) {
      filmlistManager.save(aFormat, differenceList, filmlistFilePath);
    } else {
      filmlistManager.save(aFormat, filmlist, filmlistFilePath);
    }
  }

  @NotNull
  private Path getFilmlistFilePath(final FilmlistFormats aFormat, final Path filteredSavePath) {
    final Path filmlistFileSafePath;
    if (Files.isDirectory(filteredSavePath)) {
      if (FilmlistFormats.JSON.equals(aFormat) || FilmlistFormats.OLD_JSON.equals(aFormat)) {
        filmlistFileSafePath = filteredSavePath.resolve(FILMLIST_JSON_DEFAULT_NAME);
      } else {
        filmlistFileSafePath = filteredSavePath.resolve(FILMLIST_JSON_COMPRESSED_DEFAULT_NAME);
      }
    } else {
      filmlistFileSafePath = filteredSavePath;
    }
    return filmlistFileSafePath;
  }

  /**
   * Runs all crawler and starts a timer for {@link
   * MServerConfigDTO#getMaximumServerDurationInMinutes()}.<br>
   * When configured it will be restart the crawlers after the configured schedules.
   */
  public void start() {
    startCrawlers();
  }

  /**
   * Starts the crawler for the given {@link Sender}.<br>
   * <br>
   * <b>WARNING:</b> If no crawler is listed for {@link Sender} a {@link IllegalArgumentException}
   * will be thrown.<br>
   * You can ensure if there is a crawler for {@link Sender} with {@link
   * CrawlerManager#getAviableSenderToCrawl}.
   *
   * @param aSenders The Sender which crawler to start.
   */
  public void startCrawlerForSender(final Sender... aSenders) {
    final Collection<AbstractCrawler> crawlers = new ArrayList<>();
    for (final Sender sender : aSenders) {

      if (crawlerMap.containsKey(sender)) {
        crawlers.add(crawlerMap.get(sender));
      } else {
        throw new IllegalArgumentException(
            String.format(
                "There is no registered crawler for the Sender \"%s\"", sender.getName()));
      }
    }
    runCrawlers(crawlers.toArray(new AbstractCrawler[0]));
  }

  /**
   * Runs all crawler and starts a timer for {@link
   * MServerConfigDTO#getMaximumServerDurationInMinutes()}.
   */
  public void startCrawlers() {
    final TimeoutTask timeoutRunner = createTimeoutTask();

    if (config.getMaximumServerDurationInMinutes() != null
        && config.getMaximumServerDurationInMinutes() > 0) {
      timeoutRunner.start();
    }
    final Set<AbstractCrawler> crawlerToRun = getCrawlerToRun();
    runCrawlers(crawlerToRun.toArray(new AbstractCrawler[0]));
    timeoutRunner.stopTimeout();
  }

  /** This stops all running crawler. */
  public void stop() {
    forkJoinPool.shutdown();
    executorService.shutdown();
  }

  private boolean checkAllUsedFormatsHaveSavePaths() {
    final List<FilmlistFormats> missingSavePathFormats =
        config.getFilmlistSaveFormats().stream()
            .filter(f -> !config.getFilmlistSavePaths().containsKey(f))
            .collect(Collectors.toList());
    missingSavePathFormats.forEach(
        f -> printMessage(ServerMessages.NO_FILMLIST_SAVE_PATH_FOR_FORMAT_CONFIGURED, f.name()));
    return missingSavePathFormats.isEmpty();
  }

  private boolean checkConfigForFilmlistImport() {
    if (config.getFilmlistImportFormat() == null) {
      printMessage(ServerMessages.NO_FILMLIST_IMPORT_FORMAT_IN_CONFIG);
      return false;
    }

    if (config.getFilmlistImportLocation() == null) {
      printMessage(ServerMessages.NO_FILMLIST_IMPORT_LOCATION_IN_CONFIG);
      return false;
    }

    return true;
  }

  private boolean checkConfigForFilmlistSave() {
    if (config.getFilmlistSaveFormats().isEmpty()) {
      printMessage(ServerMessages.NO_FILMLIST_FORMAT_CONFIGURED);
      return false;
    }

    if (config.getFilmlistSavePaths().isEmpty()) {
      printMessage(ServerMessages.NO_FILMLIST_SAVE_PATHS_CONFIGURED);
      return false;
    }
    return checkAllUsedFormatsHaveSavePaths();
  }

  private boolean checkFilmlistImportFile(final Path aFilmlistPath) {
    if (Files.notExists(aFilmlistPath)) {
      printMessage(
          ServerMessages.FILMLIST_IMPORT_FILE_NOT_FOUND, aFilmlistPath.toAbsolutePath().toString());
      return false;
    }
    if (!Files.isReadable(aFilmlistPath)) {
      printMessage(
          ServerMessages.FILMLIST_IMPORT_FILE_NO_READ_PERMISSION,
          aFilmlistPath.toAbsolutePath().toString());
      return false;
    }
    return true;
  }

  private TimeoutTask createTimeoutTask() {
    return new TimeoutTask(config.getMaximumServerDurationInMinutes()) {
      @Override
      public void shutdown() {
        forkJoinPool.shutdownNow();
        printMessage(ServerMessages.SERVER_TIMEOUT);
      }
    };
  }

  private Path filterPath(final Path aSavePath) {
    return Paths.get(aSavePath.toString().replaceFirst(HOME_PATTERN, USER_HOME_PATH));
  }

  private Set<AbstractCrawler> getCrawlerToRun() {
    final Set<AbstractCrawler> crawlerToRun = new HashSet<>();

    if (config.getSenderIncluded().isEmpty()) {
      crawlerToRun.addAll(crawlerMap.values());
    } else {
      config.getSenderIncluded().stream()
          .filter(crawlerMap::containsKey)
          .forEach(s -> crawlerToRun.add(crawlerMap.get(s)));
    }

    if (!config.getSenderExcluded().isEmpty()) {
      config.getSenderExcluded().stream()
          .filter(crawlerMap::containsKey)
          .forEach(s -> crawlerToRun.remove(crawlerMap.get(s)));
    }

    return crawlerToRun;
  }

  private Optional<Filmlist> importFilmlistFromFile(
      final FilmlistFormats aFormat, final String aFilmlistLocation) throws IOException {
    final Path filmlistPath = Paths.get(aFilmlistLocation);
    if (checkFilmlistImportFile(filmlistPath)) {
      filmlistManager.addAllMessageListener(messageListeners);
      return filmlistManager.importList(aFormat, filmlistPath);
    }
    return Optional.empty();
  }

  private Optional<Filmlist> importFilmListFromURl(
      final FilmlistFormats aFormat, final String aFilmlistLocation) throws IOException {
    try {
      filmlistManager.addAllMessageListener(messageListeners);
      return filmlistManager.importList(aFormat, new URL(aFilmlistLocation));
    } catch (final MalformedURLException malformedURLException) {
      printMessage(ServerMessages.FILMLIST_IMPORT_URL_INVALID, aFilmlistLocation);
    }
    return Optional.empty();
  }

  public void writeHashFile() {
    if (config.getWriteFilmlistHashFileEnabled()) {
      final Path hashFilePath =
          filterPath(Paths.get(config.getFilmlistHashFilePath())).toAbsolutePath();
      if (!Files.exists(hashFilePath.getParent())
          || !Files.isWritable(hashFilePath.getParent())
          || !filmlistManager.writeHashFile(filmlist, hashFilePath)) {
        printMessage(ServerMessages.FILMLIST_HASH_FILE_CANT_WRITE, hashFilePath.toString());
      }
    }
  }

  public void writeIdFile() {
    if (config.getWriteFilmlistIdFileEnabled()) {
      final Path idFilePath =
          filterPath(Paths.get(config.getFilmlistIdFilePath())).toAbsolutePath();
      if (!Files.exists(idFilePath.getParent())
          || !Files.isWritable(idFilePath.getParent())
          || !filmlistManager.writeIdFile(filmlist, idFilePath)) {
        printMessage(ServerMessages.FILMLIST_ID_FILE_CANT_WRITE, idFilePath.toString());
      }
    }
  }

  private void initializeCrawler(final MServerConfigManager rootConfig) {
    crawlerMap.put(
        Sender.ARD, new ArdCrawler(forkJoinPool, messageListeners, progressListeners, rootConfig));
    crawlerMap.put(
        Sender.ARTE_DE,
        new ArteCrawler(forkJoinPool, messageListeners, progressListeners, rootConfig));
    crawlerMap.put(
        Sender.ARTE_FR,
        new ArteCrawler_FR(forkJoinPool, messageListeners, progressListeners, rootConfig));
    crawlerMap.put(
        Sender.ARTE_EN,
        new ArteCrawler_EN(forkJoinPool, messageListeners, progressListeners, rootConfig));
    crawlerMap.put(
        Sender.ARTE_PL,
        new ArteCrawler_PL(forkJoinPool, messageListeners, progressListeners, rootConfig));
    crawlerMap.put(
        Sender.ARTE_IT,
        new ArteCrawler_IT(forkJoinPool, messageListeners, progressListeners, rootConfig));
    crawlerMap.put(
        Sender.ARTE_ES,
        new ArteCrawler_ES(forkJoinPool, messageListeners, progressListeners, rootConfig));
    crawlerMap.put(
        Sender.BR, new BrCrawler(forkJoinPool, messageListeners, progressListeners, rootConfig));
    crawlerMap.put(
        Sender.DREISAT,
        new DreiSatCrawler(forkJoinPool, messageListeners, progressListeners, rootConfig));
    crawlerMap.put(
        Sender.FUNK,
        new FunkCrawler(forkJoinPool, messageListeners, progressListeners, rootConfig));
    crawlerMap.put(
        Sender.HR, new HrCrawler(forkJoinPool, messageListeners, progressListeners, rootConfig));
    crawlerMap.put(
        Sender.KIKA,
        new KikaApiCrawler(forkJoinPool, messageListeners, progressListeners, rootConfig));
    crawlerMap.put(
        Sender.DW, new DwCrawler(forkJoinPool, messageListeners, progressListeners, rootConfig));
    crawlerMap.put(
        Sender.ORF, new OrfCrawler(forkJoinPool, messageListeners, progressListeners, rootConfig));
    crawlerMap.put(
        Sender.PHOENIX,
        new PhoenixCrawler(forkJoinPool, messageListeners, progressListeners, rootConfig));
    crawlerMap.put(
        Sender.SRF, new SrfCrawler(forkJoinPool, messageListeners, progressListeners, rootConfig));
    crawlerMap.put(
        Sender.SR, new SrCrawler(forkJoinPool, messageListeners, progressListeners, rootConfig));
    crawlerMap.put(
        Sender.ZDF, new ZdfCrawler(forkJoinPool, messageListeners, progressListeners, rootConfig));
  }

  private void runCrawlers(final AbstractCrawler... aCrawlers) {
    try {
      final List<Future<Set<Film>>> results = executorService.invokeAll(Arrays.asList(aCrawlers));
      for (final Future<Set<Film>> result : results) {
        Optional.ofNullable(result.get()).ifPresent(filmlist::addAllFilms);
      }
    } catch (final ExecutionException | InterruptedException exception) {
      stop();
      printMessage(ServerMessages.SERVER_ERROR);
      LOG.fatal("Something went wrong while exeuting the crawlers.", exception);
      Thread.currentThread().interrupt();
    }
  }
}
