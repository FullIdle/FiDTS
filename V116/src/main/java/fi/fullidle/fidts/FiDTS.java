package fi.fullidle.fidts;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class FiDTS extends JavaPlugin {

    static FiDTS plugin;

    @Override
    public void onLoad() {
        File data = new File(getDataFolder().getAbsolutePath()
        +File.separatorChar+"data");
        data.mkdirs();
    }

    @Override
    public void onEnable() {
        plugin = this;
        getCommand("fidts").setExecutor(new Commands());
        getLogger().info("Plugin Enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin Disable");
    }
}
