/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mServer.crawler;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.tool.Hash;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MVHttpClient;
import mServer.tool.MserverDaten;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AddToFilmlist {

  /**
   * Minimum size of films in MiB to be included in new list.
   */
  private static final int MIN_SIZE_ADD_OLD = 5;
  private final static int NUMBER_OF_THREADS = 32;//(Runtime.getRuntime().availableProcessors() * Runtime.getRuntime().availableProcessors()) / 2;
  private final ListeFilme vonListe;
  private final ListeFilme listeEinsortieren;
  /**
   * List of all locally started import threads.
   */
  private final ArrayList<ImportOldFilmlistThread> threadList = new ArrayList<>();
  private AtomicInteger threadCounter = new AtomicInteger(0);

  public AddToFilmlist(ListeFilme vonListe, ListeFilme listeEinsortieren) {
    this.vonListe = vonListe;
    this.listeEinsortieren = listeEinsortieren;
  }

  public synchronized void addLiveStream() {
    if (listeEinsortieren.size() <= 0) {
      return;
    }

    vonListe.removeIf(f -> f.arr[DatenFilm.FILM_THEMA].equals(ListeFilme.THEMA_LIVE));
    listeEinsortieren.forEach(vonListe::add);
  }

  private void performTitleSearch(HashSet<Hash> hash, final int size) {
    vonListe.parallelStream().forEach(f -> {
      synchronized (hash) {
        hash.add(f.getHashValueIndexAddOld());
      }
    });

    listeEinsortieren.removeIf((f) -> hash.contains(f.getHashValueIndexAddOld()));
    hash.clear();

    Log.sysLog("===== Liste einsortieren Title =====");
    Log.sysLog("Liste einsortieren, Anzahl: " + size);
    Log.sysLog("Liste einsortieren, entfernt: " + (size - listeEinsortieren.size()));
    Log.sysLog("Liste einsortieren, noch einsortieren: " + listeEinsortieren.size());
    Log.sysLog("");
  }

  private void performUrlSearch(HashSet<Hash> hash, final int size) {
    vonListe.parallelStream().forEach(f -> {
      synchronized (hash) {
        hash.add(f.getHashValueUrl());
      }
    });

    listeEinsortieren.removeIf((f) -> hash.contains(f.getHashValueUrl()));
    hash.clear();

    Log.sysLog("===== Liste einsortieren URL =====");
    Log.sysLog("Liste einsortieren, Anzahl: " + size);
    Log.sysLog("Liste einsortieren, entfernt: " + (size - listeEinsortieren.size()));
    Log.sysLog("Liste einsortieren, noch einsortieren: " + listeEinsortieren.size());
    Log.sysLog("");
  }

  /**
   * Remove links which don´t start with http. - * Remove old film entries which
   * are smaller than MIN_SIZE_ADD_OLD.
   */
  private void performInitialCleanup() {
    listeEinsortieren.removeIf(f -> !f.arr[DatenFilm.FILM_URL].toLowerCase().startsWith("http"));
    listeEinsortieren.removeIf(f -> {
      String groesse = f.arr[DatenFilm.FILM_GROESSE];
      if (groesse.isEmpty()) {
        return false;
      } else {
        return Long.parseLong(groesse) < MIN_SIZE_ADD_OLD;
      }

    });
  }

  private void startThreads() {
    final OkHttpClient client = MVHttpClient.getInstance().getReducedTimeOutClient();

    List syncList = Collections.synchronizedList(listeEinsortieren);
    for (int i = 0; i < NUMBER_OF_THREADS; ++i) {
      ImportOldFilmlistThread t = new ImportOldFilmlistThread(syncList, client);
      t.setName("ImportOldFilmlistThread Thread-" + i);
      threadList.add(t);
      t.start();
    }
  }

  private void stopThreads() {
    if (Config.getStop()) {
      for (ImportOldFilmlistThread t : threadList) {
        t.interrupt();
      }
      for (ImportOldFilmlistThread t : threadList) {
        try {
          t.join();
        } catch (InterruptedException ignored) {
        }
      }
    }
  }

  /*
     * Diese Methode sortiert eine vorhandene Liste in eine andere Filmliste ein,
     * dabei werden nur nicht vorhandene Filme einsortiert.
   */
  public int addOldList() {
    threadCounter = new AtomicInteger(0);
    final HashSet<Hash> hash = new HashSet<>(vonListe.size() + 1);

    performInitialCleanup();

    int size = listeEinsortieren.size();

    performTitleSearch(hash, size);

    size = listeEinsortieren.size();
    performUrlSearch(hash, size);

    size = listeEinsortieren.size();
    long oldSize = size;

    startThreads();

    int count = 0;
    while (!Config.getStop() && threadCounter.get() > 0) {
      try {
        count++;
        if (count % 5 == 0) {
          long curSize = listeEinsortieren.size();
          System.out.println("Liste: " + curSize);
          System.out.println("Entfernte Einträge: " + ((oldSize - curSize)));
          oldSize = curSize;
        }
        TimeUnit.SECONDS.sleep(2);
      } catch (Exception ex) {
        Log.errorLog(978451205, ex, "Fehler beim Import Old");
      }
    }

    stopThreads();

    final int treffer = retrieveThreadResults();

    Log.sysLog("===== Liste einsortieren: Noch online =====");
    Log.sysLog("Liste einsortieren, Anzahl: " + size);
    Log.sysLog("Liste einsortieren, entfernt: " + (size - treffer));
    Log.sysLog("");
    Log.sysLog("In Liste einsortiert: " + treffer);
    Log.sysLog("");
    return treffer;
  }

  /**
   *
   * Add all local thread results to the filmlist.
   *
   * @return the total number of entries found.
   */
  private int retrieveThreadResults() {
    int treffer = 0;
    for (ImportOldFilmlistThread t : threadList) {
      final ArrayList<DatenFilm> localList = t.getLocalAddList();
      if (MserverDaten.debug) {
        Log.sysLog("Thread " + t.getName() + " list size: " + localList.size());
      }
      vonListe.addAll(localList);
      localList.clear();
      treffer += t.getTreffer();
    }
    return treffer;
  }

  private class ImportOldFilmlistThread extends Thread {

    private final List<DatenFilm> listeOld;
    private final ArrayList<DatenFilm> localAddList = new ArrayList<>((vonListe.size() / NUMBER_OF_THREADS) + 500);
    private int treffer = 0;
    private OkHttpClient client = null;

    public ImportOldFilmlistThread(List<DatenFilm> listeOld, OkHttpClient client) {
      this.listeOld = listeOld;
      threadCounter.incrementAndGet();
      this.client = client;
    }

    public int getTreffer() {
      return treffer;
    }

    public ArrayList<DatenFilm> getLocalAddList() {
      return localAddList;
    }

    private void addOld(DatenFilm film) {
      if (BannedFilmFilter.isBanned(film)) {
        Log.sysLog("Blacklist Treffer im import Old (" + film.arr[DatenFilm.FILM_TITEL] + ")");
        return;
      }

      treffer++;
      film.init();

      localAddList.add(film);
    }

    private synchronized DatenFilm popOld(List<DatenFilm> listeOld) {
      if (!listeOld.isEmpty()) {
        return listeOld.remove(0);
      } else {
        return null;
      }
    }

    @Override
    public void run() {

      DatenFilm film;
      while (!isInterrupted() && (film = popOld(listeOld)) != null) {
        final String url = film.arr[DatenFilm.FILM_URL];
        if (film.arr[DatenFilm.FILM_GROESSE].isEmpty()) {
          Request request = new Request.Builder().url(url).head().build();
          try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
              ResponseBody responseBody = response.body();
              long respLength = responseBody == null ? -1 : responseBody.contentLength();
              if (respLength < 1_000_000) {
                respLength = -1;
              } else if (respLength > 1_000_000) {
                respLength /= 1_000_000;
              }

              if (respLength > MIN_SIZE_ADD_OLD) {
                addOld(film);
              }
            }
          } catch (SocketTimeoutException ignored) {
          } catch (IOException ex) {
            ex.printStackTrace();
          }
        } else {
          if (Long.parseLong(film.arr[DatenFilm.FILM_GROESSE]) > MIN_SIZE_ADD_OLD) {
            Request request = new Request.Builder().url(url).head().build();
            try (Response response = client.newCall(request).execute()) {
              if (response.isSuccessful()) {
                addOld(film);
              }
            } catch (SocketTimeoutException ignored) {
            } catch (IOException ex) {
              ex.printStackTrace();
            }
          }
        }
      }
      threadCounter.decrementAndGet();
    }
  }
}
