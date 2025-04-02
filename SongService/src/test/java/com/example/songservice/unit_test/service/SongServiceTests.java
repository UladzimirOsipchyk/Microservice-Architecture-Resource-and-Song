package com.example.songservice.unit_test.service;

import com.example.songservice.dto.SongRequestDTO;
import com.example.songservice.exception.exceptions.SongNotFoundException;
import com.example.songservice.model.Song;
import com.example.songservice.repository.SongRepository;
import com.example.songservice.service.SongService;
import com.example.songservice.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.example.songservice.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SongServiceTests {
  @Mock
  private SongRepository songRepository = mock(SongRepository.class);

  @InjectMocks
  private SongService songService = new SongService();

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGetSongByIdSuccess() {
    Song song = prepareSongForTest();

    when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));

    Song result = songService.getSongById(1L);


    assertNotNull(result);
    assertEquals(SONG_NAME, result.getName());
    assertEquals(SONG_ARTIST, result.getArtist());
    assertEquals(SONG_ALBUM, result.getAlbum());
    assertEquals(SONG_LENGTH, result.getLength());
    assertEquals(SONG_YEAR, result.getYear());

    verify(songRepository, times(1)).findById(SONG_ID);
  }

  @Test
  void testGetSongByIdNotFound() {
    Long songId = 100L;
    when(songRepository.findById(songId)).thenReturn(Optional.empty());

    assertThrows(SongNotFoundException.class, () -> songService.getSongById(songId));
  }

  @Test
  void testCreateSongSuccess() throws Exception {
    Song savedSong = prepareSongForTest();
    SongRequestDTO songDTO = TestUtils.prepareSongDTOForTest();

    when(songRepository.save(any(Song.class))).thenReturn(savedSong);

    Song result = songService.createSong(songDTO);

    assertNotNull(result);
    assertEquals(1L, result.getResourceId());
    assertEquals(SONG_NAME, result.getName());
    assertEquals(SONG_ARTIST, result.getArtist());
    assertEquals(SONG_ALBUM, result.getAlbum());
    assertEquals(SONG_LENGTH, result.getLength());
    assertEquals(SONG_YEAR, result.getYear());
  }

  @Test
  void testDeleteSongsSuccess() throws Exception {
    String idsCsv = "1,2";

    List<Long> idsList = Arrays.asList(1L, 2L);
    List<Song> songs = prepareListOfSongs();

    when(songRepository.findAllByResourceIdIsIn(idsList)).thenReturn(songs);

    List<Long> deletedIds = songService.deleteSongs(idsCsv);

    assertNotNull(deletedIds);
    assertEquals(2, deletedIds.size());
    assertTrue(deletedIds.contains(1L));
    assertTrue(deletedIds.contains(2L));
  }

  @Test
  void testDeleteSongsNotFound() throws Exception {
    String idsCsv = "10,20,30";
    List<Long> idsList = Arrays.asList(10L, 20L, 30L);

    when(songRepository.findAllByResourceIdIsIn(idsList)).thenReturn(Collections.emptyList());

    assertNull(songService.deleteSongs(idsCsv));
  }
}
