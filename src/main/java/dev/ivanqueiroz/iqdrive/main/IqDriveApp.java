package dev.ivanqueiroz.iqdrive.main;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import dev.ivanqueiroz.iqdrive.listeners.FileDownloadProgressListener;
import dev.ivanqueiroz.iqdrive.services.GoogleDriveService;
import dev.ivanqueiroz.iqdrive.services.IQDriveConfig;
import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class IqDriveApp {

    private static final Logger logger = LoggerFactory.getLogger(IqDriveApp.class);
    private static final IQDriveConfig config = ConfigFactory.create(IQDriveConfig.class);

    public static void main(String[] args) throws IOException {

        Drive service = GoogleDriveService.getGDriveService();

        String extensao = ".MPG";
        String pageToken = null;

        do {
            FileList result = searchFiles(service, extensao, pageToken);
            List<File> files = result.getFiles();
            if (files == null || files.isEmpty()) {
                logger.info("Não foram encontrado arquivos.");
            } else {
                logger.info("Arquivos:");
                for (File file : files) {
                    processarArquivoDrive(service, extensao, file);
                }
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
    }

    private static void processarArquivoDrive(Drive service, String extensao, File file) throws IOException {
        logger.info("{} {}", file.getName(), file.getId());
        java.io.File parentDir = new java.io.File(config.downloadDir());
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            logger.error("Não foi possível criar o diretório");
            throw new IOException("Não foi possível criar o diretório");
        }
        final java.io.File arquivo = new java.io.File(parentDir, file.getName());
        if (!arquivo.exists()) {
            if (extensao.replace(".", "").equals(FilenameUtils.getExtension((file.getName())))) {
                downloadArquivo(service, file, arquivo);
            } else {
                logger.info("Arquivo não é o esperado: {}", file.getName());
                logger.info("Extensão: {}", FilenameUtils.getExtension(file.getName()));
            }
        } else {
            logger.info("Excluíndo arquivo: {}", file.getName());
            service.files().delete(file.getId()).execute();
        }
    }

    private static void downloadArquivo(Drive service, File file, java.io.File arquivo) throws IOException {
        try (OutputStream out = new FileOutputStream(arquivo)) {
            final Drive.Files.Get request = service.files().get(file.getId());
            request.getMediaHttpDownloader().setProgressListener(new FileDownloadProgressListener());
            request.executeMediaAndDownloadTo(out);
        }
    }

    private static FileList searchFiles(Drive service, String extensao, String pageToken) throws IOException {
        return service.files().list().setQ("name contains '" + extensao + "'").setSpaces("drive").setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken).execute();
    }
}
