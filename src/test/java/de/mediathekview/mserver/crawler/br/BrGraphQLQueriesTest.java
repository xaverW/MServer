/*
 * BrGraphQLQueriesTest.java
 *
 * Projekt : MServer erstellt am: 17.11.2017 Autor : Sascha
 *
 */
package de.mediathekview.mserver.crawler.br;

import static org.junit.Assert.assertEquals;

import de.mediathekview.mserver.crawler.br.data.BrClipType;
import de.mediathekview.mserver.crawler.br.data.BrID;
import de.mediathekview.mserver.crawler.br.graphql.AbstractVariable;
import de.mediathekview.mserver.crawler.br.graphql.variables.BooleanVariable;
import de.mediathekview.mserver.crawler.br.graphql.variables.StringVariable;
import de.mediathekview.mserver.crawler.br.graphql.variables.VariableList;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.Test;

public class BrGraphQLQueriesTest {

  @After
  public void tearDown() {}

  @Test
  public void testQuery2GetFilmCount() {
    assertEquals(
        "{\"query\":\"query MediathekViewCountFilms(  $programmeFilter: ProgrammeFilter!) {  viewer {    ...on Viewer {      broadcastService(id: \\\"av:http://ard.de/ontologies/ard#BR_Fernsehen\\\") {        __typename        ...on BroadcastServiceInterface {          id          programmes(first: 1, orderBy: BROADCASTS_START_ASC, filter: $programmeFilter) {            count          }        }      }    }  }}\",\"variables\":{\"programmeFilter\":{\"status\":{\"id\":{\"eq\":\"av:http://ard.de/ontologies/lifeCycle#published\"}},\"broadcasts\":{\"start\":{\"gte\":\"1970-01-01T05:00:00.000Z\",\"lte\":\"2017-11-14T05:00:00.000Z\"}}}}}",
        BrGraphQLQueries.getQuery2GetFilmCount());
  }

  @Test
  public void testQuery2GetClipIDs() {
    ZonedDateTime dateTime =
        ZonedDateTime.of(LocalDateTime.of(2019, 4, 28, 13, 5, 22), ZoneId.of("Europe/Berlin"));

    assertEquals(
        "{\"query\":\"query MediathekViewGetClipIDs(  $triggerSearch: Boolean!  $clipCount: Int  $clipFilter: ClipFilter) {viewer {    searchAllClips: allClips(first: $clipCount, filter: $clipFilter) @include(if: $triggerSearch) {      count      pageInfo {        hasNextPage      }      edges {        node {          __typename          id        }        cursor      }    }    id  }}\",\"variables\":{\"triggerSearch\":true,\"clipCount\":1000,\"clipFilter\":{\"audioOnly\":{\"eq\":false},\"essences\":{\"empty\":{\"eq\":false}},\"availableUntil\":{\"gte\":\"2019-04-28T11:05:22Z\"}}}}",
        BrGraphQLQueries.getQuery2GetAllClipIds(dateTime, 1000, ""));
  }

  @Test
  public void testCreateHeaderWithOneStringVariable() {
    String queryTitle = "MediathekViewCountFilms";

    StringVariable sv = new StringVariable("programmeFilter", "text");
    List<AbstractVariable> rootList = new LinkedList<>();
    rootList.add(sv);
    VariableList vl = new VariableList(rootList);

    assertEquals(
        "query MediathekViewCountFilms(  $programmeFilter: String) {",
        BrGraphQLQueries.getGraphQLHeaderWithVariable(queryTitle, vl));
  }

  @Test
  public void testQuery2GetClipDetails() {
    BrID id = new BrID(BrClipType.PROGRAMME, "av:5a0603ce8c16b90012f4bc49");
    assertEquals(
        "{\"query\":\"query MediathekViewGetClipDetails(  $clipID: ID!) { viewer {  clipDetails: clip(id: $clipID) { __typename id title kicker duration ageRestriction description shortDescription slug availableUntil  authors { count  edges {  node { id name  }   }   }   subjects { count  edges {  node { id  }   }   }   tags { count  edges {  node { id label  }   }   }   executiveProducers { count  edges {  node { id name  }   }   }   credits { count  edges {  node { id name  }   }   }   categorizations { count  edges {  node { id  }   }   }   genres { count  edges {  node { id label  }   }   }   videoFiles(first: 50, orderBy: FILESIZE_DESC) { count  edges {  node { id publicLocation  accessibleIn(first: 50) { count  edges {  node { id baseIdPrefix  }   }   }   videoProfile { id height width  }   }   }   }   captionFiles(first: 50, orderBy: FILESIZE_DESC) { count  edges {  node { id publicLocation  }   }   }   ... on ItemInterface { availableUntil  itemOf { count  edges {  node { id title kicker  broadcasts(first: 1, orderBy: START_ASC) {  edges {  node { __typename start id  }   }   }   }   }   }   }   ... on ProgrammeInterface { episodeNumber  episodeOf { id title kicker scheduleInfo shortDescription  }   broadcasts(first: 1, orderBy: START_ASC) {  edges {  node { __typename start id  }   }   }   }   }  id  } }\",\"variables\":{\"clipID\":\"av:5a0603ce8c16b90012f4bc49\"}}",
        BrGraphQLQueries.getQuery2GetClipDetails(id));
  }

  @Test
  public void testFooterGenerator() {

    BooleanVariable bv = new BooleanVariable("isClip", true);
    List<AbstractVariable> rootList = new LinkedList<>();
    rootList.add(bv);
    VariableList rootElement = new VariableList(rootList);

    assertEquals(
        "\",\"variables\":{\"isClip\":true}}",
        BrGraphQLQueries.getGraphQLFooterWithVariable(rootElement));
  }
}
