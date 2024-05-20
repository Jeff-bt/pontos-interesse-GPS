package com.jeffdev.uploaddownloadapi.service;

import com.jeffdev.uploaddownloadapi.exception.FileTooLargeException;
import com.jeffdev.uploaddownloadapi.exception.InvalidFileException;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FileService {

    //Cria o nome do baú
    private static final Path CSV_STORAGE_FOLDER = Paths.get("src/main/resources/csvStorage");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy_MM_dd_HHmmss");

    private final CSVLoaderService csvLoader;

    public FileService(CSVLoaderService csvLoader) {
        this.csvLoader = csvLoader;

        init();
    }

    //Cria o baú
    public void init() {
        try {
            Files.createDirectories(CSV_STORAGE_FOLDER);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directory", e);
        }
    }

    //Joga o arquivo para dentro do baú
    public void upload(MultipartFile file) {
        try {
            validateFile(file);
            String fileName = generateFileName(file);
            Path filePath = CSV_STORAGE_FOLDER.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error copying file");
        }
    }

    private String generateFileName(MultipartFile file) {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER) + "__" + file.getOriginalFilename();
    }

    private void validateFile(MultipartFile file) {
        if (!file.getContentType().equals("text/csv")) {
            throw new InvalidFileException("Invalid file type. Only CSV files are allowed.");
        }
        if (file.getSize() > 3072L * 1024 * 1024) { // 3 Gb
            throw new FileTooLargeException("File size exceeds the limit.");
        }
    }

    public Resource download(String fileName) {
        try {
            csvLoader.extractColumnCsv(fileName);
            Path filePath = CSV_STORAGE_FOLDER.resolve(fileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found.");
            }

        } catch (IOException | CsvException e) {
            throw new RuntimeException("Error downloading file", e);
        }
    }
}
