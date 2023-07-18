package adpengprod.projectdoc.referenceapp.aca.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Defines a controller to handle HTTP requests */
@Controller
public final class AutomotiveMapsApiProberController {
  private static final Logger logger = LoggerFactory.getLogger(AutomotiveMapsApiProberController.class);

  /**
   * Create an endpoint for the landing page
   *
   * @return the index view template
   */
  @GetMapping("/")
  public String showProberHomePage() throws Exception {
    return "index";
  }

}
