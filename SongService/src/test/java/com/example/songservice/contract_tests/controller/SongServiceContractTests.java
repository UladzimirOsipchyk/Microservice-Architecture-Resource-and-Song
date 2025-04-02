package com.example.songservice.contract_tests.controller;


import com.example.songservice.controller.SongController;
import com.example.songservice.dto.SongRequestDTO;
import com.example.songservice.model.Song;
import com.example.songservice.service.SongService;
import com.example.songservice.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;


import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@ExtendWith(SpringExtension.class)
@WebMvcTest(SongController.class)
public class SongServiceContractTests {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private SongService songService;

  @BeforeEach
  void setup() throws Exception {
    Song song = TestUtils.prepareSongForTest();

    when(songService.getSongById(1L)).thenReturn(song);
    when(songService.createSong(any(SongRequestDTO.class))).thenReturn(song);
    when(songService.deleteSongs("1,2")).thenReturn(List.of(1L, 2L));
  }

  @Test
  public void shouldReturnSongById() throws Exception {

    mockMvc.perform(get("/songs/1")
        .accept(MediaType.APPLICATION_JSON))

        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))

        .andExpect(jsonPath("$.name").value("Bohemian Rhapsody"))
        .andExpect(jsonPath("$.artist").value("Queen"))
        .andExpect(jsonPath("$.album").value("A Night at the Opera"))
        .andExpect(jsonPath("$.year").value(1975));
  }

  @Test
  public void shouldCreateNewSong() throws Exception {
    String requestBody = """
                {
                    "name": "Bohemian Rhapsody",
                    "artist": "Queen",
                    "album": "A Night at the Opera",
                    "year": 1975
                }
                """;

    mockMvc.perform(post("/songs")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  public void shouldDeleteSongsByIds() throws Exception {
    mockMvc.perform(delete("/songs")
            .param("ids", "1,2")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.ids[0]").value(1))
        .andExpect(jsonPath("$.ids[1]").value(2));
  }
}
