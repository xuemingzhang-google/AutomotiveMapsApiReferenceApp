package cloudcode.helloworld.web;

import cloudcode.helloworld.adapters.AutomotiveMapsApiAdapter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ListMapsController {

  private AutomotiveMapsApiAdapter automotiveMapsApiAdapter;

  public ListMapsController(AutomotiveMapsApiAdapter automotiveMapsApiAdapter) throws Exception {
    this.automotiveMapsApiAdapter = automotiveMapsApiAdapter;
  }
  @GetMapping("/listMaps")
  public String listMaps() {
    return automotiveMapsApiAdapter.getLatestAvailableMap();
  }
}
