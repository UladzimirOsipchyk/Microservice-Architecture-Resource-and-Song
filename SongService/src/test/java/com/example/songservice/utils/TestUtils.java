package com.example.songservice.utils;

import com.example.songservice.dto.SongRequestDTO;
import com.example.songservice.model.Song;

import java.util.List;

public class TestUtils {
  public final static Long SONG_ID = 1L;
  public final static String SONG_NAME = "Bohemian Rhapsody";
  public final static String SONG_ARTIST = "Queen";
  public final static String SONG_ALBUM = "A Night at the Opera";
  public final static String SONG_LENGTH = "15:20";
  public final static String SONG_YEAR = "1975";
  public static Song prepareSongForTest() {
    Song song = new Song();
    song.setId(SONG_ID);
    song.setName(SONG_NAME);
    song.setArtist(SONG_ARTIST);
    song.setAlbum(SONG_ALBUM);
    song.setLength(SONG_LENGTH);
    song.setYear(SONG_YEAR);

    song.setResourceId(SONG_ID);
    return song;
  }

  public static List<Song> prepareListOfSongs() {
    Song songOne = prepareSongForTest();
    Song songTwo = new Song();
    songTwo.setId(2L);
    songTwo.setName("Song2");
    songTwo.setArtist("Artist2");
    songTwo.setAlbum("Album2");

    return List.of(songOne, songTwo);
  }

  public static SongRequestDTO prepareSongDTOForTest() {
    return new SongRequestDTO(
        SONG_ID,
        SONG_NAME,
        SONG_ARTIST,
        SONG_ALBUM,
        SONG_LENGTH,
        SONG_YEAR
    );
  }
}
