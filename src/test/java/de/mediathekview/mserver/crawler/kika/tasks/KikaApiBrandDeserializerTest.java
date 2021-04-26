package de.mediathekview.mserver.crawler.kika.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import de.mediathekview.mserver.crawler.kika.json.KikaApiBrandsDto;
import de.mediathekview.mserver.crawler.kika.json.KikaApiOverviewPageDeserializer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;


@RunWith(Parameterized.class)
public class KikaApiBrandDeserializerTest {

  private String jsonFile = "KikaApiBrands1.json";
  private int numberOfExpectedRecords = 0;
  private boolean hasSubpage = false;
  private boolean hasError = false;
  

  public KikaApiBrandDeserializerTest(String jsonFile, int numberOfExpectedRecords, boolean subpage, boolean error) {
    this.jsonFile = jsonFile;
    this.numberOfExpectedRecords = numberOfExpectedRecords;
    this.hasSubpage = subpage;
    this.hasError = error;
  }

  @Parameterized.Parameters
  public static Object[][] data() {
    return new Object[][] {
          {
            "/kika/KikaApiBrands1.json",
            40,
            true,
            false
          },
          {
            "/kika/KikaApiBrands2.json",
            21,
            false,
            false
          },
          {
            "/kika/KikaApiError.json",
            0,
            false,
            true
          }
        };

  }

  @Test
  public void testDeserializeBrand() throws URISyntaxException, IOException {
    final Type KikaApiBrandsDtoType = new TypeToken<Set<KikaApiBrandsDto>>() {}.getType();
    //
    final Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(KikaApiBrandsDtoType, new KikaApiOverviewPageDeserializer())
            .create();
    //
    final KikaApiBrandsDto aKikaApiBrandsDto =
        gson.fromJson(
            Files.newBufferedReader(Paths.get(getClass().getResource(jsonFile).toURI())),
            KikaApiBrandsDtoType);
    // number of elements
    assertEquals(numberOfExpectedRecords, aKikaApiBrandsDto.getElements().size());
    // has subpages
    assertEquals(hasSubpage, aKikaApiBrandsDto.getNextPage().isPresent());
    // error page
    assertEquals(hasError, aKikaApiBrandsDto.getErrorCode().isPresent());
    //
  }
  
}
