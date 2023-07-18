package adpengprod.projectdoc.referenceapp.aca.web;

import adpengprod.projectdoc.referenceapp.aca.adapters.AutomotiveMapsApiAdapter;
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
