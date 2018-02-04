﻿package de.mediathekview.mserver.crawler.orf;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.orf.tasks.OrfArchiveLetterPageTask;
import de.mediathekview.mserver.crawler.orf.tasks.OrfArchiveTopicTask;
import de.mediathekview.mserver.crawler.orf.tasks.OrfDayTask;
import de.mediathekview.mserver.crawler.orf.tasks.OrfFilmDetailTask;
import de.mediathekview.mserver.crawler.orf.tasks.OrfLetterPageTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

public class OrfCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(OrfCrawler.class);

  public OrfCrawler(final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.ORF;
  }

  private Set<OrfTopicUrlDTO> getArchiveEntries() throws InterruptedException, ExecutionException {
    final OrfArchiveLetterPageTask letterTask = new OrfArchiveLetterPageTask();
    final ConcurrentLinkedQueue<OrfTopicUrlDTO> topics = forkJoinPool.submit(letterTask).get();

    final OrfArchiveTopicTask topicTask = new OrfArchiveTopicTask(this, topics);
    final Set<OrfTopicUrlDTO> shows = forkJoinPool.submit(topicTask).get();

    printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(),
        shows.size());

    return shows;
  }

  private Set<OrfTopicUrlDTO> getDaysEntries() throws InterruptedException, ExecutionException {
    final OrfDayTask dayTask = new OrfDayTask(this, getDayUrls());
    final Set<OrfTopicUrlDTO> shows = forkJoinPool.submit(dayTask).get();

    printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(),
        shows.size());

    return shows;
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> getDayUrls() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    for (int i = 0; i < crawlerConfig.getMaximumDaysForSendungVerpasstSection()
        + crawlerConfig.getMaximumDaysForSendungVerpasstSectionFuture(); i++) {
      urls.add(new CrawlerUrlDTO(OrfConstants.URL_DAY + LocalDateTime.now()
          .plus(crawlerConfig.getMaximumDaysForSendungVerpasstSectionFuture(), ChronoUnit.DAYS)
          .minus(i, ChronoUnit.DAYS).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
    }

    return urls;
  }

  private ConcurrentLinkedQueue<OrfTopicUrlDTO> getLetterEntries()
      throws InterruptedException, ExecutionException {
    final OrfLetterPageTask letterTask = new OrfLetterPageTask();
    final ConcurrentLinkedQueue<OrfTopicUrlDTO> shows = forkJoinPool.submit(letterTask).get();

    printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(),
        shows.size());

    return shows;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    try {

      final ConcurrentLinkedQueue<TopicUrlDTO> shows = new ConcurrentLinkedQueue<>();

      shows.addAll(getArchiveEntries());
      shows.addAll(getLetterEntries());
      getDaysEntries().forEach(show -> {
        if (!shows.contains(show)) {
          shows.add(show);
        }
      });

      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(),
          shows.size());
      getAndSetMaxCount(shows.size());

      return new OrfFilmDetailTask(this, shows);
    } catch (InterruptedException | ExecutionException ex) {
      LOG.fatal("Exception in ORF crawler.", ex);
    }
    return null;
  }
  
  private ConcurrentLinkedQueue<CrawlerUrlDTO> getDayUrls() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    for (int i = 0; i < crawlerConfig.getMaximumDaysForSendungVerpasstSection(); i++) {
      urls.add(new CrawlerUrlDTO(OrfConstants.URL_DAY + 
          LocalDateTime.now().minus(i, ChronoUnit.DAYS).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
    }

    return urls;
  }
  
  private ConcurrentLinkedQueue<TopicUrlDTO> getLetterEntries() throws InterruptedException, ExecutionException {
    OrfLetterPageTask letterTask = new OrfLetterPageTask();
    ConcurrentLinkedQueue<TopicUrlDTO> shows = forkJoinPool.submit(letterTask).get();

    printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

    return shows;
  }
  
  private Set<TopicUrlDTO> getDaysEntries() throws InterruptedException, ExecutionException {
    OrfDayTask dayTask = new OrfDayTask(this, getDayUrls());
    Set<TopicUrlDTO> shows = forkJoinPool.submit(dayTask).get();

    printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());
    
    return shows;
  }
  
  private Set<TopicUrlDTO> getArchiveEntries() throws InterruptedException, ExecutionException {
      OrfArchiveLetterPageTask letterTask = new OrfArchiveLetterPageTask();
      ConcurrentLinkedQueue<TopicUrlDTO> topics = forkJoinPool.submit(letterTask).get();

      OrfArchiveTopicTask topicTask = new OrfArchiveTopicTask(this, topics);
      Set<TopicUrlDTO> shows = forkJoinPool.submit(topicTask).get();
      
      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());
    
      return shows;
  }
}
