package cloudcode.helloworld.web;

import cloudcode.helloworld.adapters.AutomotiveMapsApiAdapter;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ListTilesController {
  private AutomotiveMapsApiAdapter automotiveMapsApiAdapter;

  public ListTilesController() throws Exception {
    automotiveMapsApiAdapter = new AutomotiveMapsApiAdapter();
  }
  @GetMapping("/listTiles")
  public String listTiles(@RequestParam(required = true) Double lowLat,
      @RequestParam(required = true) Double lowLang,
      @RequestParam(required = true) Double highLat,
      @RequestParam(required = true) Double highLong,
      @RequestParam(required = false, defaultValue = "30") Optional<Integer> pageSize,
      @RequestParam(required = false) Optional<String> nextPageToken) {
    return automotiveMapsApiAdapter.listTiles(
        lowLat,
        lowLang,
        highLat,
        highLong,
        pageSize,
        nextPageToken);
  }
}
