package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.exception.CannotReadFileException;
import com.quasarbyte.llm.codereview.sdk.model.SourceFile;
import com.quasarbyte.llm.codereview.sdk.service.SourceFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.util.Optional;

public class SourceFileReaderImpl implements SourceFileReader {

    private static final Logger logger = LoggerFactory.getLogger(SourceFileReaderImpl.class);

    @Override
    public SourceFile readFile(String filePath, String codePage) {
        logger.info("Reading file: '{}', codePage: '{}'", filePath, codePage);
        Path path = Paths.get(filePath);
        try {
            byte[] content = Files.readAllBytes(path);
            logger.debug("Read {} bytes from file '{}'", content.length, filePath);

            String fileName = path.getFileName().toString();
            String fileNameExtension = getFileExtension(path).orElse(null);
            if (fileNameExtension == null) {
                logger.debug("No file extension found for file '{}'", filePath);
            } else {
                logger.debug("File extension for '{}': '{}'", filePath, fileNameExtension);
            }

            Long fileSize = Files.size(path);
            logger.debug("File size for '{}': {} bytes", filePath, fileSize);

            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            logger.debug("File timestamps for '{}': created={}, modified={}, accessed={}",
                    filePath,
                    attrs.creationTime(),
                    attrs.lastModifiedTime(),
                    attrs.lastAccessTime());

            SourceFile sourceFile = new SourceFile()
                    .setContent(content)
                    .setFilePath(filePath)
                    .setFileName(fileName)
                    .setFileNameExtension(fileNameExtension)
                    .setSize(fileSize)
                    .setCreatedAt(attrs.creationTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                    .setModifiedAt(attrs.lastModifiedTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                    .setAccessedAt(attrs.lastAccessTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                    .setCodePage(codePage);

            logger.info("Successfully read file '{}'", filePath);
            return sourceFile;
        } catch (IOException e) {
            logger.error("Failed to read file '{}': {}", filePath, e.getMessage(), e);
            throw new CannotReadFileException(String.format("Cannot read file '%s', error: '%s'", filePath, e.getMessage()), e);
        }
    }

    private static Optional<String> getFileExtension(Path path) {
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            String ext = fileName.substring(dotIndex + 1).toLowerCase();
            logger.trace("Extracted file extension '{}' from file '{}'", ext, fileName);
            return Optional.of(ext);
        }
        logger.trace("No file extension extracted from file '{}'", fileName);
        return Optional.empty();
    }

}