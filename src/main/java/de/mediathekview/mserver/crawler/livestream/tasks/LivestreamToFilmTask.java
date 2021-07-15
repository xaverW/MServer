package de.mediathekview.mserver.crawler.livestream.tasks;

import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Queue;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;

public class LivestreamToFilmTask extends AbstractRecursiveConverterTask<Film, TopicUrlDTO>{
  private static final long serialVersionUID = -2729059830661477520L;
  private static final Logger LOG = LogManager.getLogger(LivestreamToFilmTask.class);

  // REFERENCES
  // lookup name, a SenderInfo Object ( Sender, Title, useThisStream)
  private static final Hashtable<String, SenderInfo> senderLookup = new Hashtable<>();
  static {
    senderLookup.put("3sat im Livestream",new SenderInfo(Sender.DREISAT, "3Sat im Livestream", false));
    senderLookup.put("3sat Livestream",new SenderInfo(Sender.DREISAT, "3Sat Livestream", true));
    senderLookup.put("ARD-alpha Livestream",new SenderInfo(Sender.ARD_ALPHA, "ARD Alpha Livestream", true));
    senderLookup.put("arte im Livestream",new SenderInfo(Sender.ARTE_DE, "ARTE.DE Livestream", false));
    senderLookup.put("ARTE Livestream französisch",new SenderInfo(Sender.ARTE_FR, "ARTE.FR Livestream", true));
    senderLookup.put("ARTE Livestream",new SenderInfo(Sender.ARTE_DE, "ARTE.DE Livestream", true));
    senderLookup.put("BR Fernsehen Nord",new SenderInfo(Sender.BR, "BR Nord Livestreamn", true));
    senderLookup.put("BR Fernsehen Süd",new SenderInfo(Sender.BR, "BR Süd Livestreamn", true));
    senderLookup.put("Das Erste",new SenderInfo(Sender.ARD, "ARD Livestreamn", true));
    senderLookup.put("Das ZDF im Livestream",new SenderInfo(Sender.ZDF, "ZDF Livestream", true));
    senderLookup.put("Deutsche Welle (DW) - Die mediale Stimme Deutschlands",new SenderInfo(Sender.DW, "DW Livestreamn", true));
    senderLookup.put("hr-fernsehen",new SenderInfo(Sender.HR, "HR Livestreamn", true));
    senderLookup.put("KiKA im Livestream",new SenderInfo(Sender.KIKA, "KiKA im Livestream", false));
    senderLookup.put("KiKA Livestream",new SenderInfo(Sender.KIKA, "KiKA Livestream", true));
    senderLookup.put("MDR Fernsehen Sachsen",new SenderInfo(Sender.MDR, "MDR Fernsehen Sachsen Livestream", true));
    senderLookup.put("MDR Fernsehen Sachsen-Anhalt",new SenderInfo(Sender.MDR, "MDR Sachsen-Anhalt Livestream", true));
    senderLookup.put("MDR Fernsehen Thüringen",new SenderInfo(Sender.MDR, "MDR Thüringen Livestream", true));
    senderLookup.put("NDR Fernsehen Hamburg",new SenderInfo(Sender.NDR, "NDR Hamburg Livestream", true));
    senderLookup.put("NDR Fernsehen Mecklenburg-Vorpommern",new SenderInfo(Sender.NDR, "NDR Mecklenburg-Vorpommern Livestream", true));
    senderLookup.put("NDR Fernsehen Schleswig-Holstein",new SenderInfo(Sender.NDR, "NDR Schleswig-Holstein Livestream", true));
    senderLookup.put("NDR Fernsehen",new SenderInfo(Sender.NDR, "NDR Livestream", true));
    senderLookup.put("ONE im Livestream",new SenderInfo(Sender.ONE, "ONE Livestream", true));
    senderLookup.put("ORF 1",new SenderInfo(Sender.ORF, "ORF 1 Livestream", true));
    senderLookup.put("ORF 2",new SenderInfo(Sender.ORF, "ORF 2 Livestream", true));
    senderLookup.put("ORF 3",new SenderInfo(Sender.ORF, "ORF 3 Livestream", true));
    senderLookup.put("ORF Sport",new SenderInfo(Sender.ORF, "ORF Sport Livestream", true));
    senderLookup.put("Phoenix im Livestream",new SenderInfo(Sender.PHOENIX, "Phoenix Livestream", false));
    senderLookup.put("phoenix live",new SenderInfo(Sender.PHOENIX, "Phoenix Livestream", true));
    senderLookup.put("rbb Fernsehen Berlin",new SenderInfo(Sender.RBB, "RBB Berlin Livestream", true));
    senderLookup.put("rbb Fernsehen Brandenburg",new SenderInfo(Sender.RBB, "RBB Brandenburg Livestream", true));
    senderLookup.put("SR Livestream",new SenderInfo(Sender.SR, "SR Livestream", true));
    senderLookup.put("SRF 1",new SenderInfo(Sender.SRF, "SRF 1 Livestream", true));
    senderLookup.put("SRF info",new SenderInfo(Sender.SRF, "SRF info Livestream", true));
    senderLookup.put("SRF zwei",new SenderInfo(Sender.SRF, "SRF zwei Livestream", true));
    senderLookup.put("SWR Baden-Württemberg",new SenderInfo(Sender.SWR, "SWR BW Livestream", true));
    senderLookup.put("SWR Fernsehen Rheinland-Pfalz",new SenderInfo(Sender.SWR, "SWR RP Livestream", true));
    senderLookup.put("tagesschau24-Livestream",new SenderInfo(Sender.TAGESSCHAU24, "tagesschau24 Livestream", true));
    senderLookup.put("WDR Fernsehen im Livestream",new SenderInfo(Sender.WDR, "WDR Livestream", true));
    senderLookup.put("ZDFinfo im Livestream",new SenderInfo(Sender.ZDF, "ZDF.info Livestream", true));
    senderLookup.put("ZDFneo im Livestream",new SenderInfo(Sender.ZDF, "ZDF.öneo Livestream", true));
  }
  

  
  public LivestreamToFilmTask(AbstractCrawler aCrawler, Queue<TopicUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  @Override
  protected void processElement(TopicUrlDTO aElement) {
    SenderInfo info = senderLookup.get(aElement.getTopic());
    if (info != null && info.isPreferable()) {
      Film aFilm = new Film(
          UUID.randomUUID(),
          info.getSender(),
          info.getName(),
          "Livestream",
          null,
          null
          );
      try {
        aFilm.addUrl(Resolution.SMALL, new FilmUrl(aElement.getUrl(),0L));
      } catch (MalformedURLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      taskResults.add(aFilm);
      crawler.incrementAndGetActualCount();
    } 
    if (info == null) {
      LOG.error("Unknown LIVESTREAM \"{}\"",aElement.getTopic());
    }
    
  }

  @Override
  protected AbstractRecursiveConverterTask<Film, TopicUrlDTO> createNewOwnInstance(
      Queue<TopicUrlDTO> aElementsToProcess) {
    return new LivestreamToFilmTask(crawler, aElementsToProcess);
  }

  @Override
  protected Integer getMaxElementsToProcess() {
    return Integer.MAX_VALUE;
  }
  
  static class SenderInfo {
    private Sender sender;
    private String name;
    private boolean preferable;

    public SenderInfo(Sender sender, String name, boolean preferable) {
      super();
      this.sender = sender;
      this.name = name;
      this.preferable = preferable;
    }    
    
    public Sender getSender() {
      return sender;
    }
    public String getName() {
      return name;
    }
    public boolean isPreferable() {
      return preferable;
    }
  }

}
