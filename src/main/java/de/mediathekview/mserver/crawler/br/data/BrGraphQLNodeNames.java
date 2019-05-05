/*
 * BrGraphQLNodeNames.java
 * 
 * Projekt    : MServer
 * erstellt am: 14.12.2017
 * Autor      : Sascha
 * 
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br.data;

public enum BrGraphQLNodeNames {

  RESULT_ERRORS_NODE("errors"),
  RESULT_ROOT_NODE("data"),
  RESULT_ROOT_BR_NODE("viewer"),
  RESUTL_CLIP_BROADCAST_ROOT("broadcasts"),
  RESULT_CLIP_CAPTION_FILES("captionFiles"),
  RESULT_CLIP_ID_ROOT("searchAllClips"),
  RESULT_CLIP_DETAILS_ROOT("clipDetails"),
  RESULT_CLIP_EPISONEOF("episodeOf"),
  RESULT_CLIP_ITEMOF("itemOf"),
  RESULT_CLIP_VIDEO_FILES("videoFiles"),
  RESULT_CLIP_VIDEO_PROFILE("videoProfile"),
  RESULT_ITEM_OF("itemOf"),
  RESULT_PAGE_INFO("pageInfo"),
  RESULT_NODE_EDGES("edges"),
  RESULT_NODE("node")
  ;
  
  private String nodeName;
  
  private BrGraphQLNodeNames(String nodeName) {
    this.nodeName = nodeName;
  }
  
  public String getName() {
    return this.nodeName;
  }
  
}
