package com.jeffdev.uploaddownloadapi.service;

import com.opencsv.*;
import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class CSVLoaderService {

    private static final Logger logger = LoggerFactory.getLogger(CSVLoaderService.class);

    private static final char COLUMN_SEPARATOR = ';';
    private static final String BASE_FILE_PATH = "src/main/resources/csvStorage/";
    private static final String TEMP_FILE_NAME = "file.tmp";

    public void extractColumnCsv(String fileName) throws IOException, CsvException {
        Path originalFilePath = Paths.get(BASE_FILE_PATH + fileName);
        Path tempFilePath = Paths.get(BASE_FILE_PATH + TEMP_FILE_NAME);

        long startTime = System.currentTimeMillis(); // temporário

        try (
                CSVReader csvReader = createCsvReader(originalFilePath);
                ICSVWriter csvWriter = createCsvWriter(tempFilePath)
        ) {
            // Escreve o cabeçalho do arquivo de saída
            csvWriter.writeNext(new String[]{"descricao", "linha"});

            // Lê e processa cada linha do arquivo CSV
            int currentLine = 0;
            String[] csvLine;
            while ((csvLine = csvReader.readNext()) != null) {
                String[] outputLine = processLine(csvLine, currentLine);
                csvWriter.writeNext(outputLine);
                currentLine++;
            }

            long endTime = System.currentTimeMillis(); // temporário
            long totalTime = endTime - startTime; // temporário
            logger.info("Total CSV processing time: {}ms", totalTime); // temporário

        } catch (CsvException e) {
            logger.error("Error processing CSV file: {}", e.getMessage(), e);
            throw new CsvException("Error processing CSV file: " + e.getMessage());
        } catch (IOException e) {
            logger.error("Error reading or writing CSV file: {}", e.getMessage(), e);
            throw new IOException("Error reading or writing CSV file: " + e.getMessage(), e);
        } finally {
            // Move o arquivo temporário para o nome original
            moveFile(tempFilePath, originalFilePath);
        }
    }

    private CSVReader createCsvReader(Path filePath) throws IOException {
        return new CSVReaderBuilder(new FileReader(filePath.toString(), StandardCharsets.UTF_8))
                .withSkipLines(1)
                .withCSVParser(new CSVParserBuilder().withSeparator(COLUMN_SEPARATOR).build())
                .build();
    }

    private ICSVWriter createCsvWriter(Path filePath) throws IOException {
        return new CSVWriterBuilder(new FileWriter(filePath.toString(), StandardCharsets.UTF_8))
                .withSeparator(COLUMN_SEPARATOR)
                .build();
    }

    private String[] processLine(String[] csvLine, int currentLine) {
        // Extrai os dados da linha CSV e processa-os
        String descricao = csvLine[13];
        int lineNumber = currentLine + 2;
        return new String[]{descricao, String.valueOf(lineNumber)};
    }

    private void moveFile(Path tempFilePath, Path originalFilePath) throws IOException {
        // Exclui o arquivo original
        Files.deleteIfExists(originalFilePath);
        // Move o arquivo temporário para o nome original
        Files.move(tempFilePath, originalFilePath);
    }
}
