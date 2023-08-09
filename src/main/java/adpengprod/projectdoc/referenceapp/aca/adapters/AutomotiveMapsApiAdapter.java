package adpengprod.projectdoc.referenceapp.aca.adapters;

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
    String accessToken = "ya29.c.b0Aaekm1I-ivo9-nkJslgCP_gYk_mPMPhoF9mDpXvTog1THyrKT8WSDtbtUn-zyrzIdKz-Ci4R0QN2xf00Ej80m-rmJTT_WbZAcEYAwp2dbr5MMMXUKRwPRXeMAoXp_v9U5sq74p-IrJ-ItIJ3DeYCQ62spVGgLFRQjJgnH8WsYs_kO4dSr10cLIzV55tHVMhmdtGfcQ7k0WaUpLUU30buZsMRkimHiXR-7n2JQDUd76QYw2ECisrDtUJt3hrGLsSPHrdjOKtrj2klKhfHuTALmwZjy7OrSr6JrRhty9vZO2Zg3vb88yOfWxw_dkvJcrdGhNl2V8qZRWPDrzPlkgQ43WoYEV6FLBzCVGlTA6Dn9Hi0DXOhu7QSL3YiiovV0EYJmGzYTP7vJqoA0gIwueF_lR0V9bFe4lFEvrZIb_34hIZFDxyj9Di9VpC1EwfZJVUaBW9rHcEFx08_heYMC0dSa68UzJsKUfn0et-Yzo84f_5XuLueud4OIzVn3twLhQwSa0ebrqBtDroRliYx1ipV44yMaRzM8eUIBvXhYfivJNg6386-jWdB_L2vlASgmk253PcfG589P3t_0m50hyRWYYv6iRIJ49rWwvQamjtiB1VM0nvBBpSQZ8br9RJO8xm89cs2eQibx4BZv-2SxbhZnB19WosVh-xvRrasxz5MykflQMqhhuk9V1rpOc6OgU7fka4Zs6lid6x8d9kRcz2iljwcUrmnx4WFzjxXwSYbgfQgRWcq93VUo8sX0zMBzyacWRY-_r2jdntImjYeUwQ14mwv92mfIz5jp66k8cytj5zrr9Wif5htwIZFwlivQ5km5p9j9cSrJ0uRkhiow7YQ_QBFbgQeoFB4X8-jhgYm1iX1pSue9cmiWzUpi2Vdo6b3y1X3l014Z7U16oFX7lqm1j1i_slfjgzSqjrt-QU8s9Oe2kXdbI3iM5pmeJqpmkXiupvu1UxJRRy-orbBMoumbdvo474_Qds-9uxvUkvu8nw9V99pUk5JwVe";
    Credentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null));

    //For Prod authorization: use application default credentials(ADC) with explicitly requested scope.
    //Credentials credentials = GoogleCredentials.getApplicationDefault().createScoped(ACA_SCOPE);

    return credentials;

  }

  public byte[] listTiles(double lowLat, double lowLang, double highLat, double highLong,
      Optional<String> dataLayer, Optional<Integer> pageSize, Optional<String> nextPageToken) throws Exception {
    String mapName = getLatestAvailableMap();
    // Request all tiles for a particular viewport and map version.
    // Tiles will be populated with data from the specified DataLayer.
    // Use a larger page size to page through results faster, but
    // be careful not to use a page size so large that your client OOMs.
    ListTilesRequest request = ListTilesRequest.newBuilder().setParent(mapName)
        .setPageSize(pageSize.isPresent()? pageSize.get() : LIST_TILES_DEFAULT_PAGE_SIZE)
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
      return TILE_NOT_PRESENT_MSG.getBytes();
    }
    //Tile tile = Tile.parseFrom(response.getTilesList().get(0).toByteArray());
    //System.out.println("Testtesttest: " + tile.getSegments(0).getAllowedDirectionsValue());
    return response.getTilesList().get(0).toByteArray();
  }

}
