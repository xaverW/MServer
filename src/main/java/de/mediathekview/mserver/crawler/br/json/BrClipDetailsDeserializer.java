package de.mediathekview.mserver.crawler.br.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.br.data.BrClipType;
import de.mediathekview.mserver.crawler.br.data.BrGraphQLElementNames;
import de.mediathekview.mserver.crawler.br.data.BrGraphQLNodeNames;
import de.mediathekview.mserver.crawler.br.data.BrID;
import de.mediathekview.mserver.crawler.br.graphql.GsonGraphQLHelper;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import mServer.crawler.CrawlerTool;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BrClipDetailsDeserializer implements JsonDeserializer<Optional<Film>> {

  private static final String DEFAULT_BR_VIDEO_URL_PRAEFIX = "https://www.br.de/mediathek/video/";
  private static final Logger LOG = LogManager.getLogger(BrClipDetailsDeserializer.class);

  private AbstractCrawler crawler;
  private BrID id;

  public BrClipDetailsDeserializer(AbstractCrawler crawler, BrID id) {
    super();
    this.crawler = crawler;
    this.id = id;
  }

  /*
   * Pseudonymized Example to see the Nodes filled
   *
   * {
   *   "data": {
   *     "viewer": {
   *       "clipDetails": {
   *         "__typename": "Programme",
   *         "id": "av:5a0603ce8c16b90012f4bc49",
   *         "title": "Der Titel ist der \"Hammer\"",
   *         "kicker": "Hammertime vom 25. Oktober",
   *         "duration": 844,
   *         "ageRestriction": 0,
   *         "description": "Dies ist ein Typoblindtext. An ihm kann man sehen, ob alle Buchstaben da sind und wie sie aussehen. Manchmal benutzt man Worte wie Hamburgefonts, Rafgenduks oder Handgloves, um Schriften zu testen.\n\nManchmal Sätze, die alle Buchstaben des Alphabets enthalten - man nennt diese Sätze »Pangrams«. Sehr bekannt ist dieser: The quick brown fox jumps over the lazy old dog.\n\nOft werden in Typoblindtexte auch fremdsprachige Satzteile eingebaut (AVAIL® and Wefox™ are testing aussi la Kerning), um dieWirkung in anderen Sprachen zu testen. In Lateinisch sieht zum Beispiel fast jede Schrift gut aus.\n\nQuod erat demonstrandum. Seit 1975 fehlen in den meisten Testtexten die Zahlen, weswegen nach TypoGb. 204 § ab dem Jahr 2034 Zahlen in 86 der Texte zur Pflicht werden.\n\nNichteinhaltung wird mit bis zu 245 € oder 368 $ bestraft. Genauso wichtig in sind mittlerweile auch Âçcèñtë, die in neueren Schriften aber fast immer enthalten sind. Ein wichtiges aber schwierig zu integrierendes Feld sindOpenType-Funktionalitäten.\n\nJe nach Software und Voreinstellungen können eingebaute Kapitälchen, Kerning oder Ligaturen (sehr pfiffig) nicht richtig dargestellt werden. Dies ist ein Typoblindtext. An ihm kann man sehen, ob alle Buchstaben da sind und wie sie aussehen.",
   *         "shortDescription": "Überall dieselbe alte Leier. Das Layout ist fertig, der Text lässt auf sich warten. Damit das Layout nun nicht nackt im Raume steht und sich klein und leer vorkommt, springe ich ein: der Blindtext. Genau zu diesem Zwecke erschaffen, immer im Schatten meines großen Bruders »Lorem Ipsum«, freue ich mich jedes Mal, wenn Sie ein paar Zeilen lesen.",
   *         "slug": "hammertime-vom-25-oktober-ein-wiedersehen-mit-tick-trick-und-track",
   *         "authors": {
   *           "count": 1,
   *           "edges": [
   *             {
   *               "node": {
   *                 "id": "av:59f0b9ebe9d83c0018fd63cb",
   *                 "name": "Dagobert Duck, Donald Duck, Daisy Duck"
   *               }
   *             }
   *           ]
   *         },
   *         "subjects": {
   *           "count": 0,
   *           "edges": []
   *         },
   *         "tags": {
   *           "count": 0,
   *           "edges": []
   *         },
   *         "executiveProducers": {
   *           "count": 0,
   *           "edges": []
   *         },
   *         "credits": {
   *           "count": 1,
   *           "edges": [
   *             {
   *               "node": {
   *                 "id": "av:59f0b9ebe9d83c0018fd63cb",
   *                 "name": "Dagobert Duck, Donald Duck, Daisy Duck"
   *               }
   *             }
   *           ]
   *         },
   *         "categorizations": {
   *           "count": 2,
   *           "edges": [
   *             {
   *               "node": {
   *                 "id": "av:http://ard.de/ontologies/categories#kultur"
   *               }
   *             },
   *             {
   *               "node": {
   *                 "id": "av:http://ard.de/ontologies/categories#kino"
   *               }
   *             }
   *           ]
   *         },
   *         "genres": {
   *           "count": 0,
   *           "edges": []
   *         },
   *         "videoFiles": {
   *           "count": 7,
   *           "edges": [
   *             {
   *               "node": {
   *                 "id": "av:59f0b9ebe9d83c0018fd63c5",
   *                 "publicLocation": "https://cdn-storage.br.de/MUJIuUOVBwQIbtCCBLzGiLC1uwQoNA4p_A0S/_AES/_A4G5A8H9U1S/fec59c2f-61ef-40de-99a0-305414ed10c6_X.mp4",
   *                 "accessibleIn": {
   *                   "count": 0,
   *                   "edges": []
   *                 },
   *                 "videoProfile": {
   *                   "id": "av:http://ard.de/ontologies/audioVideo#VideoProfile_HD",
   *                   "height": 720,
   *                   "width": 1280
   *                 }
   *               }
   *             },
   *             {
   *               "node": {
   *                 "id": "av:59f0b9ebe9d83c0018fd63c3",
   *                 "publicLocation": "https://cdn-storage.br.de/MUJIuUOVBwQIbtCCBLzGiLC1uwQoNA4p_A0S/_AES/_A4G5A8H9U1S/fec59c2f-61ef-40de-99a0-305414ed10c6_C.mp4",
   *                 "accessibleIn": {
   *                   "count": 0,
   *                   "edges": []
   *                 },
   *                 "videoProfile": {
   *                   "id": "av:http://ard.de/ontologies/audioVideo#VideoProfile_Premium",
   *                   "height": 540,
   *                   "width": 969
   *                 }
   *               }
   *             },
   *             {
   *               "node": {
   *                 "id": "av:59f0b9ebe9d83c0018fd63c4",
   *                 "publicLocation": "https://cdn-storage.br.de/MUJIuUOVBwQIbtCCBLzGiLC1uwQoNA4p_A0S/_AES/_A4G5A8H9U1S/fec59c2f-61ef-40de-99a0-305414ed10c6_E.mp4",
   *                 "accessibleIn": {
   *                   "count": 0,
   *                   "edges": []
   *                 },
   *                 "videoProfile": {
   *                   "id": "av:http://ard.de/ontologies/audioVideo#VideoProfile_Large",
   *                   "height": 360,
   *                   "width": 640
   *                 }
   *               }
   *             },
   *             {
   *               "node": {
   *                 "id": "av:59f0b9ebe9d83c0018fd63c2",
   *                 "publicLocation": "https://cdn-storage.br.de/MUJIuUOVBwQIbtCCBLzGiLC1uwQoNA4p_A0S/_AES/_A4G5A8H9U1S/fec59c2f-61ef-40de-99a0-305414ed10c6_B.mp4",
   *                 "accessibleIn": {
   *                   "count": 0,
   *                   "edges": []
   *                 },
   *                 "videoProfile": {
   *                   "id": "av:http://ard.de/ontologies/audioVideo#VideoProfile_Standard",
   *                   "height": 288,
   *                   "width": 512
   *                 }
   *               }
   *             },
   *             {
   *               "node": {
   *                 "id": "av:59f0b9ebe9d83c0018fd63c1",
   *                 "publicLocation": "https://cdn-storage.br.de/MUJIuUOVBwQIbtCCBLzGiLC1uwQoNA4p_A0S/_AES/_A4G5A8H9U1S/fec59c2f-61ef-40de-99a0-305414ed10c6_A.mp4",
   *                 "accessibleIn": {
   *                   "count": 0,
   *                   "edges": []
   *                 },
   *                 "videoProfile": {
   *                   "id": "av:http://ard.de/ontologies/audioVideo#VideoProfile_Mobile",
   *                   "height": 270,
   *                   "width": 480
   *                 }
   *               }
   *             },
   *             {
   *               "node": {
   *                 "id": "av:59f0b9ebe9d83c0018fd63c0",
   *                 "publicLocation": "https://cdn-storage.br.de/MUJIuUOVBwQIbtCCBLzGiLC1uwQoNA4p_A0S/_AES/_A4G5A8H9U1S/fec59c2f-61ef-40de-99a0-305414ed10c6_0.mp4",
   *                 "accessibleIn": {
   *                   "count": 0,
   *                   "edges": []
   *                 },
   *                 "videoProfile": {
   *                   "id": "av:http://ard.de/ontologies/audioVideo#VideoProfile_Mobile_S",
   *                   "height": 180,
   *                   "width": 320
   *                 }
   *               }
   *             },
   *             {
   *               "node": {
   *                 "id": "av:59f0b9ebe9d83c0018fd63c6",
   *                 "publicLocation": "https://br-i.akamaihd.net/i/MUJIuUOVBwQIbtCCBLzGiLC1uwQoNA4p_A0S/_AES/_A4G5A8H9U1S/fec59c2f-61ef-40de-99a0-305414ed10c6_,0,A,B,E,C,X,.mp4.csmil/master.m3u8?__b__\u003d200",
   *                 "accessibleIn": {
   *                   "count": 0,
   *                   "edges": []
   *                 },
   *                 "videoProfile": {
   *                   "id": "av:http://ard.de/ontologies/audioVideo#VideoProfile_HLS"
   *                 }
   *               }
   *             }
   *           ]
   *         },
   *         "captionFiles": {
   *           "count": 0,
   *           "edges": []
   *         },
   *         "episodeOf": {
   *           "id": "av:584f4bfd3b467900117be493",
   *           "title": "hammertime",
   *           "kicker": "hammertime",
   *           "scheduleInfo": "Mittwochs um 00.15 Uhr im BR Fernsehen. Freitags 00.15 Uhr in ARD-alpha, Dienstags um 21.45 Uhr in 3sat",
   *           "shortDescription": "Hammertime - das Bauseminar für IT-Profis. Der richtige Umgang mit Werkzeugen jede Woche neues übers Heimwerkern."
   *         },
   *         "broadcasts": {
   *           "edges": [
   *             {
   *               "node": {
   *                 "__typename": "BroadcastEvent",
   *                 "start": "2017-10-25T22:15:00.000Z",
   *                 "id": "av:5a0603ce8c16b90012f4bc49|5a0603ce8c16b90012f4bc43"
   *               }
   *             }
   *           ]
   *         }
   *       },
   *       "id": "Viewer:__VIEWER"
   *     }
   *   }
   * }
   *
   *
   */
  @Override
  public Optional<Film> deserialize(
      JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {

    JsonObject rootObject = json.getAsJsonObject();

    if (GsonGraphQLHelper.checkForErrors(rootObject)) {
      throw new IllegalStateException(
          "Fehler beim auflösen des aktuellen Films mit ID: " + this.id.getId());
    }

    Optional<JsonObject> clipDetails = getClipDetailsNode(rootObject);
    if (clipDetails.isPresent()) {
      JsonObject clipDetailRoot = clipDetails.get();

      // Done
      Optional<String> titel = getTitel(clipDetailRoot);
      Optional<String> thema = getThema(clipDetailRoot);
      Optional<LocalDateTime> sendeZeitpunkt = getSendeZeitpunkt(clipDetailRoot);
      Optional<Duration> clipLaenge = getClipLaenge(clipDetailRoot);

      // Todo
      Optional<Set<URL>> subtitles = getSubtitles(clipDetailRoot);
      Optional<Map<Resolution, FilmUrl>> videoUrls = getVideos(clipDetailRoot);
      Collection<GeoLocations> geoLocations = getGeoLocations(videoUrls);
      Optional<String> beschreibung = getBeschreibung(clipDetailRoot);
      Optional<URL> webSite = getWebSite(clipDetailRoot);

      Optional<LocalDateTime> availableUntil = getAvailableUntil(clipDetailRoot);

      if (titel.isPresent() && thema.isPresent() && clipLaenge.isPresent()) {
        if (videoUrls.isPresent() && !videoUrls.get().isEmpty()) {
          Film currentFilm =
              new Film(
                  UUID.randomUUID(),
                  this.crawler.getSender(),
                  titel.get(),
                  thema.get(),
                  sendeZeitpunkt.orElse(null),
                  clipLaenge.get());

          if (videoUrls.isPresent()) {
            currentFilm.addAllUrls(videoUrls.get());
          }

          if (beschreibung.isPresent()) {
            currentFilm.setBeschreibung(beschreibung.get());
          }

          currentFilm.setWebsite(webSite);

          if (subtitles.isPresent()) {
            currentFilm.addAllSubtitleUrls(subtitles.get());
          }

          currentFilm.setGeoLocations(geoLocations);

          if (availableUntil.isPresent()) {

            // TODO: Hier wird in Zukunft geprüft wie mit den Filmen umgegangen wird...

          }

          return Optional.of(currentFilm);
        }
      } else {
        LOG.error(
            "Kein komplett gültiger Film: "
                + this.id.getId()
                + " Titel da? "
                + titel.isPresent()
                + " Thema da? "
                + thema.isPresent()
                + " Länge da? "
                + clipLaenge.isPresent());
      }
    }
    this.crawler.incrementAndGetErrorCount();
    return Optional.empty();
  }

  private Collection<GeoLocations> getGeoLocations(Optional<Map<Resolution, FilmUrl>> videoUrls) {
    if (videoUrls.isPresent()) {
      return CrawlerTool.getGeoLocations(
          Sender.BR, videoUrls.get().get(Resolution.NORMAL).toString());
    }

    return new ArrayList<>();
  }

  private Optional<JsonObject> getClipDetailsNode(JsonObject rootObject) {
    Optional<JsonObject> dataNodeOptional =
        GsonGraphQLHelper.getChildObjectIfExists(
            rootObject, BrGraphQLNodeNames.RESULT_ROOT_NODE.getName());
    if (!dataNodeOptional.isPresent()) {
      return Optional.empty();
    }
    JsonObject dataNode = dataNodeOptional.get();

    Optional<JsonObject> viewerNodeOptional =
        GsonGraphQLHelper.getChildObjectIfExists(
            dataNode, BrGraphQLNodeNames.RESULT_ROOT_BR_NODE.getName());
    if (!viewerNodeOptional.isPresent()) {
      return Optional.empty();
    }
    JsonObject viewerNode = viewerNodeOptional.get();

    return GsonGraphQLHelper.getChildObjectIfExists(
        viewerNode, BrGraphQLNodeNames.RESULT_CLIP_DETAILS_ROOT.getName());
  }

  private Optional<String> getTitel(JsonObject clipDetailRoot) {
    Optional<JsonPrimitive> titleElementOptional =
        GsonGraphQLHelper.getChildPrimitiveIfExists(
            clipDetailRoot, BrGraphQLElementNames.STRING_CLIP_TITLE.getName());
    if (!titleElementOptional.isPresent()) {
      return Optional.empty();
    }
    JsonPrimitive titleElement = titleElementOptional.get();

    return Optional.of(titleElement.getAsString());
  }

  private Optional<String> getThema(JsonObject clipDetailRoot) {

    /*
     * Ist der aktuelle Titel ein Programm wird versucht zu prüfen, ob der aktuelle Titel
     * Teil einer Serie ist und wenn das so ist, den entsprechenden Titel als Thema zurück
     * zu geben.
     */
    switch (this.id.getType()) {
      case PROGRAMME:
        Optional<JsonObject> episodeOfNode = getEpisodeOfNode(clipDetailRoot);
        if (episodeOfNode.isPresent()) {
          JsonObject episodeOf = episodeOfNode.get();

          Optional<JsonPrimitive> episodeOfTitleElementOptional =
              GsonGraphQLHelper.getChildPrimitiveIfExists(
                  episodeOf, BrGraphQLElementNames.STRING_CLIP_TITLE.getName());
          if (episodeOfTitleElementOptional.isPresent()) {

            JsonPrimitive episodeOfTitleElement = episodeOfTitleElementOptional.get();

            return Optional.of(episodeOfTitleElement.getAsString());
          }
        }
        break;
      case ITEM:
        Optional<JsonObject> itemOfNode = getItemOfNode(clipDetailRoot);
        if (itemOfNode.isPresent()) {
          JsonObject itemOf = itemOfNode.get();

          Optional<JsonPrimitive> itemOfTitleElementOptional =
              GsonGraphQLHelper.getChildPrimitiveIfExists(
                  itemOf, BrGraphQLElementNames.STRING_CLIP_TITLE.getName());
          if (itemOfTitleElementOptional.isPresent()) {

            JsonPrimitive itemOfTitleElement = itemOfTitleElementOptional.get();

            return Optional.of(itemOfTitleElement.getAsString());
          }
        }
        break;

      default:
        break;
    }

    /*
     * Wenn wir hier ankommen ist weder episodeOf noch itemOf gefüllt. Dann nehmen wir halt den kicker auch wenn der nicht
     * so gut ist ein Thema zu bilden. Aber besser wie gar nichts.
     */
    Optional<JsonPrimitive> kickerElementOptional =
        GsonGraphQLHelper.getChildPrimitiveIfExists(
            clipDetailRoot, BrGraphQLElementNames.STRING_CLIP_KICKER.getName());
    if (kickerElementOptional.isPresent()) {
      JsonPrimitive kickerElement = kickerElementOptional.get();

      return Optional.of(kickerElement.getAsString());
    }

    return Optional.empty();
  }

  private Optional<JsonObject> getEpisodeOfNode(JsonObject clipDetailRoot) {
    Optional<JsonObject> episodeOfNodeOptional =
        GsonGraphQLHelper.getChildObjectIfExists(
            clipDetailRoot, BrGraphQLNodeNames.RESULT_CLIP_EPISONEOF.getName());
    if (!episodeOfNodeOptional.isPresent()) {
      return Optional.empty();
    }
    JsonObject episodeOfNode = episodeOfNodeOptional.get();

    if (episodeOfNode.entrySet().isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(episodeOfNode);
  }

  private Optional<JsonObject> getItemOfNode(JsonObject clipDetailRoot) {

    Optional<JsonObject> itemOfRootNodeOptional =
        GsonGraphQLHelper.getChildObjectIfExists(
            clipDetailRoot, BrGraphQLNodeNames.RESULT_CLIP_ITEMOF.getName());
    if (!itemOfRootNodeOptional.isPresent()) {
      return Optional.empty();
    }
    JsonObject itemOfRootNode = itemOfRootNodeOptional.get();

    Optional<JsonArray> itemOfEdgesNodeOptional =
        GsonGraphQLHelper.getChildArrayIfExists(
            itemOfRootNode, BrGraphQLNodeNames.RESULT_NODE_EDGES.getName());
    if (!itemOfEdgesNodeOptional.isPresent()) {
      return Optional.empty();
    }
    JsonArray itemOfEdgesNode = itemOfEdgesNodeOptional.get();

    if (itemOfEdgesNode.size() == 0) {
      return Optional.empty();
    }

    if (itemOfEdgesNode.size() >= 1) {
      if (itemOfEdgesNode.size() > 1) {
        LOG.debug("ID hat mehr als ein itemOf-Node: " + this.id.getId());
      }
      JsonObject firstItemOfEdge = itemOfEdgesNode.get(0).getAsJsonObject();

      Optional<JsonObject> itemOfNodeOptional =
          GsonGraphQLHelper.getChildObjectIfExists(
              firstItemOfEdge, BrGraphQLNodeNames.RESULT_NODE.getName());
      if (!itemOfNodeOptional.isPresent()) {
        return Optional.empty();
      }

      return Optional.of(itemOfNodeOptional.get());
    }

    return Optional.empty();
  }

  private Optional<Duration> getClipLaenge(JsonObject clipDetailRoot) {

    Optional<JsonPrimitive> durationElementOptional =
        GsonGraphQLHelper.getChildPrimitiveIfExists(
            clipDetailRoot, BrGraphQLElementNames.INT_CLIP_DURATION.getName());
    if (!durationElementOptional.isPresent()) {
      return Optional.empty();
    }
    JsonPrimitive durationElement = durationElementOptional.get();

    return Optional.of(Duration.ofSeconds(durationElement.getAsInt()));
  }

  private Optional<LocalDateTime> getSendeZeitpunkt(JsonObject clipDetailRoot) {

    /*
     * Normale ITEMS besitzen keinen Ausstrahlungszeitpunkt, Programme normalerweise schon.
     */
    if (!this.id.getType().equals(BrClipType.PROGRAMME)) {
      return Optional.empty();
    }

    Optional<JsonObject> broadcastNodeElement = getFirstBroadcastNode(clipDetailRoot);
    if (broadcastNodeElement.isPresent()) {
      JsonObject broadcastNode = broadcastNodeElement.get();

      Optional<JsonPrimitive> startElementOptional =
          GsonGraphQLHelper.getChildPrimitiveIfExists(
              broadcastNode, BrGraphQLElementNames.STRING_CLIP_START.getName());
      if (!startElementOptional.isPresent()) {
        return Optional.empty();
      }

      JsonPrimitive startElement = startElementOptional.get();

      String startDateTimeString = startElement.getAsString();

      return Optional.of(brDateTimeString2LocalDateTime(startDateTimeString));
    }

    return Optional.empty();
  }

  private Optional<JsonObject> getFirstBroadcastNode(JsonObject clipDetailRoot) {

    Optional<JsonObject> broadcastBaseNodeOptional =
        GsonGraphQLHelper.getChildObjectIfExists(
            clipDetailRoot, BrGraphQLNodeNames.RESUTL_CLIP_BROADCAST_ROOT.getName());
    if (!broadcastBaseNodeOptional.isPresent()) {
      return Optional.empty();
    }
    JsonObject broadcastBaseNode = broadcastBaseNodeOptional.get();

    Optional<JsonArray> broadcastEdgeNodeOptional =
        GsonGraphQLHelper.getChildArrayIfExists(
            broadcastBaseNode, BrGraphQLNodeNames.RESULT_NODE_EDGES.getName());
    if (!broadcastEdgeNodeOptional.isPresent()) {
      return Optional.empty();
    }
    JsonArray broadcastEdgeNode = broadcastEdgeNodeOptional.get();

    if (broadcastEdgeNode.size() == 0) {
      return Optional.empty();
    }
    if (broadcastEdgeNode.size() >= 1) {
      if (broadcastEdgeNode.size() > 1) {
        LOG.debug("ID hat mehr als einen Broadcast-Node: " + this.id.getId());
      }
      JsonObject firstBroadcastEdgeElement = broadcastEdgeNode.get(0).getAsJsonObject();

      Optional<JsonObject> broadcastNodeElementOptional =
          GsonGraphQLHelper.getChildObjectIfExists(
              firstBroadcastEdgeElement, BrGraphQLNodeNames.RESULT_NODE.getName());

      if (!broadcastNodeElementOptional.isPresent()) {
        return Optional.empty();
      }

      return Optional.of(broadcastNodeElementOptional.get());
    }

    return Optional.empty();
  }

  private Optional<Map<Resolution, FilmUrl>> getVideos(JsonObject clipDetailRoot) {

    Optional<JsonObject> videoFilesOptional =
        GsonGraphQLHelper.getChildObjectIfExists(
            clipDetailRoot, BrGraphQLNodeNames.RESULT_CLIP_VIDEO_FILES.getName());
    if (!videoFilesOptional.isPresent()) {
      return Optional.empty();
    }
    JsonObject videoFiles = videoFilesOptional.get();

    Optional<JsonArray> videoFilesEdgesOptional =
        GsonGraphQLHelper.getChildArrayIfExists(
            videoFiles, BrGraphQLNodeNames.RESULT_NODE_EDGES.getName());
    if (!videoFilesEdgesOptional.isPresent()) {
      return Optional.empty();
    }
    JsonArray videoFilesEdges = videoFilesEdgesOptional.get();

    if (videoFilesEdges.size() == 0) {
      return Optional.empty();
    }

    Map<Resolution, FilmUrl> videoListe = new ConcurrentHashMap<>();

    videoFilesEdges.forEach(
        (JsonElement currentEdge) -> {
          if (currentEdge.isJsonObject()) {

            Optional<JsonObject> videoFilesEdgeNodeOptional =
                GsonGraphQLHelper.getChildObjectIfExists(
                    currentEdge.getAsJsonObject(), BrGraphQLNodeNames.RESULT_NODE.getName());
            if (videoFilesEdgeNodeOptional.isPresent()) {

              JsonObject videoFilesEdgeNode = videoFilesEdgeNodeOptional.get();

              Optional<JsonPrimitive> videoFileURLOptional =
                  GsonGraphQLHelper.getChildPrimitiveIfExists(
                      videoFilesEdgeNode, BrGraphQLElementNames.STRING_CLIP_URL.getName());

              Optional<JsonObject> accessibleInOptional =
                  GsonGraphQLHelper.getChildObjectIfExists(videoFilesEdgeNode, "accessibleIn");
              if (accessibleInOptional.isPresent()) {
                Optional<JsonPrimitive> countAccessibleInEdgesOptional =
                    GsonGraphQLHelper.getChildPrimitiveIfExists(
                        accessibleInOptional.get(), "count");
                if (countAccessibleInEdgesOptional.isPresent()) {
                  if (countAccessibleInEdgesOptional.get().getAsInt() > 0) {
                    LOG.debug(this.id.getId() + " hat Geoinformationen?!");
                  }
                }
              }

              Optional<JsonObject> videoFileProfileNodeOptional =
                  GsonGraphQLHelper.getChildObjectIfExists(
                      videoFilesEdgeNode, BrGraphQLNodeNames.RESULT_CLIP_VIDEO_PROFILE.getName());
              if (videoFileProfileNodeOptional.isPresent()) {
                JsonObject videoFileProfileNode = videoFileProfileNodeOptional.get();

                Optional<JsonPrimitive> videoProfileIDOptional =
                    GsonGraphQLHelper.getChildPrimitiveIfExists(
                        videoFileProfileNode, BrGraphQLElementNames.ID_ELEMENT.getName());

                if (videoFileURLOptional.isPresent() && videoProfileIDOptional.isPresent()) {

                  JsonPrimitive videoFileURL = videoFileURLOptional.get();
                  JsonPrimitive videoFileProfile = videoProfileIDOptional.get();

                  if (videoFileURL.isString() && videoFileProfile.isString()) {

                    // Nur hier haben wir sowohl eine gültige URL als auch ein VideoProfil um einen
                    // MapEintrag zu erzeugen!

                    Resolution resolution =
                        Resolution.getResolutionFromArdAudioVideoOrdinalsByProfileName(
                            videoFileProfile.getAsString());

                    URL videoURL;
                    try {
                      videoURL = new URL(videoFileURL.getAsString());
                      FilmUrl filmUrl = new FilmUrl(videoURL, 0L);

                      if (!videoListe.containsKey(resolution)) {
                        videoListe.put(resolution, filmUrl);
                      }

                    } catch (MalformedURLException e) {
                      // Nothing to be done here
                      LOG.error(
                          "Fehlerhafte URL in den VideoURLs vorhanden! Clip-ID: "
                              + this.id.getId());
                    }
                  }
                }
              }
            }
          }
        });

    if (videoListe.size() > 0) {
      return Optional.of(videoListe);
    }
    LOG.error("Erzeugung der VideoURLs fehlgeschlagen für ID: " + this.id.getId());
    return Optional.empty();
  }

  private Optional<String> getBeschreibung(JsonObject clipDetailRoot) {

    Optional<JsonPrimitive> descriptionOptional =
        GsonGraphQLHelper.getChildPrimitiveIfExists(
            clipDetailRoot, BrGraphQLElementNames.STRING_CLIP_DESCRIPTION.getName());
    if (descriptionOptional.isPresent()) {

      JsonPrimitive description = descriptionOptional.get();
      if (description.isString() && StringUtils.isNotEmpty(description.getAsString())) {
        return Optional.of(description.getAsString());
      }
    }

    Optional<JsonPrimitive> shortDescriptionOptional =
        GsonGraphQLHelper.getChildPrimitiveIfExists(
            clipDetailRoot, BrGraphQLElementNames.STRING_CLIP_SHORT_DESCRIPTION.getName());
    if (shortDescriptionOptional.isPresent()) {

      JsonPrimitive shortDescription = shortDescriptionOptional.get();
      if (shortDescription.isString() && StringUtils.isNotEmpty(shortDescription.getAsString())) {
        return Optional.of(shortDescription.getAsString());
      }
    }

    return Optional.empty();
  }

  private Optional<URL> getWebSite(JsonObject clipDetailRoot) {

    Optional<JsonPrimitive> slugOptional =
        GsonGraphQLHelper.getChildPrimitiveIfExists(
            clipDetailRoot, BrGraphQLElementNames.STRING_CLIP_SLUG.getName());
    if (slugOptional.isPresent()) {

      JsonPrimitive slug = slugOptional.get();

      if (slug.isString() && StringUtils.isNotEmpty(slug.getAsString())) {
        try {
          return Optional.of(
              new URL(DEFAULT_BR_VIDEO_URL_PRAEFIX + slug.getAsString() + "-" + this.id.getId()));
        } catch (MalformedURLException e) {
          // Wird ein Empty!
        }
      }
    }

    return Optional.empty();
  }

  private Optional<Set<URL>> getSubtitles(JsonObject clipDetailRoot) {

    Optional<JsonObject> captionFilesOptional =
        GsonGraphQLHelper.getChildObjectIfExists(
            clipDetailRoot, BrGraphQLNodeNames.RESULT_CLIP_CAPTION_FILES.getName());
    if (!captionFilesOptional.isPresent()) {
      return Optional.empty();
    }

    JsonObject captionFiles = captionFilesOptional.get();

    Optional<JsonArray> captionFilesEdgesOptional =
        GsonGraphQLHelper.getChildArrayIfExists(
            captionFiles, BrGraphQLNodeNames.RESULT_NODE_EDGES.getName());
    if (!captionFilesEdgesOptional.isPresent()) {
      return Optional.empty();
    }

    JsonArray captionFilesEdges = captionFilesEdgesOptional.get();
    if (captionFilesEdges.size() == 0) {
      return Optional.empty();
    }

    Set<URL> subtitleUrls = new HashSet<>();

    captionFilesEdges.forEach(
        (JsonElement currentEdge) -> {
          if (currentEdge.isJsonObject()) {

            Optional<JsonObject> captionFilesNodeOptional =
                GsonGraphQLHelper.getChildObjectIfExists(
                    currentEdge.getAsJsonObject(), BrGraphQLNodeNames.RESULT_NODE.getName());
            if (captionFilesNodeOptional.isPresent()) {

              JsonObject captionFilesNode = captionFilesNodeOptional.get();

              Optional<JsonPrimitive> publicLocationOptional =
                  GsonGraphQLHelper.getChildPrimitiveIfExists(
                      captionFilesNode, BrGraphQLElementNames.STRING_CLIP_URL.getName());
              if (publicLocationOptional.isPresent()) {
                JsonPrimitive captionFileUrl = publicLocationOptional.get();

                if (captionFileUrl.isString()) {

                  try {
                    subtitleUrls.add(new URL(captionFileUrl.getAsString()));
                  } catch (MalformedURLException e) {
                    // Keine gültige URL kein Eintrag fürs Set
                  }
                }
              }
            }
          }
        });

    if (!subtitleUrls.isEmpty()) {
      return Optional.of(subtitleUrls);
    }

    return Optional.empty();
  }

  private Optional<LocalDateTime> getAvailableUntil(JsonObject clipDetailRoot) {

    Optional<JsonPrimitive> availableUntilOptional =
        GsonGraphQLHelper.getChildPrimitiveIfExists(
            clipDetailRoot, BrGraphQLElementNames.STRING_CLIP_AVAILABLE_UNTIL.getName());
    if (!availableUntilOptional.isPresent()) {
      return Optional.empty();
    }

    JsonPrimitive availableUntil = availableUntilOptional.get();

    if (availableUntil.isString() && StringUtils.isNoneEmpty(availableUntil.getAsString())) {
      return Optional.of(brDateTimeString2LocalDateTime(availableUntil.getAsString()));
    }

    return Optional.empty();
  }

  private LocalDateTime brDateTimeString2LocalDateTime(String dateTimeString) {

    ZonedDateTime inputDateTime =
        ZonedDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME);

    return inputDateTime.withZoneSameInstant(ZoneId.of("Europe/Berlin")).toLocalDateTime();
  }
}
