package adpengprod.projectdoc.referenceapp.aca.adapters;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.base.Strings;
import com.google.geo.type.Viewport;
import com.google.maps.automotivemaps.v1.AutomotiveMapsClient;
import com.google.maps.automotivemaps.v1.AutomotiveMapsSettings;
import com.google.maps.automotivemaps.v1.DataLayer;
import com.google.maps.automotivemaps.v1.GeoBounds;
import com.google.maps.automotivemaps.v1.ListMapsRequest;
import com.google.maps.automotivemaps.v1.ListMapsResponse;
import com.google.maps.automotivemaps.v1.ListTilesRequest;
import com.google.maps.automotivemaps.v1.ListTilesResponse;
import com.google.maps.automotivemaps.v1.Map;
import com.google.maps.automotivemaps.v1.Tile;
import com.google.protobuf.util.Timestamps;
import com.google.type.LatLng;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import java.util.List;
import com.google.auth.oauth2.AccessToken;

/**
 * Adapter class to call Google Automotive Cloud API's listMaps and listTiles APIs.
 * API reference: https://developers.google.com/maps/documentation/automotive/automotive-maps/reference/rpc
 */
@Service
public class AutomotiveMapsApiAdapter {
  private static final String ACA_SCOPE = "https://www.googleapis.com/auth/automotivemaps";

  private static final String MAP_NOT_PRESENT_MSG = "Most recent map version does not present";

  private static final String TILE_NOT_PRESENT_MSG = "No tile available.";

  private static final int LIST_TILES_DEFAULT_PAGE_SIZE = 30;

  private AutomotiveMapsClient automotiveMapsClient;
  public AutomotiveMapsApiAdapter() throws Exception {
    setUpAutomotiveMapsClient();
  }

  public String getLatestAvailableMap() {
    // Request all available map versions.
    ListMapsRequest request = ListMapsRequest.newBuilder().setPageSize(10)
        .setFilter("state=AVAILABLE").build();
    ArrayList<Map> allAvailableMaps = new ArrayList<>();

    // Page through the results.
    while (true) {
      ListMapsResponse response = automotiveMapsClient.listMapsCallable().call(request);
      for (Map map : response.getMapsList()) {
        allAvailableMaps.add(map);
      }
      String nextPageToken = response.getNextPageToken();
      if (!Strings.isNullOrEmpty(nextPageToken)) {
        request = request.toBuilder().setPageToken(nextPageToken).build();
      } else {
        break;
      }
    }

    // Determine the most recent map version.
    Optional<Map> mostRecentMap = allAvailableMaps.stream()
        .max(Comparator.comparing(map -> map.getGenerationTime(), Timestamps.comparator()));

    return mostRecentMap.isPresent()? mostRecentMap.get().getName() : MAP_NOT_PRESENT_MSG;
  }

