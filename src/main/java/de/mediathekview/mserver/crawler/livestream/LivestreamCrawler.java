package de.mediathekview.mserver.crawler.livestream;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.livestream.json.LivestreamArdStreamDeserializer;
import de.mediathekview.mserver.crawler.livestream.tasks.LivestreamArdOverviewTask;
import de.mediathekview.mserver.crawler.livestream.tasks.LivestreamArdStreamTask;
import de.mediathekview.mserver.crawler.livestream.tasks.LivestreamOrfOverviewTask;
import de.mediathekview.mserver.crawler.livestream.tasks.LivestreamOrfStreamTask;
import de.mediathekview.mserver.crawler.livestream.tasks.LivestreamSrfOverviewTask;
import de.mediathekview.mserver.crawler.livestream.tasks.LivestreamSrfStreamTask;
import de.mediathekview.mserver.crawler.livestream.tasks.LivestreamToFilmTask;
import de.mediathekview.mserver.crawler.livestream.tasks.LivestreamZdfOverviewTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class LivestreamCrawler extends AbstractCrawler {
  private static final DateTimeFormatter DAY_PAGE_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public LivestreamCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.LIVESTREAM;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    //
    // ARD
    //
    final Set<TopicUrlDTO> ardLivestreamsLinks;
    final LivestreamArdOverviewTask ard = new LivestreamArdOverviewTask(
        this, 
        new ConcurrentLinkedQueue<>( Arrays.asList( new CrawlerUrlDTO[] { new CrawlerUrlDTO(LivestreamConstants.URL_ARD_LIVESTREAMS) } ) ) );
    ardLivestreamsLinks = forkJoinPool.invoke(ard);
    //
    final Set<TopicUrlDTO> ardLivestreams;
    final LivestreamArdStreamTask lardS = new LivestreamArdStreamTask(this, new ConcurrentLinkedQueue<>(ardLivestreamsLinks));
    ardLivestreams = forkJoinPool.invoke(lardS);
    //ardLivestreams.forEach(a-> { System.out.println(a.getTopic() + " " + a.getUrl());});
    //
    // ZDF
    //
    final LocalDateTime now = LocalDateTime.now();
    final String dt = now.format(DAY_PAGE_DATE_FORMATTER);
    final Set<TopicUrlDTO> zdfLivestreams;
    final String zdfQueryUrl = String.format(LivestreamConstants.URL_ZDF_LIVESTREAMS, dt);
    final LivestreamZdfOverviewTask zdf = new LivestreamZdfOverviewTask(
        this, 
        new ConcurrentLinkedQueue<>( Arrays.asList( new CrawlerUrlDTO[] { new CrawlerUrlDTO(zdfQueryUrl) } ) ) );
    zdfLivestreams = forkJoinPool.invoke(zdf);
    //zdfLivestreams.forEach(a-> { System.out.println(a.getTopic() + " " + a.getUrl());});
    //
    // ORF
    //
    final Set<TopicUrlDTO> orfLivestreamsLinks;
    final LivestreamOrfOverviewTask orf = new LivestreamOrfOverviewTask(
        this, 
        new ConcurrentLinkedQueue<>( Arrays.asList( new CrawlerUrlDTO[] { new CrawlerUrlDTO(LivestreamConstants.URL_ORF_LIVESTREAMS) } ) ) );
    orfLivestreamsLinks = forkJoinPool.invoke(orf);
    //
    final Set<TopicUrlDTO> orfLivestreams;
    final LivestreamOrfStreamTask lorfs = new LivestreamOrfStreamTask(this, new ConcurrentLinkedQueue<>(orfLivestreamsLinks));
    orfLivestreams = forkJoinPool.invoke(lorfs);
    //orfLivestreams.forEach(a-> { System.out.println(a.getTopic() + " " + a.getUrl());});
    //
    // SRF
    //
    final Set<TopicUrlDTO> srfLivestreamsLinks;
    final LivestreamSrfOverviewTask srf = new LivestreamSrfOverviewTask(
        this, 
        new ConcurrentLinkedQueue<>( Arrays.asList( new CrawlerUrlDTO[] { new CrawlerUrlDTO(LivestreamConstants.URL_SRF_LIVESTREAMS) } ) ) );
    srfLivestreamsLinks = forkJoinPool.invoke(srf);
    //
    final Set<TopicUrlDTO> srfLivestreams;
    final LivestreamSrfStreamTask lsrfs = new LivestreamSrfStreamTask(this, new ConcurrentLinkedQueue<>(srfLivestreamsLinks));
    srfLivestreams = forkJoinPool.invoke(lsrfs);
    //srfLivestreams.forEach(a-> { System.out.println(a.getTopic() + " " + a.getUrl());});
    //
    HashSet<TopicUrlDTO> allLivestreams = new HashSet<TopicUrlDTO>();
    allLivestreams.addAll(ardLivestreams);
    allLivestreams.addAll(zdfLivestreams);
    allLivestreams.addAll(orfLivestreams);
    allLivestreams.addAll(srfLivestreams);
    
    return new LivestreamToFilmTask(this, new ConcurrentLinkedQueue<>(allLivestreams));
  }
}
