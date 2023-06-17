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

/**
 * Fetches and processes tiles for the latest available map version.
 */
public class AutomotiveMapsApiAdapter {
  public static String fetchTiles() throws Exception {
    String accessToken = "ya29.c.b0Aaekm1J1ms-_C1dldWYjXZtov_pFq0O5MNy3hlWH9HztIjYxQt7pcwz1e98o6q8oCuqY9C0lbwgxLWcNNztbE3qoEm5cdUBF31-Ztp7KCIXsQMFB0f8yFVDGIcC8cF7Ydufh7NGUschYrNLMQ1IICFfbO5oQvKNg-baCy2i4wuwVufpv896jTYLUHejI073o1fhMH1vivXoj6idzIyoNMOP6FTzvUJs6CCvjBDctyaPNfCDNeofWoUPJKpwFvpbxRV2tdamXx3rqMltdAVxkRxwZ3kxK1E05LE_-QuN_YJeIVshZnB0zfOd6GjXw-gEI8m3qA3rqH3kl6SfKfHTVEYl-prwf-wRy6yPciJo4v5Bqgkhj7VWb32zzDHunZUrA_e5LfA0ulkkqTPvHn2HjixcabZ2rkOYit9NKYVEODr5XmvWrZGUdHmYxHPH1qegxyfTi9Rv5d7jzwsLOEW3CNL_pmBHvlKa5oSFQ6G_JEFMAais2NXUWd3uzFyeK4VbPxbvERF0qmu1pNrCaT-teaZBbdoR_K4RKF5CXp-QMUfzeJ703uv7RQqjM6j76xFMM7DxTWAT591DRiVcpRqeoJSz_Xlac6BiJ1Syh5exWsoxXrFlf1W9hw9yg3u4_2QVaQ-zBgvIfwoZIM0QewyjemVSvcvsra_8X67ypXIm6wiqf1okeM2V8ap-z48avufx3iIhMcXJg4sZRexZjM_6bbjjgebopOlZ33la9B-6Ba6FQ3y5ySqXMuwczvpn_uf6spUyqQayJaMbYxu61n6M_19gpxoRuOYSwrgM4mj_xdjrxVYJY6ciBxVuMs6wQcYvaJn2r5uM3QWJ4Szthy0_z7sf-8MQjR1k8OWIW0u-eauWZ4ymemoy-j0SMdO6fZcJUla9q1b73J59pppsqXf_oU-jqWgbFUeg12Qca4i4trfwU_UZ06bhyIByXQilvXcgF69M96I1yfXVJRyUWsQvIr3_sJ5pFym61lj6SjchQ95ozc8ema3gqjt4";
    // Credentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null));
    Credentials credentials = GoogleCredentials.getApplicationDefault();
    CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(credentials);
    AutomotiveMapsSettings automotiveMapsSettings =
        AutomotiveMapsSettings.newBuilder()
            .setCredentialsProvider(credentialsProvider)
            .build();
    try (AutomotiveMapsClient automotiveMapsClient = AutomotiveMapsClient.create(
        automotiveMapsSettings)) {
      Optional<Map> mostRecentMap = getLatestAvailableMap(automotiveMapsClient);

      if (mostRecentMap.isEmpty()) {
        return "Empty!!"; // There are no available map versions!
      }

      return mostRecentMap.get().getName();

      // processTiles(automotiveMapsClient, mostRecentMap.get().getName());
    }
  }

  public static Optional<Map> getLatestAvailableMap(AutomotiveMapsClient automotiveMapsClient) {
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
    return allAvailableMaps.stream()
        .max(Comparator.comparing(map -> map.getGenerationTime(), Timestamps.comparator()));
  }

  public static void processTiles(AutomotiveMapsClient automotiveMapsClient, String mapName) {
    // Request all tiles for a particular viewport and map version.
    // Tiles will be populated with data from the specified DataLayer.
    // Use a larger page size to page through results faster, but
    // be careful not to use a page size so large that your client OOMs.
    ListTilesRequest request = ListTilesRequest.newBuilder().setParent(mapName).setPageSize(30)
        .setGeoBounds(GeoBounds.newBuilder().setViewport(
            Viewport.newBuilder()
                .setLow(LatLng.newBuilder().setLatitude(57.716018).setLongitude(11.875966))
                .setHigh(LatLng.newBuilder().setLatitude(57.753558).setLongitude(11.978491)).build()

        ).build()).setDataLayer(DataLayer.ALL_LAYERS).build();

    // Page through the results.
    while (true) {
      // In this example, each response will contain 30 tiles.
      ListTilesResponse response = automotiveMapsClient.listTilesCallable().call(request);
      for (Tile element : response.getTilesList()) {
        // TODO: Process each tile.
      }
      String nextPageToken = response.getNextPageToken();
      if (!Strings.isNullOrEmpty(nextPageToken)) {
        request = request.toBuilder().setPageToken(nextPageToken).build();
      } else {
        break;
      }
    }
  }
}
