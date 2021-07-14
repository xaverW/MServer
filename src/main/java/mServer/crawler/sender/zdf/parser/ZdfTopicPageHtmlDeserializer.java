package mServer.crawler.sender.zdf.parser;

import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.UrlParseException;
import mServer.crawler.sender.base.UrlUtils;
import mServer.crawler.sender.zdf.ZdfConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ZdfTopicPageHtmlDeserializer {
  private static final Logger LOG = LogManager.getLogger(ZdfTopicPageHtmlDeserializer.class);
  private static final String HEADLINES = "article.b-cluster";
  private static final String HEADLINES2 = "section.b-content-teaser-list";
  private static final String LINK_SELECTOR1 = "article.b-content-teaser-item h3 a";
  private static final String LINK_SELECTOR2 = "article.b-cluster-teaser h3 a";
  private static final String TEASER_SELECTOR = "div.b-cluster-teaser.lazyload";
  private static final String MAIN_VIDEO_SELECTOR = "div.b-playerbox";

  private static final String[] BLACKLIST_HEADLINES =
          new String[]{
                  "Comedy",
                  "neoriginal",
                  "Komödien",
                  "Beliebte ",
                  "Mehr zum Herzkino",
                  "Mehr Samstagskrimis",
                  "Mehr tolle Filme",
                  "Mehr zum Thema",
                  "Mehr zur SOKO",
                  "Mehr Talk und Show",
                  "Mehr ZDFkultur",
                  "Mehr bei ZDFkultur",
                  "Mehr von ZDFkultur",
                  "Mehr Unterhaltung in Spielfilmlänge",
                  "Mehr Doku-Themen",
                  "Mehr Wissenssendungen",
                  "Mehr Bier",
                  "Mehr Zweiteiler",
                  "Mehr Wissenschaft",
                  "Mehr Quiz und Show",
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
                  "Auch interessant",
                  "Alle Herzkino",
                  "Film-Highlights"
          };
  private static final String ATTRIBUTE_HREF = "href";

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

                  parseHeadline(results, headline);
                }
              } else {
                parseHeadline(results, headline);
              }
            });

    return results;
  }

  private void parseHeadline(Set<CrawlerUrlDTO> results, Element headline) {
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
                sophoraId.ifPresent(
                        s ->
                                results.add(
                                        new CrawlerUrlDTO(
                                                String.format(ZdfConstants.URL_FILM_JSON, urlApiBase, s))));
              } catch (UrlParseException e) {
                LOG.error(e);
              }
            });
  }

  private Optional<String> buildFilmUrlJsonFromHtmlLink(String attr) {
    return UrlUtils.getFileName(attr)
            .map(s -> String.format(ZdfConstants.URL_FILM_JSON, urlApiBase, s.split("\\.")[0]));
  }
}
