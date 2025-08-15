package com.ttb.crm.service.migrationdata.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("internal")
@AllArgsConstructor
public class LogDownloadController {

    private static final String LOG_DIR_PATH = "./logs";

    @PostMapping("/log/download")
    public void downloadLogs(HttpServletResponse response) throws IOException {
        Path logDir = Paths.get(LOG_DIR_PATH);

        if (!Files.exists(logDir) || !Files.isDirectory(logDir)) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.getWriter().write("Log directory not found.");
            return;
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=logs.zip");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            zipDirectory(logDir, logDir.getFileName().toString(), zos);
        }
    }

    private void zipDirectory(Path folder, String parentFolder, ZipOutputStream zos) throws IOException {
        try (Stream<Path> paths = Files.list(folder)) {
            paths.forEach(path -> {
                try {
                    String zipEntryName = "%s/%s".formatted(parentFolder, path.getFileName().toString());
                    if (Files.isDirectory(path)) {
                        zipDirectory(path, zipEntryName, zos);
                    } else {
                        zos.putNextEntry(new ZipEntry(zipEntryName));
                        Files.copy(path, zos);
                        zos.closeEntry();
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException("Error zipping file: %s".formatted(path), e);
                }
            });
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearLogs() {
        File logDir = new File(LOG_DIR_PATH);

        if (!logDir.exists() || !logDir.isDirectory()) {
            return ResponseEntity.badRequest().body("Logs directory does not exist or is not a directory.");
        }

        File[] files = logDir.listFiles();
        if (files == null) {
            return ResponseEntity.internalServerError().body("Failed to list log files.");
        }

        long deletedCount = Stream.of(files)
                .filter(File::isFile)
                .filter(File::exists)
                .map(File::delete)
                .filter(Boolean::booleanValue)
                .count();

        return ResponseEntity.ok("Deleted %d log files.".formatted(deletedCount));
    }
}
