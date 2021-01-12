/*
 * WebTargetDS.java
 * 
 * Projekt    : MServer
 * erstellt am: 05.10.2017
 * Autor      : Sascha
 * 
 * (c) 2017 by Sascha Wiegandt
 */
// Ist eigentlich aus MLib in der neuen Architektur, aber für den neuen BR-Crawler erstmal hierher kopiert
package mServer.crawler.sender.br;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;

public class WebTargetDS {
    
    private static final Map<String, WebTarget> connectionPool = new ConcurrentHashMap<>();
    
    private WebTargetDS() {
        
    }
    
    public static WebTarget getInstance(String url) {
        if (connectionPool.containsKey(url)) {
            return connectionPool.get(url);
        } else {
            Client client = ClientBuilder.newClient();
            connectionPool.put(url, client.target(url));

            return connectionPool.get(url);
        }
    }

}
