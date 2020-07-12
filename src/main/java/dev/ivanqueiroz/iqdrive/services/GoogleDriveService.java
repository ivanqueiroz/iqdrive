package dev.ivanqueiroz.iqdrive.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.ivanqueiroz.iqdrive.main.IqDriveApp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class GoogleDriveService {
    private final static Logger logger = LoggerFactory.getLogger(GoogleDriveService.class);

    private static final String ARQUIVO_CREDENCIAIS = "/credentials.json";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static Drive service;
    private static final String NOME_APLICACAO = "IQDrive";

    public Drive getGDriveService(){
        if(service == null) {
            NetHttpTransport HTTP_TRANSPORT = null;
            try {
                HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, autenticacao(HTTP_TRANSPORT)).setApplicationName(NOME_APLICACAO).build();
            } catch (GeneralSecurityException e) {
                logger.error("Erro ao configurar serviço.", e);
            } catch (IOException e) {
                logger.error("Erro ao configurar serviço.", e);
            }
        }
        return this.service;
    }

    private static Credential autenticacao(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = IqDriveApp.class.getResourceAsStream(ARQUIVO_CREDENCIAIS);
        if (in == null) {
            logger.error("Recurso não encontrado.");
            throw new FileNotFoundException("Recurso não encontrado: " + ARQUIVO_CREDENCIAIS);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
