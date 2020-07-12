package dev.ivanqueiroz.iqdrive.listeners;

import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileDownloadProgressListener implements MediaHttpDownloaderProgressListener {
    private static final Logger logger = LoggerFactory.getLogger(FileDownloadProgressListener.class);
    @Override public void progressChanged(MediaHttpDownloader downloader) {

        switch (downloader.getDownloadState()) {
            case NOT_STARTED:
                logger.info("Não iniciado");
                break;
            case MEDIA_IN_PROGRESS:
                logger.info("Download em progresso: {}", downloader.getProgress());
                break;
            case MEDIA_COMPLETE:
                logger.info("Download completo!");
                break;
        }

    }
}
