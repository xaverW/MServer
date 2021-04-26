package de.mediathekview.mserver.crawler.kika;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.kika.json.KikaApiFilmDto;
import de.mediathekview.mserver.crawler.kika.tasks.*;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class KikaApiCrawler extends AbstractCrawler {
  private static final Logger LOG = LogManager.getLogger(KikaApiCrawler.class);

  public KikaApiCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager aRootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, aRootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.KIKA;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {

    try {
      // get all brands from json doc
      final Queue<CrawlerUrlDTO> root = new ConcurrentLinkedQueue<CrawlerUrlDTO>();
      root.add(new CrawlerUrlDTO(KikaApiConstants.OVERVIEW));
      final KikaApiOverviewTask aKikaApiTopicOverviewTask = new KikaApiOverviewTask(this, root, 0);
      final Queue<TopicUrlDTO> topics = new ConcurrentLinkedQueue<TopicUrlDTO>();
      topics.addAll(aKikaApiTopicOverviewTask.fork().join());
      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_COUNT, getSender().getName(), topics.size());
      //
      // go through each topic and get all episodes for this topic
      final KikaApiTopicTask aKikaApiTopicTask = new KikaApiTopicTask(this, topics, 0);
      final Queue<KikaApiFilmDto> episodes = new ConcurrentLinkedQueue<KikaApiFilmDto>();
      episodes.addAll(aKikaApiTopicTask.fork().join());
      //
      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), episodes.size());
      getAndSetMaxCount(episodes.size());
      //
      // get all video urls for this episode
      return new KikaApiFilmTask(this, episodes);
    } catch (final Exception ex) {
      LOG.fatal("Exception in KIKA crawler.", ex);
    }

    return null;
  }

}
