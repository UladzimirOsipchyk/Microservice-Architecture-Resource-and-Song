package com.example.songservice.controller;

import com.example.songservice.dto.CreatedSongDTO;
import com.example.songservice.dto.DeletedSongsDTO;
import com.example.songservice.dto.SongDTO;
import com.example.songservice.dto.SongRequestDTO;
import com.example.songservice.model.Song;
import com.example.songservice.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/songs")

public class SongController {

  @Autowired
  private SongService songService;

  @GetMapping("/{id}")
  public ResponseEntity<SongDTO> getSongById(@PathVariable Long id) throws Exception {
    Song song = songService.getSongById(id);
    SongDTO songDTO = new SongDTO(
        song.getName(),
        song.getArtist(),
        song.getAlbum(),
        song.getLength(),
        song.getYear()
    );
    return new ResponseEntity<>(songDTO, HttpStatus.OK);
  }

  @PostMapping
  public ResponseEntity<CreatedSongDTO> createSong(@RequestBody SongRequestDTO songRequestDTO) throws Exception {
    Song song = songService.createSong(songRequestDTO);
    return new ResponseEntity<>(new CreatedSongDTO(song.getId()), HttpStatus.OK);
  }

  @DeleteMapping()
  public ResponseEntity<DeletedSongsDTO> deleteResource(@RequestParam String ids) throws Exception{
    List<Long> deletedIds = songService.deleteSongs(ids);
    return new ResponseEntity<>(new DeletedSongsDTO(deletedIds), HttpStatus.OK);
  }

}
