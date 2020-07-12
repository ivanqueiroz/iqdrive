package dev.ivanqueiroz.iqdrive.services;

import org.aeonbits.owner.Config;

@Config.Sources({ "file:~/.config.properties",
    "file:./config/config.properties" })
public interface IQDriveConfig extends Config {

    @Key("download.dir")
    String downloadDir();

    @Key("credenciais.json")
    String credenciaisJson();
}
