package adpengprod.projectdoc.referenceapp.aca.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.mockito.Mockito.when;

import adpengprod.projectdoc.referenceapp.aca.adapters.AutomotiveMapsApiAdapter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.boot.test.mock.mockito.MockBean;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ListMapsControllerTests {

  @Autowired private MockMvc mvc;

  @MockBean
  private AutomotiveMapsApiAdapter automotiveMapsApiAdapter;

  @Test
  public void getMaps() throws Exception {
    when(automotiveMapsApiAdapter.getLatestAvailableMap()).thenReturn("Hello, Mock!");

    String response = mvc.perform(MockMvcRequestBuilders.get("/listMaps"))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    // Then
    assertThat(response).isEqualTo("Hello, Mock!");
  }

}
