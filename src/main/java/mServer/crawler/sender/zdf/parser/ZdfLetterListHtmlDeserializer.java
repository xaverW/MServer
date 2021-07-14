package mServer.crawler.sender.zdf.parser;

import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.UrlUtils;
import mServer.crawler.sender.zdf.ZdfConstants;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;

public class ZdfLetterListHtmlDeserializer {
  private static final String LINK_SELECTOR = "ul.letter-list li a";
  private static final String ATTRIBUTE_HREF = "href";

  public Set<CrawlerUrlDTO> deserialize(final Document document) {
    final Set<CrawlerUrlDTO> results = new HashSet<>();

    Elements filmUrls = document.select(LINK_SELECTOR);
    filmUrls.forEach(
            filmUrlElement -> {
              String url = filmUrlElement.attr(ATTRIBUTE_HREF);
              url = UrlUtils.addDomainIfMissing(url, ZdfConstants.URL_BASE);
              results.add(new CrawlerUrlDTO(url));
            });

    return results;
  }

}
