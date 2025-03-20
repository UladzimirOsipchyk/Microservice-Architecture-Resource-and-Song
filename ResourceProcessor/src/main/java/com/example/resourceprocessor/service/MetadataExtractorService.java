package com.example.resourceprocessor.service;


import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;

@Service
public class MetadataExtractorService {
  public Metadata extractMetadata(String fileName) throws Exception {
    FileInputStream inputStream = new FileInputStream(fileName);
    Mp3Parser parser = new Mp3Parser();
    BodyContentHandler handler = new BodyContentHandler();
    Metadata metadata = new Metadata();
    ParseContext context = new ParseContext();
    parser.parse(inputStream, handler, metadata, context);
    return metadata;
  }

  public static String formatDuration(String seconds) {
    double secondsInDouble = Double.parseDouble(seconds);
    int totalSeconds = (int) Math.round(secondsInDouble);
    int minutes = totalSeconds / 60;
    int remainingSeconds = totalSeconds % 60;
    return String.format("%02d:%02d", minutes, remainingSeconds);
  }
}
