package adpengprod.projectdoc.referenceapp.aca.web;

import adpengprod.projectdoc.referenceapp.aca.adapters.AutomotiveMapsApiAdapter;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ListTilesController {
  private AutomotiveMapsApiAdapter automotiveMapsApiAdapter;

  private String results;

  public ListTilesController(AutomotiveMapsApiAdapter automotiveMapsApiAdapter) throws Exception {
    this.automotiveMapsApiAdapter = automotiveMapsApiAdapter;
    results = "";
  }

  @GetMapping("/listTiles")
  public String listTiles (
      @RequestParam(required = true) Double lowLat,
      @RequestParam(required = true) Double lowLng,
      @RequestParam(required = true) Double highLat,
      @RequestParam(required = true) Double highLng,
      @RequestParam(required = false) Optional<String> dataLayer,
      @RequestParam(required = false, defaultValue = "30") Optional<Integer> pageSize,
      @RequestParam(required = false) Optional<String> nextPageToken) throws Exception {
    results += automotiveMapsApiAdapter.listTiles(
            lowLat,
            lowLng,
            highLat,
            highLng,
            dataLayer,
            pageSize,
            nextPageToken);
    return results;
  }
}
