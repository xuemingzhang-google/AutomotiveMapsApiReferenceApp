package cloudcode.helloworld.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cloudcode.helloworld.adapters.AutomotiveMapsApiAdapter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ListTilesControllerTests {

  @Autowired private MockMvc mvc;

  @MockBean
  private AutomotiveMapsApiAdapter automotiveMapsApiAdapter;

  @Test
  public void getTiles() throws Exception {
    when(automotiveMapsApiAdapter.listTiles(anyDouble(),anyDouble(),anyDouble(),anyDouble(),any(),any()))
        .thenReturn("Hello, Mock!");

    String response = mvc.perform(
        MockMvcRequestBuilders
            .get("/listTiles")
            .param("lowLat", "1.0")

            .param("lowLang", "1.0")
            .param("highLat", "1.0")
            .param("highLong", "1.0"))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    // Then
    assertThat(response).isEqualTo("Hello, Mock!");
  }

}
