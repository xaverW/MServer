package de.mediathekview.mserver.crawler.arte;

import java.util.Collection;
import java.util.concurrent.ForkJoinPool;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

public class ArteCrawler_PL extends ArteCrawler {

  public ArteCrawler_PL(final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.ARTE_PL;
  }

  @Override
  protected ArteLanguage getLanguage() {
    return ArteLanguage.PL;
  }

}