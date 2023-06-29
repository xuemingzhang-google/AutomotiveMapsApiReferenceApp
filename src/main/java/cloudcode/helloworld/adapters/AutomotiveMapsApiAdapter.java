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
 * Adapter class to call Google Automotive Cloud API's listMaps and listTiles APIs.
 * API reference: https://developers.google.com/maps/documentation/automotive/automotive-maps/reference/rpc
 */
public class AutomotiveMapsApiAdapter {
  private static final String ACA_SCOPE = "https://www.googleapis.com/auth/automotivemaps";

  private static final String MAP_NOT_PRESENT_MSG = "Most recent map version does not present";

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
    // String accessToken = "";
    // Credentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null));

    //For Prod authorization: use application default credentials(ADC) with explicitly requested scope.
    Credentials credentials = GoogleCredentials.getApplicationDefault().createScoped(ACA_SCOPE);

    return credentials;
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
