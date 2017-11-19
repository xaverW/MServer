package de.mediathekview.mserver.crawler.srf.parser;

import com.google.gson.JsonArray;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.hamcrest.Matchers;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class SrfSendungenOverviewJsonDeserializerTest {
  @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { 
          { "/srf/srf_overview_page_data_section.json", 
            new CrawlerUrlDTO[] { 
              new CrawlerUrlDTO("https://www.srf.ch/play/tv/sendung/1-gegen-100?id=6fd27ab0-d10f-450f-aaa9-836f1cac97bd"), 
              new CrawlerUrlDTO("https://www.srf.ch/play/tv/sendung/10vor10?id=c38cc259-b5cd-4ac1-b901-e3fddd901a3d"),
              new CrawlerUrlDTO("https://www.srf.ch/play/tv/sendung/26-minutes?id=420426e5-4e4d-4ba7-ab44-09a4e17c13bf"),
              new CrawlerUrlDTO("https://www.srf.ch/play/tv/sendung/8x15-?id=c5a89422-4580-0001-4f24-1889dc30d730"),
              new CrawlerUrlDTO("https://www.srf.ch/play/tv/sendung/abstimmungen-teilw--in-gebaerdensprache?id=c5e431c3-ab90-0001-3228-16001350159c"),
              new CrawlerUrlDTO("https://www.srf.ch/play/tv/sendung/aeschbacher?id=0a7932df-dea7-4d8a-bd35-bba2fe2798b5")
            } 
          }
        });
    }
    
    private final String jsonFile;
    private final CrawlerUrlDTO[] expectedUrls;
    private final SrfSendungenOverviewJsonDeserializer target;
    
    public SrfSendungenOverviewJsonDeserializerTest(String aJsonFile, CrawlerUrlDTO[] aExpectedUrls) {
      jsonFile = aJsonFile;
      expectedUrls = aExpectedUrls;
      
      target = new SrfSendungenOverviewJsonDeserializer();
    }
    
    @Test
    public void test() {
      JsonArray json = JsonFileReader.readJsonArray(jsonFile);
      
      Set<CrawlerUrlDTO> actual = target.deserialize(json, Set.class, null);
      
      assertThat(actual, notNullValue());
      assertThat(actual.size(), equalTo(expectedUrls.length));
      assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
    }
}