package de.mediathekview.mserver.crawler.zdf.parser;

import de.mediathekview.mserver.base.utils.UrlParseException;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static de.mediathekview.mserver.base.HtmlConsts.ATTRIBUTE_HREF;

public class ZdfTopicPageHtmlDeserializer {
  private static final Logger LOG = LogManager.getLogger(ZdfTopicPageHtmlDeserializer.class);

  private static final String HEADLINES = "article.b-cluster";
  private static final String HEADLINES2 = "section.b-content-teaser-list";
  private static final String LINK_SELECTOR1 = "article.b-content-teaser-item h3 a";
  private static final String LINK_SELECTOR2 = "article.b-cluster-teaser h3 a";
  private static final String TEASER_SELECTOR = "div.b-cluster-teaser.lazyload";
  private static final String MAIN_VIDEO_SELECTOR = "div.b-playerbox";

  private static final String[] BLACKLIST_HEADLINES =
      new String[] {
        "Comedy",
        "neoriginal",
        "Komödien",
        "Beliebte ",
        "Mehr ",
        "Krimis",
        "Shows",
        "Weitere Dokus",
        "True Crime",
        "- Kommissare",
        "-Serien",
        "Spannung in Spielfilmlänge",
        "Reihen am",
        "Weitere Fernsehfilme",
        "Weitere funk",
        "Die Welt von",
        "Weitere Filme",
        "Alle Samstagskrimis",
        "Alle Freitagskrimis",
        "Alle SOKOs",
        "Weitere SOKOs",
        "Weitere Thriller",
        "Direkt zu",
        "Das könnte",
        "Auch interessant"
      };

  private final String urlApiBase;

  public ZdfTopicPageHtmlDeserializer(final String urlApiBase) {
    this.urlApiBase = urlApiBase;
  }

  public Set<CrawlerUrlDTO> deserialize(final Document document) {
    final Set<CrawlerUrlDTO> results = new HashSet<>();

    final Elements mainVideos = document.select(MAIN_VIDEO_SELECTOR);
    mainVideos.forEach(
        mainVideo -> {
          final String id = mainVideo.attr("data-zdfplayer-id");
          if (id != null) {
            final String url = String.format(ZdfConstants.URL_FILM_JSON, urlApiBase, id);
            results.add(new CrawlerUrlDTO(url));
          }
        });

    final Elements headlines = document.select(HEADLINES);
    headlines.addAll(document.select(HEADLINES2));
    headlines.forEach(
        headline -> {
          Element x = headline.select("h2").first();
          if (x != null) {

            if (Arrays.stream(BLACKLIST_HEADLINES)
                .noneMatch(blacklistEntry -> x.text().contains(blacklistEntry))) {
              LOG.info(x.text());

              Elements filmUrls = headline.select(LINK_SELECTOR1);
              filmUrls.addAll(headline.select(LINK_SELECTOR2));
              filmUrls.forEach(
                  filmUrlElement -> {
                    final String href = filmUrlElement.attr(ATTRIBUTE_HREF);
                    final Optional<String> url = buildFilmUrlJsonFromHtmlLink(href);
                    url.ifPresent(u -> results.add(new CrawlerUrlDTO(u)));
                  });

              Elements teasers = headline.select(TEASER_SELECTOR);
              teasers.forEach(
                  teaserElement -> {
                    final String teaserUrl = teaserElement.attr("data-teaser-xhr-url");
                    final Optional<String> sophoraId;
                    try {
                      sophoraId = UrlUtils.getUrlParameterValue(teaserUrl, "sophoraId");
                      sophoraId.ifPresent(s -> results.add(
                              new CrawlerUrlDTO(
                                      String.format(
                                              ZdfConstants.URL_FILM_JSON, urlApiBase, s))));
                    } catch (UrlParseException e) {
                      e.printStackTrace();
                    }
                  });
            }
          } else {
            LOG.info("no h2");
          }
        });

    return results;
  }

  private Optional<String> buildFilmUrlJsonFromHtmlLink(String attr) {
    return UrlUtils.getFileName(attr)
        .map(s -> String.format(ZdfConstants.URL_FILM_JSON, urlApiBase, s.split("\\.")[0]));
  }
}
