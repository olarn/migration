package com.ttb.crm.service.migrationdata.controller;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.DelegatingServletOutputStream;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
@ExtendWith(MockitoExtension.class)
class LogDownloadControllerTest {

    @InjectMocks
    LogDownloadController logDownloadController;

    @Mock
    private HttpServletResponse response;

    @AfterEach
    void cleanup() throws IOException {
        Path logDir = Paths.get("./logs");
        if (Files.exists(logDir)) {
            try (var walk = Files.walk(logDir)) {
                walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                });
            }
        }
    }
    @Test
    void test_downloadLogs_success() throws IOException {
        Path logDir = Paths.get("./logs");
        Files.createDirectories(logDir);
        Path logFile = logDir.resolve("log.txt");
        Files.writeString(logFile, "mock log");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ServletOutputStream servletOutputStream = new DelegatingServletOutputStream(outputStream);

        when(response.getOutputStream()).thenReturn(servletOutputStream);

        logDownloadController.downloadLogs(response);

        verify(response).setContentType("application/zip");
        verify(response).setHeader("Content-Disposition", "attachment; filename=logs.zip");

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(outputStream.toByteArray()))) {
            ZipEntry entry = zis.getNextEntry();
            assertNotNull(entry);
        }
    }
    @Test
    void test_downloadLogs_missingDir_returns404() throws Exception {
        Path logDir = Paths.get("./logs");

        var controller = new LogDownloadController();
        var resp = new MockHttpServletResponse();

        controller.downloadLogs(resp);

        assertEquals(404, resp.getStatus());
        assertTrue(resp.getContentAsString().contains("Log directory not found."));
    }
    @Test
    void test_downloadLogs_whenWriteFails_throwsUncheckedIOException() throws Exception {
        Path logDir = Paths.get("./logs");
        Files.createDirectories(logDir);
        Files.writeString(logDir.resolve("a.log"), "x");

        var controller = new LogDownloadController();

        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override public void write(int b) throws IOException { throw new IOException("boom"); }
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener writeListener) {}
        });

        assertThrows(UncheckedIOException.class, () -> controller.downloadLogs(resp));
    }
    @Test
    void test_clearLogs_success() throws IOException {
        Path logDir = Paths.get("./logs");
        Files.createDirectories(logDir);
        Path log1 = Files.writeString(logDir.resolve("log1.txt"), "log1");
        logDownloadController = new LogDownloadController();
        ResponseEntity<String> responseLog = logDownloadController.clearLogs();
        assertEquals(HttpStatus.OK, responseLog.getStatusCode());
        Files.deleteIfExists(log1);
        Files.deleteIfExists(logDir);
    }
}