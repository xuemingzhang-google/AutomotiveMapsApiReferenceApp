package cloudcode.helloworld.adapters;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.AccessToken;
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
import org.springframework.stereotype.Service;

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
            .build();

    automotiveMapsClient = AutomotiveMapsClient.create(
        automotiveMapsSettings);
  }
  private Credentials getCredentials() throws Exception{
    // For local development: follow https://developers.google.com/maps/documentation/automotive/automotive-maps/oauth-token
    // to generate a short-lived auth token, uncomment the below two lines and replace "" with the token generated.
    // Comment out the credentials created for Prod authorization.
    // DO NOT include the token in public Git repo.
    String accessToken = "ya29.c.b0Aaekm1I_q5tG8Ov_YoOGcnJyMEw83VUpAHvM4vpDw1STZ5SjRKF4-ShjTCJA5aytS5zN-1TPaWf1LejKmh8ReOzYRsOph87zhnc9X5NF960FadvQHKkPQcnMah-1MEoqiHaZQrXawYOLwRPjHmm1o1mvxWJKUCnVK_q_rFRH9nXB-UD-bEiIhGb4oKjUpmBEzajRcx5b1mq2pf0IRPCgeK9Lw1q_8M-icDuu_RjjPkjqqNlH9wbRCR_Wwh1EkxygmfZNv9zrbv0Kd97yT2KONEfzDAUk7fNtiEG2KVqoQ5uEhvIARGvbfzn-e4sp7oI7fPh2Zjn2ZcpsTx97hGB_x4LGtz46vG5VIQkPJs1frSm4scdcpElvlCCqHooc6Q52TEYMsJkCO3_FEpn8lE7f_NEkb_BEjG8O4vz50vJkxvcRMOYaI9NMTb-KN3-9EDOBqGIOplME5yEe5fC5fqJTTxEIfz0uhfGWLA_i1UYXHJkdObz9_eQXzDxM8TgL2C97sck6lOU_Krk2gN2wRIB1lBo53CW74G7Ku0SjsNTB3DkpY1RlJXlQQuMVoZuCyehQqzsaQwL591AuSxIiIO9pmp324xJFX-e2XSYpyzsk6wOkf1XRRF3qwZoRVRcq_owwS423r7Rvgrss8a38lrhXOVwhhVr8i-O-tOge8sBgbz999m3XuJ_neeXJb5Jgc-Bp3FJ18sUJrabce-O2OJ6u5Xkr9OkoJBzI8wq11Zs_jWt1lIscw-16Ig8Ig6WlqqWB37iyU81iay_in2B4S0kjh_RrQ9gI2Qhwr-xYR69RoOXy_5BlS1uj67wW7z8at3kVQ7tvxdQ4muX3QYjejmrOt3lBQW2xOwzsXMwWZb76-hjQBF1V3aut4rIQsczcRXmj0ki1opg5cgj-40g6ouiX9j7FWf_290oR8v00V0VBMRJQxxi-OaVuq6BrMpr7gFenVFue4hpbWgn7jRXkfrw5aMBXivnwqjdoM9XpxrV0QRaiM1sZuU93pY4";
    Credentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null));

    //For Prod authorization: use application default credentials(ADC) with explicitly requested scope.
    //Credentials credentials = GoogleCredentials.getApplicationDefault().createScoped(ACA_SCOPE);

    return credentials;

  }

  public String listTiles(double lowLat, double lowLang, double highLat, double highLong,
      Optional<Integer> pageSize, Optional<String> nextPageToken) {
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
        ).build()).setDataLayer(DataLayer.HW_LIMITED_USE).build();

    if (nextPageToken.isPresent() && !nextPageToken.get().isEmpty()) {
      request = request.toBuilder().setPageToken(nextPageToken.get()).build();
    }

    ListTilesResponse response = automotiveMapsClient.listTilesCallable().call(request);
    if (response == null || response.getTilesList() == null || response.getTilesList().isEmpty()) {
      return TILE_NOT_PRESENT_MSG;
    }

    return response.getTilesList().get(0).getName() + response.getNextPageToken();
  }

}
