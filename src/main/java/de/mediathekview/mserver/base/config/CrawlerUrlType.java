/*
 * CrawlerUrlType.java
 *
 * Projekt    : MServer
 * erstellt am: 19.11.2017
 * Autor      : Sascha
 *
 */
package de.mediathekview.mserver.base.config;

import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public enum CrawlerUrlType {
  BR_API_URL("https://proxy-base.master.mango.express/graphql"),
  FUNK_WEBSITE("https://www.funk.net"),
  FUNK_API_URL("https://www.funk.net/api/v4.0/"),
  NEXX_CLUD_API_URL("https://api.nexx.cloud/v3/741"),
  KIKA_API_URL("https://prod.kinderplayer.cdn.tvnext.tv");

  private URL defaultUrl;

  CrawlerUrlType(final String urlText) {
    try {
      if (StringUtils.isNotEmpty(urlText)) {
        defaultUrl = new URL(urlText);
      }
    } catch (final MalformedURLException e) {
      defaultUrl = null;
    }
  }

  public Optional<URL> getDefaultUrl() {
    return Optional.ofNullable(defaultUrl);
  }
}
