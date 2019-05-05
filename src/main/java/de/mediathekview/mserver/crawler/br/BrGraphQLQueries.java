/*
 * BrGraphQLQueries.java
 * 
 * Projekt    : MServer
 * erstellt am: 17.11.2017
 * Autor      : Sascha
 * 
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import de.mediathekview.mserver.crawler.br.data.BrGraphQLElementNames;
import de.mediathekview.mserver.crawler.br.data.BrGraphQLNodeNames;
import de.mediathekview.mserver.crawler.br.data.BrID;
import de.mediathekview.mserver.crawler.br.graphql.AbstractVariable;
import de.mediathekview.mserver.crawler.br.graphql.variables.BooleanVariable;
import de.mediathekview.mserver.crawler.br.graphql.variables.FloatVariable;
import de.mediathekview.mserver.crawler.br.graphql.variables.IDVariable;
import de.mediathekview.mserver.crawler.br.graphql.variables.IntegerVariable;
import de.mediathekview.mserver.crawler.br.graphql.variables.RecursiveAbstractVariable;
import de.mediathekview.mserver.crawler.br.graphql.variables.StringVariable;
import de.mediathekview.mserver.crawler.br.graphql.variables.VariableList;

public class BrGraphQLQueries {


  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_INSTANT;
  private static final String JSON_GRAPHQL_HEADER = "{\"query\":\"";
    
    public static String getQuery2GetFilmCount() {
        
      StringVariable lteVariable = new StringVariable("lte", "2017-11-14T05:00:00.000Z");
      StringVariable gteVariable = new StringVariable("gte", "1970-01-01T05:00:00.000Z");
      List<AbstractVariable> startList = new LinkedList<>();
      startList.add(gteVariable);
      startList.add(lteVariable);
      VariableList startListVariable = new VariableList("start", startList);
      RecursiveAbstractVariable broadcastsVariable = new RecursiveAbstractVariable("broadcasts", startListVariable);
      
      StringVariable eqOntologyVariable = new StringVariable("eq", "av:http://ard.de/ontologies/lifeCycle#published");
      RecursiveAbstractVariable idEqOntologyVariable = new RecursiveAbstractVariable("id", eqOntologyVariable);
      RecursiveAbstractVariable statusIdEqOntologyVariable = new RecursiveAbstractVariable("status", idEqOntologyVariable);
      
      List<AbstractVariable> programmeFilterList = new LinkedList<>();
      programmeFilterList.add(statusIdEqOntologyVariable);
      programmeFilterList.add(broadcastsVariable);
      VariableList programmeFilterVariable = new VariableList("programmeFilter", programmeFilterList);
      programmeFilterVariable.setAsNotNullableType();
      
      List<AbstractVariable> variablesList = new LinkedList<>();
      variablesList.add(programmeFilterVariable);
      VariableList variables = new VariableList(variablesList);
      
      StringBuilder sb = new StringBuilder();
      
      sb.append(JSON_GRAPHQL_HEADER);
      sb.append(getGraphQLHeaderWithVariable("MediathekViewCountFilms",  variables));
        
      sb.append("  viewer {");
      sb.append("    ...on Viewer {");
      sb.append("      broadcastService(id: \\\"av:http://ard.de/ontologies/ard#BR_Fernsehen\\\") {");
      sb.append("        __typename");
      sb.append("        ...on BroadcastServiceInterface {");
      sb.append("          id");
      sb.append("          programmes(first: 1, orderBy: BROADCASTS_START_ASC, filter: $programmeFilter) {");
      sb.append("            count");
      sb.append("          }");
      sb.append("        }");
      sb.append("      }");
      sb.append("    }");
      sb.append("  }");
      sb.append("}");
      sb.append(getGraphQLFooterWithVariable(variables));
        
      return sb.toString();
        
    }
    
    public static String getQuery2GetClipDetails(BrID clipId) {

      boolean addAuthors            = true;
      boolean addSubjects           = true;
      boolean addTags               = true;
      boolean addExecutiveProducers = true;
      boolean addCredits            = true;
      boolean addCategorizations    = true;
      boolean addGenres             = true;
      boolean addCaptionFiles       = true;
      
      IDVariable clipID = new IDVariable("clipID", clipId.getId());
      clipID.setAsNotNullableType();
      List<AbstractVariable> variablesList = new LinkedList<>();
      variablesList.add(clipID);
      
      VariableList rootVariable = new VariableList(variablesList);
      
      String searchTitle = "MediathekViewGetClipDetails";
      
      StringBuilder sb = new StringBuilder();
      
      sb.append(JSON_GRAPHQL_HEADER);
      sb.append(getGraphQLHeaderWithVariable(searchTitle, rootVariable));
      sb.append(addObjectConstruct(BrGraphQLNodeNames.RESULT_ROOT_BR_NODE.getName()
          , addObjectConstruct("clipDetails: clip(id: $clipID)"
              , BrGraphQLElementNames.GRAPHQL_TYPE_ELEMENT.getName()
              , BrGraphQLElementNames.ID_ELEMENT.getName()
              , BrGraphQLElementNames.STRING_CLIP_TITLE.getName()
              , BrGraphQLElementNames.STRING_CLIP_KICKER.getName()
              , BrGraphQLElementNames.INT_CLIP_DURATION.getName()
              , "ageRestriction"
              , BrGraphQLElementNames.STRING_CLIP_DESCRIPTION.getName()
              , BrGraphQLElementNames.STRING_CLIP_SHORT_DESCRIPTION.getName()
              , BrGraphQLElementNames.STRING_CLIP_SLUG.getName()
              , BrGraphQLElementNames.STRING_CLIP_AVAILABLE_UNTIL.getName()
              , addAuthors()
              , addSubjects()
              , addTags()
              , addExecutiveProducers()
              , addCredits()
              , addCategorizations()
              , addGenres()
              , addVideoFiles()
              , addCaptionFiles()
              , addOnItemInterface()
              , addOnProgrammeInterface()
              )
          , "id"
          ));
      
      sb.append("}");
      
      sb.append(getGraphQLFooterWithVariable(rootVariable));      
      
      return sb.toString();
      
    }
    
    public static String getQuery2GetAllClipIds(ZonedDateTime minimumAvailableUntil, int clipCount, String cursor) {
      
      BooleanVariable triggerSearchVariable = new BooleanVariable("triggerSearch", true);
      triggerSearchVariable.setAsNotNullableType();
      
      IntegerVariable clipCountVariable = new IntegerVariable("clipCount", clipCount); 
      
      BooleanVariable eqFalseVariable = new BooleanVariable("eq", false);
      RecursiveAbstractVariable emptyEqFalseVariable = new RecursiveAbstractVariable("empty", eqFalseVariable);
      RecursiveAbstractVariable essencesEmptyEqFalseVariable = new RecursiveAbstractVariable("essences", emptyEqFalseVariable);
      
      RecursiveAbstractVariable audioOnlyEqFalseVariable = new RecursiveAbstractVariable("audioOnly", eqFalseVariable);

      StringVariable gtTodayVariable = new StringVariable("gte", minimumAvailableUntil.format(DATE_FORMATTER));
      RecursiveAbstractVariable videoAvailableVariable = new RecursiveAbstractVariable("availableUntil", gtTodayVariable);

      List<AbstractVariable> clipFilterList = new LinkedList<>();
      clipFilterList.add(audioOnlyEqFalseVariable);
      clipFilterList.add(essencesEmptyEqFalseVariable);
      clipFilterList.add(videoAvailableVariable);

      VariableList clipFilter = new VariableList("clipFilter", clipFilterList);
      
      List<AbstractVariable> variablesList = new LinkedList<>();
      variablesList.add(triggerSearchVariable);
      variablesList.add(clipCountVariable);
      variablesList.add(clipFilter);
      
      VariableList rootVariable = new VariableList(variablesList);
      
      String searchTitle = "MediathekViewGetClipIDs";
      
      StringBuilder sb = new StringBuilder();

      sb.append(JSON_GRAPHQL_HEADER);
      sb.append(getGraphQLHeaderWithVariable(searchTitle, rootVariable));
      sb.append("viewer {");
      sb.append("    searchAllClips: allClips(first: $clipCount, filter: $clipFilter");
      if(StringUtils.isNotEmpty(cursor)) {
        sb.append(", after:\\\"");
        sb.append(cursor);
        sb.append("\\\"");
      } else {
      }
      sb.append(") @include(if: $triggerSearch) {");
      sb.append("      count");
      sb.append("      pageInfo {");
      sb.append("        hasNextPage");
      sb.append("      }");
      sb.append("      edges {");
      sb.append("        node {");
      sb.append("          __typename");
      sb.append("          id");
      sb.append("        }");
      sb.append("        cursor");
      sb.append("      }");
      sb.append("    }");
      sb.append("    id");
      sb.append("  }");
      sb.append("}");
      
      sb.append(getGraphQLFooterWithVariable(rootVariable));
      
      return sb.toString();      
      
    }
    
    static String getGraphQLHeaderWithVariable(String queryName, VariableList rootElement) throws IllegalArgumentException {
        
      if(!rootElement.isRootElement())
        throw new IllegalArgumentException("Header basieren auf einer echten rootElement Liste. Bitte diese übergeben!");
      
      StringBuilder sb = new StringBuilder();

      sb.append("query ");
      sb.append(queryName);
      sb.append("(");
        
      List<AbstractVariable> elements = rootElement.getValue();
      
      elements.stream().forEach( (AbstractVariable v) -> {
        sb.append(getVariableNameAsJSON(v.getName()));
        if(v instanceof BooleanVariable) {
          sb.append("Boolean");
        } else if (v instanceof FloatVariable) {
          sb.append("Float");
        } else if (v instanceof IDVariable) {
          sb.append("ID");
        } else if (v instanceof IntegerVariable) {
          sb.append("Int");
        } else if (v instanceof StringVariable) {
          sb.append("String");
        } else {
          sb.append(v.getName().substring(0, 1).toUpperCase());
          sb.append(v.getName().substring(1, v.getName().length()));
        }
        if(v.isNotNullableType()) {
          sb.append("!");
        }
      });
        
      sb.append(") {");
      
      return sb.toString();
        
    }
    
    static String getGraphQLFooterWithVariable(VariableList rootElement) {
      return "\"," + rootElement.getJSONFromVariableOrDefaulNull() + "}";
    }

    private static String getVariableNameAsJSON(String variable) {
      return "  $" + variable.substring(0,1).toLowerCase() + variable.substring(1, variable.length()) + ": ";
    }
    
    private static String addAuthors() {
      return addObjectConstruct("authors", "count", addObjectConstruct("edges", addObjectConstruct("node", "id", "name")));
    }
    
    private static String addSubjects() {
      return addObjectConstruct("subjects", "count", addObjectConstruct("edges", addObjectConstruct("node", "id")));
    }
    
    private static String addTags() {
      return addObjectConstruct("tags", "count", addObjectConstruct("edges", addObjectConstruct("node", "id", "label")));
    }
    
    private static String addExecutiveProducers() {
      return addObjectConstruct("executiveProducers", "count", addObjectConstruct("edges", addObjectConstruct("node", "id", "name")));
    }
    
    private static String addCredits() {
      return addObjectConstruct("credits"
          , "count"
          ,addObjectConstruct("edges"
              , addObjectConstruct("node"
                  , "id"
                  , "name")));
    }

    private static String addCategorizations() {
      return addObjectConstruct("categorizations"
          , "count"
          , addObjectConstruct("edges"
              , addObjectConstruct("node"
                  , "id")));
    }

    private static String addGenres() {
      return addObjectConstruct("genres"
          , "count"
          , addObjectConstruct("edges"
              , addObjectConstruct("node"
                  , "id"
                  , "label")));
    }
    
    private static String addVideoFiles() {
      return addObjectConstruct("videoFiles(first: 50, orderBy: FILESIZE_DESC)"
          , "count"
          , addObjectConstruct("edges"
              , addObjectConstruct("node"
                  , "id"
                  , "publicLocation"
                  , addObjectConstruct("accessibleIn(first: 50)"
                      , "count"
                      , addObjectConstruct("edges"
                          , addObjectConstruct("node"
                              , "id"
                              , "baseIdPrefix")
                          ))
                 , addObjectConstruct("videoProfile"
                      , "id"
                      , "height"
                      , "width"
      ))));
    }
    
    private static String addCaptionFiles() {
      return addObjectConstruct("captionFiles(first: 50, orderBy: FILESIZE_DESC)"
          , "count"
          , addObjectConstruct("edges"
              , addObjectConstruct("node"
                  , "id"
                  , "publicLocation"
                  )));
    }
    
    private static String addOnItemInterface() {
      return addObjectConstruct("... on ItemInterface"
          , "availableUntil"
          , addObjectConstruct("itemOf"
              , "count"
              , addObjectConstruct("edges"
                  , addObjectConstruct("node"
                      , "id"
                      , "title"
                      , addBroadcastInterface()
                      ))));
    }
    
    private static String addOnProgrammeInterface() {
      return addObjectConstruct("... on ProgrammeInterface"
          , "episodeNumber"
          , addObjectConstruct("episodeOf"
              , "id"
              , "title"
              , "kicker"
              , "scheduleInfo"
              , "shortDescription"
              )
          , addBroadcastInterface());
    }

  private static String addBroadcastInterface() {
    return addObjectConstruct("broadcasts(first: 1, orderBy: START_ASC)"
        , addObjectConstruct("edges"
            , addObjectConstruct("node"
                , "__typename"
                , "start"
                , "id"
            )));
  }

  private static String addObjectConstruct(String title, String... subVariables) {
      
      StringBuilder sb = new StringBuilder();
      sb.append(" ");
      sb.append(title);
      sb.append(" { ");
      Arrays.asList(subVariables).forEach( (String name) -> sb.append(name + " "));  
      sb.append(" } ");
      
      return sb.toString();
      
      
    }
    
}


