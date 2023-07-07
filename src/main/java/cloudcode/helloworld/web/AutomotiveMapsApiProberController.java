package cloudcode.helloworld.web;

import cloudcode.helloworld.adapters.AutomotiveMapsApiAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Defines a controller to handle HTTP requests */
@Controller
public final class AutomotiveMapsApiProberController {

  private static String project;
  private static final Logger logger = LoggerFactory.getLogger(AutomotiveMapsApiProberController.class);

  private AutomotiveMapsApiAdapter automotiveMapsApiAdapter;

  public AutomotiveMapsApiProberController() throws Exception {
    automotiveMapsApiAdapter = new AutomotiveMapsApiAdapter();
  }

  /**
   * Create an endpoint for the landing page
   *
   * @return the index view template
   */
  @GetMapping("/")
  public String showProberHomePage(Model model) throws Exception {

    // Get Cloud Run environment variables.
    String revision = System.getenv("K_REVISION") == null ? "???" : System.getenv("K_REVISION");
    String service = System.getenv("K_SERVICE") == null ? "???" : System.getenv("K_SERVICE");

    String latestMapVersion = automotiveMapsApiAdapter.getLatestAvailableMap();

    // Set variables in html template.
    // model.addAttribute("revision", revision);
    model.addAttribute("revision", latestMapVersion);
    model.addAttribute("service", service);
    return "index";
  }

}
