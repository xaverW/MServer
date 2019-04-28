package de.mediathekview.mserver.crawler.br.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.br.BrCrawler;
import de.mediathekview.mserver.crawler.br.data.BrClipType;
import de.mediathekview.mserver.crawler.br.data.BrID;
import de.mediathekview.mserver.testhelper.AssertFilm;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

public class BrGetClipDetailsTaskTestError extends BrTaskTestBase {

  @Test
  public void testNoVideoFiles() {

    final String requestUrl = "/myBrRequets";
    setupSuccessfulJsonPostResponse(requestUrl, "/br/br_film_no_videos.json");

    BrGetClipDetailsTask clipDetails = new BrGetClipDetailsTask(createCrawler(), createClipQueue("av:5a2fb98b69acd400179e99ec"));

    Set<Film> resultSet = clipDetails.compute();

    assertThat(resultSet.size(), equalTo(0));
  }
}
