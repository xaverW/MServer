/*
 * WebAccessHelper.java
 * 
 * Projekt    : MLib
 * erstellt am: 04.10.2017
 * Autor      : Sascha
 * 
 */
// Ist eigentlich aus MLib in der neuen Architektur, aber für den neuen BR-Crawler erstmal hierher kopiert
package mServer.crawler.sender.br;

import de.mediathekview.mlib.Const;
import java.net.URL;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import mServer.crawler.FilmeSuchen;
import mServer.crawler.RunSender;

public class WebAccessHelper {
  
    private WebAccessHelper() {}

    public static String getJsonResultFromGetAccess(URL serverUrl) {
        if(null == serverUrl)
            throw new IllegalArgumentException("Es wurde keine gültige ServerURL angegeben");

        WebTarget target = WebTargetDS.getInstance(serverUrl.toString());
        Response response = target.request(MediaType.APPLICATION_JSON_TYPE).get();
        String result = response.readEntity(String.class);

        FilmeSuchen.listeSenderLaufen.inc(Const.BR, RunSender.Count.ANZAHL);

        return result;
    }

    public static String getJsonResultFromPostAccess(URL serverUrl, String request) {
        if(null == serverUrl)
            throw new IllegalArgumentException("Es wurde keine gültige ServerURL angegeben");
        
        WebTarget target = WebTargetDS.getInstance(serverUrl.toString());

        Response response = target.request(MediaType.APPLICATION_JSON_TYPE)
          .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
        String result = response.readEntity(String.class);

        FilmeSuchen.listeSenderLaufen.inc(Const.BR, RunSender.Count.ANZAHL);
        
        return result;
    }

}
