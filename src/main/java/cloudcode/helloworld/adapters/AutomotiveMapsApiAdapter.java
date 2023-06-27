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
    // String accessToken = "ya29.c.b0Aaekm1JcNA04A2PenVv_ko9ZfnfIOY-sNcQdbuuzOXx7ciT2qp0nvygmwCzEIZii9pt62AJ4ZHMVV6Nd4d5Lu0qabCiGCOovjXX5ZAKYeE5Y_tm0LAsNgsfqRLjeUDQDT23imX4944WsLt5yhO8MOdZ7BIac30s1LEfYBXorWSkN56kp4vF7quJBwbAhl1RPy1NT-2p23G56UA-halncvyEmejL10Q0y-ipeRtW8zaN9oFC6E4HJ6I9qfyBjkIwndkNYNv9wDvn7X_ttxlk0FWkrwluSETMkpAJtNHp896Bivp6_Jz-qDrIFcbxHXS9VilXrEre01h3Nmb2Qv0W3d0uYjy24sk8Y5ewULnWSWUYcxfD2qiXB4VFdtk31buiXwdhBXan0txtKNHdYbP65pN7n6X8WvLoqklOVNpzy0_oDAIs2zVftVddo17QZXnS6UkMulSWUBSm13Tf_Z4sHdJxvkdWhNO9GCGFBu97gFBTFj1-xuYyCEmrV2N0dJm_HhaSUGrRfUQujvY5dhHLeNp3TslblebrUSwe_nqiMSQ2GB9T1TKXgYQbIxJfS4iqevlzo1gL591PwUj2yok3h2vdsj7vSVXQnop-w--cx8szvp2kJ11F7JnUI73sUfMp0fo2x_wrd048UihIqZ9MUovMkBvniR9om92cJejVhUyo9c7aWRBeWYqUup-X54pBJQminWodkyjjer8gtSq2j-aUpn5_lqr4Y6Wd9Rs4amgix6leuUymJfhxF-OJBY6ehQ3ci91ouoqkU_z6ZFpgInX7-lJe4_-9Jzc7-8jo8I4af3wg6Y4IjQheeaIumqvam9vgd5_xvaOWF2p9dZoIMoa2eba07du2VXFJS6i4-V1OiMn-p7-YOSJqOZfY5JRyxyU-ubxvdc22aFuV914vRURk_J4Jfewxmju0ky6nvtBpotlO2ffJhfeu0m7Feyto8niI6BF6WMzyzlVS2ob_j0BuwnIdpa3py0bIY3zj4i_ikln1SVU-JW0s";
    // Credentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null));
    Credentials credentials = GoogleCredentials.getApplicationDefault().createScoped("https://www.googleapis.com/auth/automotivemaps");
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

      //return processTilesStub(automotiveMapsClient, mostRecentMap.get().getName());
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

  public static String processTilesStub(AutomotiveMapsClient automotiveMapsClient, String mapName) {
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

    ListTilesResponse response = automotiveMapsClient.listTilesCallable().call(request);
    if (response == null || response.getTilesList() == null || response.getTilesList().isEmpty()) {
      return "Tile list does not exist";
    }
    return response.getTilesList().size() + response.getTilesList().get(0).toString();

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
