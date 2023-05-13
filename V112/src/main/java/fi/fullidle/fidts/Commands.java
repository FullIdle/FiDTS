package fi.fullidle.fidts;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.attacks.AttackBase;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Moveset;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.items.EnumPokeballs;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.util.*;

import static fi.fullidle.fidts.FiDTS.plugin;

public class Commands implements CommandExecutor {
    String[] help = new String[]{
            "help",
            "upload",
            "download",
            "confirm"
    };
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = sender instanceof Player ? ((Player) sender):null;
        if (args.length >= 1){
            if (args[0].equalsIgnoreCase("upload")) {
                if (player == null) {sender.sendMessage("该指令只能有玩家执行");return false;}
                player.removeMetadata(FiDTS.class.toString(),plugin);
                player.setMetadata(FiDTS.class.toString(),new FixedMetadataValue(plugin,"upload"));
                sender.sendMessage("请使用/fidts confirm进行确认");
                return false;
            }
            if (args[0].equalsIgnoreCase("download")){
                if (player == null) {sender.sendMessage("该指令只能有玩家执行");return false;}
                player.removeMetadata(FiDTS.class.toString(),plugin);
                player.setMetadata(FiDTS.class.toString(),new FixedMetadataValue(plugin,"download"));
                sender.sendMessage("请使用/fidts confirm进行确认");
                return false;
            }
            if (args[0].equalsIgnoreCase("confirm")){
                if (player == null) {sender.sendMessage("该指令只能有玩家执行");return false;}
                if (!(player.getMetadata(FiDTS.class.toString()).size() > 0)){
                    sender.sendMessage("没有需要确认的内容");
                    return false;
                }
                String com = player.getMetadata(FiDTS.class.toString()).get(0).asString();
                File file = new File(plugin.getDataFolder().getAbsolutePath()+File.separatorChar+"data"+File.separatorChar+player.getName()+".yml");
                System.out.println(file.getAbsolutePath());
                FileUtil fileUtil = new FileUtil(file);
                PlayerPartyStorage pps = Pixelmon.storageManager.getParty(player.getUniqueId());
                PCStorage pcs = Pixelmon.storageManager.getPCForPlayer(player.getUniqueId());
                if (com.equalsIgnoreCase("upload")) {
                    sender.sendMessage("正在上传中...");
                    fileUtil.createFile();
                    FileConfiguration con = fileUtil.getConfiguration();
                    List<String> bList = new ArrayList<>();
                    List<String> pList = new ArrayList<>();
                    List<String> itemList = new ArrayList<>();
                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin,()->{
                        int slot = 0;
                        for (Pokemon pokemon : pps.getAll()) {
                            if (pokemon !=null){
                                bList.add(serializationPokemon(pokemon));
                                if (!pokemon.getHeldItem().toString().contains("air")){
                                    itemList.add(pokemon.getHeldItem().toString());
                                }
                                slot++;
                                player.sendTitle("","已上次:"+slot+"只");
                            }
                        }
                        for (Pokemon pokemon : pcs.getAll()) {
                            if (pokemon !=null){
                                pList.add(serializationPokemon(pokemon));
                                if (!pokemon.getHeldItem().toString().contains("air")){
                                    itemList.add(pokemon.getHeldItem().toString());
                                }
                                slot++;
                                player.sendTitle("","已上次:"+slot+"只");
                            }
                        }
                        for (Pokemon pokemon : pps.getAll()) {
                            if (pokemon!=null){
                                pps.set(pokemon.getPosition(),null);
                            }
                        }
                        for (Pokemon pokemon : pcs.getAll()) {
                            if (pokemon!=null){
                                pcs.set(pokemon.getPosition(),null);
                            }
                        }
                        con.set("Backpack",bList);
                        con.set("PC",pList);
                        con.set("V1_12",itemList);
                        fileUtil.save(con);
                        sender.sendMessage("上传成功了");
                    });
                    player.removeMetadata(FiDTS.class.toString(),plugin);
                    return false;
                }
                if (com.equalsIgnoreCase("download")){
                    if (fileUtil.getFile().exists()){
                        sender.sendMessage("正在下载");
                        FileConfiguration con = fileUtil.getConfiguration();
                        List<String> bList = con.getStringList("Backpack");
                        List<String> pList = con.getStringList("PC");
                        plugin.getServer().getScheduler().runTaskAsynchronously(plugin,()->{
                            int slot = 1;
                            for (String s : bList) {
                                sender.sendMessage(s);
                                deserializationPokemon(s,pps);
                                player.sendTitle("","已添加:"+slot+"只");
                                slot++;
                            }
                            for (String s : pList) {
                                deserializationPokemon(s,pcs);
                                player.sendTitle("","已添加:"+slot+"只");
                                slot++;
                            }
                            sender.sendMessage("下载完成");
                        });
                    }else{
                        sender.sendMessage("你没有数据可以获取");
                    }
                    player.removeMetadata(FiDTS.class.toString(),plugin);
                    return false;
                }
                return false;
            }
            sendHelp(sender);
        }else{
            sendHelp(sender);
        }
        return false;
    }
    public void sendHelp(CommandSender sender) {
        for (String s : help) {
            sender.sendMessage(s);
        }
    }
    public String serializationPokemon(Pokemon pokemon){
        StringBuilder builder = new StringBuilder();
        builder.append("IsEgg:").append(pokemon.isEgg()).append(";");
        builder.append("UUID:").append(pokemon.getUUID()).append(";");
        builder.append("Position.order:").append(pokemon.getPosition().order).append(";");
        builder.append("Position.box:").append(pokemon.getPosition().box).append(";");
        builder.append("Species.name:").append(pokemon.getSpecies().name).append(";");
        builder.append("Experience:").append(pokemon.getExperience()).append(";");
        builder.append("Level:").append(pokemon.getLevel()).append(";");
        builder.append("DynamaxLevel:").append(pokemon.getDynamaxLevel()).append(";");
        builder.append("IVs.hp:").append(pokemon.getIVs().getStat(StatsType.HP)).append(";");
        builder.append("IVs.attack:").append(pokemon.getIVs().getStat(StatsType.Attack)).append(";");
        builder.append("IVs.defence:").append(pokemon.getIVs().getStat(StatsType.Defence)).append(";");
        builder.append("IVs.specialAttack:").append(pokemon.getIVs().getStat(StatsType.SpecialAttack)).append(";");
        builder.append("IVs.specialDefence:").append(pokemon.getIVs().getStat(StatsType.SpecialDefence)).append(";");
        builder.append("IVs.speed:").append(pokemon.getIVs().getStat(StatsType.Speed)).append(";");
        builder.append("EVs.hp:").append(pokemon.getEVs().getStat(StatsType.HP)).append(";");
        builder.append("EVs.attack:").append(pokemon.getEVs().getStat(StatsType.Attack)).append(";");
        builder.append("EVs.defence:").append(pokemon.getEVs().getStat(StatsType.Defence)).append(";");
        builder.append("EVs.specialAttack:").append(pokemon.getEVs().getStat(StatsType.SpecialAttack)).append(";");
        builder.append("EVs.specialDefence:").append(pokemon.getEVs().getStat(StatsType.SpecialDefence)).append(";");
        builder.append("EVs.speed:").append(pokemon.getEVs().getStat(StatsType.Speed)).append(";");
        builder.append("DoesLevel:").append(pokemon.doesLevel()).append(";");
        builder.append("Ability:").append(pokemon.getAbility().getName()).append(";");
        builder.append("AbilitySlot:").append(pokemon.getAbilitySlot()).append(";");
        int i = 0;
        for (Attack attack : pokemon.getMoveset()) {
            if (attack != null){
                builder.append("Moveset.").append(i).append(":").append(attack.getMove().getAttackName()).append(";");
            }
            i++;
        }
        builder.append("CaughtBall:").append(pokemon.getCaughtBall().name()).append(";");
        builder.append("Health:").append(pokemon.getHealth()).append(";");
        builder.append("IsShiny:").append(pokemon.isShiny()).append(";");
        builder.append("Gender:").append(pokemon.getGender().getName()).append(";");
        builder.append("EggCycles:").append(pokemon.getEggCycles()).append(";");
        builder.append("EggSteps:").append(pokemon.getEggSteps()).append(";");
        builder.append("Form:").append(pokemon.getForm()).append(";");
        builder.append("Nickname:").append(pokemon.getNickname() == null ? "" : pokemon.getNickname()).append(";");
        return builder.toString();
    }

    public Pokemon deserializationPokemon(String data, PlayerPartyStorage pps){
        Map<String,String> map = new HashMap<>();
        String[] or = data.split(";");
        for (String ors : or) {
            String[] strings = ors.split(":");
            if (strings.length == 2){
                map.put(strings[0],strings[1]);
            }
        }
        Pokemon pokemon = Pixelmon.pokemonFactory.create(EnumSpecies.getFromNameAnyCase(map.get("Species.name")));
        pokemon.setUUID(UUID.fromString(map.get("UUID")));
        if (Boolean.parseBoolean(map.get("IsEgg"))){
            pokemon.setEggCycles(Integer.valueOf(map.get("EggCycles")));
            pokemon.setEggSteps(Integer.valueOf(map.get("EggSteps")));
        }
        pokemon.setNickname(map.get("Nickname"));
        pokemon.setExperience(Integer.parseInt(map.get("Experience")));
        pokemon.setLevel(Integer.parseInt(map.get("Level")));
        pokemon.setDynamaxLevel(Integer.parseInt(map.get("DynamaxLevel")));
        pokemon.setDoesLevel(Boolean.parseBoolean(map.get("DoesLevel")));
        pokemon.getIVs().setStat(StatsType.HP, Integer.parseInt(map.get("IVs.hp")));
        pokemon.getIVs().setStat(StatsType.Attack, Integer.parseInt(map.get("IVs.attack")));
        pokemon.getIVs().setStat(StatsType.SpecialAttack, Integer.parseInt(map.get("IVs.defence")));
        pokemon.getIVs().setStat(StatsType.Defence, Integer.parseInt(map.get("IVs.specialAttack")));
        pokemon.getIVs().setStat(StatsType.SpecialDefence, Integer.parseInt(map.get("IVs.speed")));
        pokemon.getIVs().setStat(StatsType.Speed, Integer.parseInt(map.get("IVs.specialDefence")));
        pokemon.getEVs().setStat(StatsType.HP, Integer.parseInt(map.get("EVs.hp")));
        pokemon.getEVs().setStat(StatsType.Attack, Integer.parseInt(map.get("EVs.attack")));
        pokemon.getEVs().setStat(StatsType.SpecialAttack, Integer.parseInt(map.get("EVs.defence")));
        pokemon.getEVs().setStat(StatsType.Defence, Integer.parseInt(map.get("EVs.specialAttack")));
        pokemon.getEVs().setStat(StatsType.SpecialDefence, Integer.parseInt(map.get("EVs.speed")));
        pokemon.getEVs().setStat(StatsType.Speed, Integer.parseInt(map.get("EVs.specialDefence")));
        pokemon.setAbility(map.get("Ability"));
        pokemon.setAbilitySlot(Integer.parseInt(map.get("AbilitySlot")));
        Moveset moveset = new Moveset();
        for (int i = 0;i < 4;i++){
            String attackName = map.get("Moveset."+i);
            if (attackName != null){
                Attack attack = new Attack(AttackBase.getAttackBaseFromEnglishName(attackName).get());
                moveset.add(attack);
            }
        }
        pokemon.setMoveset(moveset);
        pokemon.setCaughtBall(EnumPokeballs.getPokeballFromString(map.get("CaughtBall")));
        pokemon.setHealth(Integer.parseInt(map.get("Health")));
        pokemon.setShiny(Boolean.parseBoolean(map.get("IsShiny")));
        pokemon.setGender(Gender.getGender(map.get("Gender")));
        pokemon.setForm(Integer.parseInt(map.get("Form")));

        if (pps != null){
            StoragePosition position = new StoragePosition(Integer.parseInt(map.get("Position.box")),Integer.parseInt(map.get("Position.order")));
            pps.set(position,pokemon);
        }
        return pokemon;
    }
    public Pokemon deserializationPokemon(String data, PCStorage pcs){
        Map<String,String> map = new HashMap<>();
        String[] or = data.split(";");
        for (String ors : or) {
            String[] strings = ors.split(":");
            if (strings.length == 2){
                map.put(strings[0],strings[1]);
            }
        }
        Pokemon pokemon = Pixelmon.pokemonFactory.create(EnumSpecies.getFromNameAnyCase(map.get("Species.name")));
        pokemon.setUUID(UUID.fromString(map.get("UUID")));
        if (Boolean.parseBoolean(map.get("IsEgg"))){
            pokemon.setEggCycles(Integer.valueOf(map.get("EggCycles")));
            pokemon.setEggSteps(Integer.valueOf(map.get("EggSteps")));
        }
        pokemon.setNickname(map.get("Nickname"));
        pokemon.setExperience(Integer.parseInt(map.get("Experience")));
        pokemon.setLevel(Integer.parseInt(map.get("Level")));
        pokemon.setDynamaxLevel(Integer.parseInt(map.get("DynamaxLevel")));
        pokemon.setDoesLevel(Boolean.parseBoolean(map.get("DoesLevel")));
        pokemon.getIVs().setStat(StatsType.HP, Integer.parseInt(map.get("IVs.hp")));
        pokemon.getIVs().setStat(StatsType.Attack, Integer.parseInt(map.get("IVs.attack")));
        pokemon.getIVs().setStat(StatsType.SpecialAttack, Integer.parseInt(map.get("IVs.defence")));
        pokemon.getIVs().setStat(StatsType.Defence, Integer.parseInt(map.get("IVs.specialAttack")));
        pokemon.getIVs().setStat(StatsType.SpecialDefence, Integer.parseInt(map.get("IVs.speed")));
        pokemon.getIVs().setStat(StatsType.Speed, Integer.parseInt(map.get("IVs.specialDefence")));
        pokemon.getEVs().setStat(StatsType.HP, Integer.parseInt(map.get("EVs.hp")));
        pokemon.getEVs().setStat(StatsType.Attack, Integer.parseInt(map.get("EVs.attack")));
        pokemon.getEVs().setStat(StatsType.SpecialAttack, Integer.parseInt(map.get("EVs.defence")));
        pokemon.getEVs().setStat(StatsType.Defence, Integer.parseInt(map.get("EVs.specialAttack")));
        pokemon.getEVs().setStat(StatsType.SpecialDefence, Integer.parseInt(map.get("EVs.speed")));
        pokemon.getEVs().setStat(StatsType.Speed, Integer.parseInt(map.get("EVs.specialDefence")));
        pokemon.setAbility(map.get("Ability"));
        pokemon.setAbilitySlot(Integer.parseInt(map.get("AbilitySlot")));
        Moveset moveset = new Moveset();
        for (int i = 0;i < 4;i++){
            String attackName = map.get("Moveset."+i);
            if (attackName != null){
                Attack attack = new Attack(AttackBase.getAttackBaseFromEnglishName(attackName).get());
                moveset.add(attack);
            }
        }
        pokemon.setMoveset(moveset);
        pokemon.setCaughtBall(EnumPokeballs.getPokeballFromString(map.get("CaughtBall")));
        pokemon.setHealth(Integer.parseInt(map.get("Health")));
        pokemon.setShiny(Boolean.parseBoolean(map.get("IsShiny")));
        pokemon.setGender(Gender.getGender(map.get("Gender")));
        pokemon.setForm(Integer.parseInt(map.get("Form")));

        if (pcs != null){
            StoragePosition position = new StoragePosition(Integer.parseInt(map.get("Position.box")),Integer.parseInt(map.get("Position.order")));
            pcs.set(position,pokemon);
        }
        return pokemon;
    }
}
