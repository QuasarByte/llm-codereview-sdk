package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.service.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ResourceLoaderImpl implements ResourceLoader {

    private static final Logger logger = LoggerFactory.getLogger(ResourceLoaderImpl.class);

    private final ClassLoader classLoader;

    public ResourceLoaderImpl() {
        this.classLoader = getClass().getClassLoader();
        logger.debug("Initialized ResourceLoaderImpl with default class loader: {}", this.classLoader);
    }

    public ResourceLoaderImpl(ClassLoader classLoader) {
        this.classLoader = classLoader;
        logger.debug("Initialized ResourceLoaderImpl with custom class loader: {}", this.classLoader);
    }

    @Override
    public String load(String location) throws IOException {
        logger.info("Loading resource from location: {} (UTF-8)", location);
        return load(location, null);
    }

    @Override
    public String load(String location, String codePage) throws IOException {
        logger.info("Loading resource from location: {} with encoding: {}", location, codePage);
        if (location == null || location.isEmpty()) {
            logger.error("Resource location is null or empty!");
            throw new IllegalArgumentException("Resource location must not be null or empty");
        }

        Charset charset = (codePage != null && !codePage.isEmpty())
                ? Charset.forName(codePage)
                : StandardCharsets.UTF_8;

        logger.debug("Resolved charset: {}", charset.displayName());

        if (location.startsWith("classpath:")) {
            String path = location.substring("classpath:".length());
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            logger.debug("Loading classpath resource: {}", path);
            try (InputStream in = classLoader.getResourceAsStream(path)) {
                if (in == null) {
                    logger.error("Classpath resource not found: {}", path);
                    throw new IOException("Classpath resource not found: " + path);
                }
                String content = new String(readAllBytes(in), charset);
                logger.info("Successfully loaded classpath resource: {}", path);
                return content;
            } catch (IOException e) {
                logger.error("Failed to load classpath resource: {}: {}", path, e.getMessage(), e);
                throw e;
            }
        } else if (location.startsWith("file:")) {
            String path = location.substring("file:".length());
            logger.debug("Loading file resource: {}", path);
            try {
                String content = new String(Files.readAllBytes(Paths.get(path)), charset);
                logger.info("Successfully loaded file resource: {}", path);
                return content;
            } catch (IOException e) {
                logger.error("Failed to load file resource: {}: {}", path, e.getMessage(), e);
                throw e;
            }
        } else {
            // Default: treat as file path
            logger.debug("Loading file system resource: {}", location);
            try {
                String content = new String(Files.readAllBytes(Paths.get(location)), charset);
                logger.info("Successfully loaded file system resource: {}", location);
                return content;
            } catch (IOException e) {
                logger.error("Failed to load file system resource: {}: {}", location, e.getMessage(), e);
                throw e;
            }
        }
    }

    private static byte[] readAllBytes(InputStream input) throws IOException {
        final int bufLen = 4096;
        byte[] buf = new byte[bufLen];
        int readLen;
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            while ((readLen = input.read(buf, 0, bufLen)) != -1) {
                output.write(buf, 0, readLen);
            }
            return output.toByteArray();
        }
    }
}
