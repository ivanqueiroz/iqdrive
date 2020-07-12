package dev.ivanqueiroz.iqdrive.main;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import dev.ivanqueiroz.iqdrive.listeners.FileDownloadProgressListener;
import dev.ivanqueiroz.iqdrive.services.GoogleDriveService;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.List;

public class IqDriveApp {

    private final static Logger logger = LoggerFactory.getLogger(IqDriveApp.class);

    private static final String DIR_FOR_DOWNLOADS = "/home/ivanqueiroz/Downloads/drive-videos";

    public static void main(String[] args) throws IOException, GeneralSecurityException {

        Drive service = new GoogleDriveService().getGDriveService();

        String extensao = ".mpg";
        String pageToken = null;
        FileList result = service.files().list()
            .setQ("name contains '"+extensao+"'")
            .setSpaces("drive")
            .setFields("nextPageToken, files(id, name)")
            .setPageToken(pageToken)
            .execute();

        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            logger.info("Não foram encontrado arquivos.");
        } else {
            logger.info("Arquivos:");
            do {
            for (File file : files) {
                logger.info("{} {}", file.getName(), file.getId());
                java.io.File parentDir = new java.io.File(DIR_FOR_DOWNLOADS);
                if (!parentDir.exists() && !parentDir.mkdirs()) {
                    logger.error("Não foi possível criar o diretório");
                    throw new IOException("Não foi possível criar o diretório");
                }
                final java.io.File arquivo = new java.io.File(parentDir, file.getName());
                if(!arquivo.exists()) {
                    if(extensao.replace(".","").equals(FilenameUtils.getExtension((file.getName())))) {
                        try (OutputStream out = new FileOutputStream(arquivo)) {
                            final Drive.Files.Get request = service.files().get(file.getId());
                            request.getMediaHttpDownloader().setProgressListener(new FileDownloadProgressListener());
                            request.executeMediaAndDownloadTo(out);
                        }
                    }else{
                        logger.info("Arquivo não é o esperado: "+file.getName() );
                        logger.info("Extensão: "+FilenameUtils.getExtension(file.getName()));
                    }
                } else {
                    logger.info("Excluíndo arquivo: "+file.getName());
                    service.files().delete(file.getId()).execute();
                }
            }
                pageToken = result.getNextPageToken();
            } while (pageToken != null);
        }

    }
}
