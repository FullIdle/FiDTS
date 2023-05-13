package fi.fullidle.fidts;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class FileUtil {
    File file;
    public File getFile() {
        return file;
    }

    public FileConfiguration getConfiguration() {
        return configuration;
    }

    FileConfiguration configuration;
    public FileUtil(File file,Boolean newFile){
        if (newFile){
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        this.file = new File(file.getAbsolutePath());
        this.configuration = YamlConfiguration.loadConfiguration(this.file);
    }
    public FileUtil(File file){
        this.file = new File(file.getAbsolutePath());
        this.configuration = YamlConfiguration.loadConfiguration(this.file);
    }
    public void createFile(){
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.file = new File(file.getAbsolutePath());
        this.configuration = YamlConfiguration.loadConfiguration(this.file);
    }

    public void save(FileConfiguration configuration){
        try {
            configuration.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
