package com.example.songservice.service;

import com.example.songservice.dto.SongRequestDTO;
import com.example.songservice.exception.exceptions.FieldsFormatException;
import com.example.songservice.exception.exceptions.InvalidCsvLengthException;
import com.example.songservice.exception.exceptions.SongNotFoundException;
import com.example.songservice.model.Song;
import com.example.songservice.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class SongService {
  @Autowired
  private SongRepository songRepository;

  public Song getSongById(Long id) {
    Optional<Song> song = songRepository.findById(id);

    if (song.isPresent()) {
      return song.get();
    } else {
      throw new SongNotFoundException(String.valueOf(id));
    }
  }

  public Song createSong(SongRequestDTO songRequestDTO) throws Exception{
    validateSongDto(songRequestDTO);

    Song song = new Song();
    song.setResourceId(songRequestDTO.getResourceId());
    song.setName(songRequestDTO.getName());
    song.setArtist(songRequestDTO.getArtist());
    song.setAlbum(songRequestDTO.getAlbum());
    song.setLength(songRequestDTO.getDuration());
    song.setYear(songRequestDTO.getYear());
    songRepository.save(song);

    return song;
  }

  public List<Long> deleteSongs(String ids) throws Exception {
    if (ids.length() > 200) {
      throw new InvalidCsvLengthException();
    }

    List<Long> idsList = Arrays.stream(ids.replaceAll(" ", "").split(","))
        .map(Long::parseLong)
        .toList();

    List<Song> songs = songRepository.findAllByResourceIdIsIn(idsList);
    if (!songs.isEmpty()) {
      songRepository.deleteAll(songs);
      return idsList;
    }

    return null;
  }

  public void validateSongDto(SongRequestDTO songRequestDTO) {
    Map<String, String> validationErrors = new HashMap<>();

    if (!isValidName(songRequestDTO.getName())) {
      validationErrors.put("name", "Song name is required");
    }

    if (!isValidDuration(songRequestDTO.getDuration())) {
      validationErrors.put("duration", "Duration must be in the format MM:SS");
    }

    if (!isValidYear(songRequestDTO.getYear())) {
      validationErrors.put("year", "Year must be in a YYYY format");
    }

    if (!validationErrors.isEmpty()) {
      throw new FieldsFormatException(validationErrors);
    }
  }

  private Boolean isValidName(String name) {
    if (name == null || name.isEmpty() || name.isBlank()) {
      return false;
    }

    return true;
  }


  private Boolean isValidDuration(String duration) {
    if (duration == null || !duration.matches("\\d{2}:\\d{2}")) {
      return false;
    }

    String[] parts = duration.split(":");
    int minutes = Integer.parseInt(parts[0]);
    int seconds = Integer.parseInt(parts[1]);

    if (minutes < 0 || seconds < 0 || seconds >= 60) {
      return false;
    }

    return true;
  }

  private Boolean isValidYear(String year) {
    try {
      Year.parse(year, DateTimeFormatter.ofPattern("yyyy"));
    } catch (DateTimeParseException ex) {
      return false;
    }

    return true;
  }
}
