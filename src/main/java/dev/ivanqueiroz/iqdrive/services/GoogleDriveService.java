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
import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class GoogleDriveService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleDriveService.class);
    private static final IQDriveConfig config = ConfigFactory.create(IQDriveConfig.class);
    private static final String ARQUIVO_CREDENCIAIS = config.credenciaisJson();
    private static final String NOME_APLICACAO = "IQDrive";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static Drive service;

    private GoogleDriveService(){
    }

    public static Drive getGDriveService(){
        if(service == null) {
            NetHttpTransport netHttpTransport;
            try {
                netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
                service = new Drive.Builder(netHttpTransport, JSON_FACTORY, autenticacao(netHttpTransport)).setApplicationName(NOME_APLICACAO).build();
            } catch (GeneralSecurityException | IOException e) {
                logger.error("Erro ao configurar serviço.", e);
            }
        }
        return service;
    }

    private static Credential autenticacao(final NetHttpTransport httpTransport) throws IOException {
        //Criar em https://code.google.com/apis/console/?api=drive
        java.io.File credenciais = new java.io.File(ARQUIVO_CREDENCIAIS);
        InputStream in = FileUtils.openInputStream(credenciais);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