  private void setUpAutomotiveMapsClient() throws Exception{
    Credentials credentials = getCredentials();
    CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(credentials);
    AutomotiveMapsSettings automotiveMapsSettings =
        AutomotiveMapsSettings.newBuilder()
            .setCredentialsProvider(credentialsProvider)
            .setEndpoint("preprod-automotivemaps.sandbox.googleapis.com:443")
            .build();

    automotiveMapsClient = AutomotiveMapsClient.create(
        automotiveMapsSettings);
  }
  private Credentials getCredentials() throws Exception{
    // For local development: follow https://developers.google.com/maps/documentation/automotive/automotive-maps/oauth-token
    // to generate a short-lived auth token, uncomment the below two lines and replace "" with the token generated.
    // Comment out the credentials created for Prod authorization.
    // DO NOT include the token in public Git repo.
    String accessToken = "ya29.c.b0Aaekm1L5tI53Wr1JPLWF0Rg42eH-CSG1LsqGWIhYM0lQDiT3Mpj_oVJa1BUOZtAFW30mBDBNta6ZAaANNCPC02072hTMyUHjFBcX9LpG1u5caAmCtKoWYExaiYogGOIIwfXARhLBONtKuWoaInHEGC1jsN-GDjBnM498T8gh6_K19Jk6GIACAXUAnVUWzU44pQSgQqzR0qVxCGK72BxF7ZmVsFRpkZCKn8hvy3g0LpqXyMFUCxuAY7E8mN1HhT4x-xjKXOkDAkwfARAp9l7Ezl36O1LoAZv3AUPA6OQSgrGRihwsoEYPp2FhwHtlm4w7e3duNm_wcj8hhCOOiSVIOWgu8-vOzd7pSfuzXjHW9X6JmjrZEHNwLKE1V7H_uyQh4su0YcGg2STA8v-uxo7jpEQo5vIg7o5e2_fAzYOnNdPVP6uFlPoxAy3b4ZSJuFdYvHOwkLHWjJ_n6NNTwmq7eKnMg0ftVP58UrNo7rf_ZCmxxwU8oD8ZQyDbqgZp3NCXgnmKmTn8bWakiUJkDCOgtk2FJTbejN0Jwg4KrfH-cjRbH25_x53zOxwZQipRPotBYUoN588CS9BcbR9m3U0dY6xpxyJImre16Mp05O-BikYoWWyFvJzh5MBze4Ba-SW6Mt7ayedb9ap9yb875Vn1-83dyykIOq-vqk2JZfse-a6RsXa3l7jxa0w6yZ-hzXwVMhi7F3ro35j6qww5ovIrgVSnzcrcYBlfjQ4gs0_y4Z4sh0YmOssWqb1SIY-6Xy7oleglyy00U5v1aQzq7btjMbpVkojeXZJ4tZs29z-wuJsUb_5qQr2_bx3kwS2aQnz_1qum7snW3yz3qtv6iywBQSk5fhqUROguXJdYt-z9tiy--FF2FrdcQYYWe0i0p3F4IX4qgaeO8e5WuIFkuzb14W_58R_UM-5kZxzlBrXcQiSR3xe9W1RY4w9jBl67t99IwtJ-4dugV66uobezaod1U3FaUxQwMf0_rVOgZqYrpSyzBX2wY3xYiXJ";
    Credentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null));

    //For Prod authorization: use application default credentials(ADC) with explicitly requested scope.
    //Credentials credentials = GoogleCredentials.getApplicationDefault().createScoped(ACA_SCOPE);

    return credentials;

  }

  public String listTiles(double lowLat, double lowLang, double highLat, double highLong,
      Optional<String> dataLayer, Optional<Integer> pageSize, Optional<String> nextPageToken) {
    String mapName = getLatestAvailableMap();
    // Request all tiles for a particular viewport and map version.
    // Tiles will be populated with data from the specified DataLayer.
    // Use a larger page size to page through results faster, but
    // be careful not to use a page size so large that your client OOMs.
    ListTilesRequest request = ListTilesRequest.newBuilder().setParent(mapName)
        .setPageSize(LIST_TILES_DEFAULT_PAGE_SIZE)
        .setGeoBounds(GeoBounds.newBuilder().setViewport(
            Viewport.newBuilder()
                .setLow(LatLng.newBuilder().setLatitude(lowLat).setLongitude(lowLang))
                .setHigh(LatLng.newBuilder().setLatitude(highLat).setLongitude(highLong)).build()
        ).build())
        .setDataLayer((dataLayer.isPresent() && !dataLayer.get().isEmpty())?
            DataLayer.valueOf(dataLayer.get())
            : DataLayer.HW_LIMITED_USE)
        .build();

    if (nextPageToken.isPresent() && !nextPageToken.get().isEmpty()) {
      request = request.toBuilder().setPageToken(nextPageToken.get()).build();
    }

    ListTilesResponse response = automotiveMapsClient.listTilesCallable().call(request);
    if (response == null || response.getTilesList() == null || response.getTilesList().isEmpty()) {
      return TILE_NOT_PRESENT_MSG;
    }

    Tile tile = response.getTilesList().get(0);
    List<String> fildNames = tile.getAllFields().keySet()
        .stream()
        .map(fieldDescriptor -> fieldDescriptor.getName())
        .collect(Collectors.toList());

    return String.join(",", fildNames);
  }

}
