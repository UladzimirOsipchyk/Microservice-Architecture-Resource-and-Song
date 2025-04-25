package com.example.songservice.contract.cdc;


import com.example.songservice.SongServiceApplication;
import com.example.songservice.controller.SongController;
import com.example.songservice.dto.CreatedSongDTO;
import com.example.songservice.dto.DeletedSongsDTO;
import com.example.songservice.dto.SongDTO;
import com.example.songservice.dto.SongRequestDTO;
import com.example.songservice.model.Song;
import com.example.songservice.utils.TestUtils;


import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;

@SpringBootTest(classes = SongServiceApplication.class)
public abstract class CdcBaseClass {
  private static final Song SONG = TestUtils.prepareSongForTest();

  @BeforeEach
  public void setup() {
    SongController controller = new SongController() {
      @Override
      public ResponseEntity<SongDTO> getSongById(Long id) {
        return ResponseEntity.ok(new SongDTO(
            SONG.getName(),
            SONG.getArtist(),
            SONG.getAlbum(),
            SONG.getLength(),
            SONG.getYear()
        ));
      }

      @Override
      public ResponseEntity<CreatedSongDTO> createSong(SongRequestDTO songRequestDTO) {
        return new ResponseEntity<>(new CreatedSongDTO(SONG.getId()), HttpStatus.OK);
      }

      @Override
      public ResponseEntity<DeletedSongsDTO> deleteResource(@RequestParam String ids) {
        List<Long> deletedIds = Arrays.asList(1L, 2L, 3L);
        return new ResponseEntity<>(new DeletedSongsDTO(deletedIds), HttpStatus.OK);
      }
    };

    RestAssuredMockMvc.standaloneSetup(controller);
  }

}
