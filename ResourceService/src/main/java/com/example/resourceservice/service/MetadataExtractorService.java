package com.example.resourceservice.service;

import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.springframework.stereotype.Service;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

@Service
public class MetadataExtractorService {
  public Metadata extractMetadata(byte[] fileData) throws Exception {
    InputStream inputStream = new ByteArrayInputStream(fileData);
    Mp3Parser parser = new Mp3Parser();
    BodyContentHandler handler = new BodyContentHandler();
    Metadata metadata = new Metadata();
    ParseContext context = new ParseContext();
    parser.parse(inputStream, handler, metadata, context);
    return metadata;
  }
}
