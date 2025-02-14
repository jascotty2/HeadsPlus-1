package io.github.thatsmusic99.headsplus.config.customheads;

import com.google.common.io.Files;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.thatsmusic99.headsplus.HeadsPlus;
import io.github.thatsmusic99.headsplus.commands.maincommand.DebugPrint;
import io.github.thatsmusic99.headsplus.config.ConfigSettings;
import io.github.thatsmusic99.headsplus.nms.NMSManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class HeadsPlusConfigCustomHeads extends ConfigSettings {

    public boolean s = false;
    private final double cVersion = 2.6;
    public final Map<String, List<String>> sections = new HashMap<>();
    public final Map<String, ItemStack> headsCache = new HashMap<>();
    public final Set<String> allHeadsCache = new HashSet<>();

    public HeadsPlusConfigCustomHeads() {
        this.conName = "customheads";
        headsxEnable();
    }

    private void loadHeadsX() {
        getConfig().options().header("HeadsPlus by Thatsmusic99 "
                + "\n WARNING: This is an advanced section of the plugin. If you do not know what you a doing with it, please do not use it due to risk of crashing your own and other's games. "
                + "\n For more information visit the GitHub wiki for HeadsX.yml: https://github.com/Thatsmusic99/HeadsPlus/wiki/customheads.yml");

        for (HeadsXSections h : HeadsXSections.values()) {
            getConfig().addDefault("sections." + h.let + ".display-name", h.dn);
            getConfig().addDefault("sections." + h.let + ".texture", h.tx);
        }
        for (HeadsXEnums e : HeadsXEnums.values()) {
            // getConfig().addDefault("heads." + e.name + ".database", true);
            getConfig().addDefault("heads." + e.name + ".encode", false);
            getConfig().addDefault("heads." + e.name + ".displayname", e.dn);
            getConfig().addDefault("heads." + e.name + ".texture", e.tex);
            getConfig().addDefault("heads." + e.name + ".price", "default");
            getConfig().addDefault("heads." + e.name + ".section", e.sec);
        }

        getConfig().options().copyDefaults(true);
        save();
        initCategories();
    }

    @Override
    public void reloadC(boolean a) {
        if (configF == null) {
            File oldFile = new File(HeadsPlus.getInstance().getDataFolder(), "headsx.yml");
            File newFile = new File(HeadsPlus.getInstance().getDataFolder(), "customheads.yml");
            if (oldFile.exists()) {
                try {
                    Files.copy(oldFile, newFile);
                    oldFile.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                configF = newFile;
            } else {
                configF = newFile;
            }
        }
        config = YamlConfiguration.loadConfiguration(configF);
        getConfig().addDefault("options.update-heads", true);
        getConfig().addDefault("options.version", cVersion);
        getConfig().addDefault("options.default-price", 10.00);
        getConfig().addDefault("options.price-per-world.example-one", 15.00);
        getConfig().options().copyDefaults(true);
     //   getConfig().addDefault("options.advent-calendar", true);
        //    getConfig().addDefault("options.advent-texture", "HP#snowman");
        //    getConfig().addDefault("options.advent-display-name", "&4[&a&lHeadsPlus &c&lAdvent Calendar!&2]");
        //    getConfig().addDefault("options.christmas-hype", 0);
        //    if (getConfig().getBoolean("options.advent-calendar")) {
        //        for (AdventCManager acm : AdventCManager.values()) {
        //            getConfig().addDefault("advent-18." + acm.name(), new ArrayList<>());
        //        }
        //    }
        if (configF.length() <= 500) {
            loadHeadsX();
        }
        boolean b = getConfig().getBoolean("options.update-heads");
        if (getConfig().getDouble("options.version") < cVersion && b) {
            for (String str : getConfig().getConfigurationSection("heads").getKeys(false)) {
                getConfig().addDefault("heads." + str + ".price", "default");
            }
            getConfig().set("options.version", cVersion);
            for (String str : getConfig().getConfigurationSection("heads").getKeys(false)) {
                for (HeadsXEnums e : HeadsXEnums.values()) {
                    if (e.name.equalsIgnoreCase(str)) {
                        getConfig().addDefault("heads." + e.name + ".section", e.sec);
                    }
                }
            }
            for (HeadsXSections h : HeadsXSections.values()) {
                if (h.d == cVersion) {
                    getConfig().addDefault("sections." + h.let + ".display-name", h.dn);
                    getConfig().addDefault("sections." + h.let + ".texture", h.tx);
                }
            }
            for (HeadsXEnums e : HeadsXEnums.values()) {
                if (e.v == cVersion) {
                    //getConfig().addDefault("heads." + e.name + ".database", true); // isn't actually a required field
                    getConfig().addDefault("heads." + e.name + ".encode", false);
                    getConfig().addDefault("heads." + e.name + ".displayname", e.dn);
                    getConfig().addDefault("heads." + e.name + ".texture", e.tex);
                    getConfig().addDefault("heads." + e.name + ".price", "default");
                    getConfig().addDefault("heads." + e.name + ".section", e.sec);
                }
            }

            getConfig().options().copyDefaults(true);
        }
        save();
        initCategories();
        s = false;
    }

    private void headsxEnable() {
        reloadC(false);
       // if (s) {
        //      loadHeadsX();
        //  }
        s = false;
    }

    private void initCategories() {
        sections.clear();
        for (String cat : getConfig().getConfigurationSection("sections").getKeys(false)) {
            sections.put(cat, new ArrayList<>());
        }
        ConfigurationSection heads = getConfig().getConfigurationSection("heads");
        try {
            for (String head : heads.getKeys(false)) {
                allHeadsCache.add(heads.getString(head + ".texture"));
                if (heads.getBoolean(head + ".database", true)) {
                    final String sec = heads.getString(head + ".section");
                    List<String> list = sections.get(sec);
                    if (list != null) {
                        list.add(head);
                        headsCache.put(head, getSkull(head));
                    }
                }
            }
        } catch (RuntimeException ex) {
            HeadsPlus.getInstance().getLogger().log(Level.SEVERE, "Failed to init skull database", ex);
            sections.clear();
            return;
        }
        if (getConfig().getBoolean("options.advent-calendar")) {
            sections.put("advent-calendar", new ArrayList<>());
        }
    }

    public boolean isAdvent() {
        return getConfig().getBoolean("options.advent-calendar", false) && (new GregorianCalendar()).get(GregorianCalendar.MONTH) == GregorianCalendar.DECEMBER;
    }

    public boolean isHPXSkull(String str) {
        return str.startsWith("HP#");
    }

    public ItemStack getSkull(String s) {
        final String key = s.contains("#") ? s.split("#")[1] : s;
        ItemStack is = headsCache.get(s);
        // todo? allow loading texture directly from parameter if matches base64 pattern?
        return is != null ? is.clone() : getSkullFromTexture(
                getConfig().getString("heads." + key + ".texture"),
                getConfig().getBoolean("heads." + key + ".encode"),
                getConfig().getString("heads." + key + ".displayname"));
    }

    public ItemStack getSkullFromTexture(String texture, boolean encoded, String displayName) {
        NMSManager nms = HeadsPlus.getInstance().getNMS();
        ItemStack i = nms.getSkullMaterial(1);
        SkullMeta sm = (SkullMeta) i.getItemMeta();
        GameProfile gm;
        if (encoded) {
            gm = new GameProfile(UUID.nameUUIDFromBytes(texture.getBytes()), "HPXHead");
            byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"http://textures.minecraft.net/texture/%s\"}}}", texture).getBytes());
            gm.getProperties().put("textures", new Property("textures", new String(encodedData)));
        } else {
            gm = new GameProfile(UUID.nameUUIDFromBytes(texture.getBytes()), "HPXHead");
            gm.getProperties().put("textures", new Property("texture", texture.replaceAll("=", "")));
        }

        try {
            Field profileField;
            profileField = sm.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(sm, gm);
            if (displayName != null) {
                sm.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
            }
            i.setItemMeta(sm);
            return i;
        } catch (NoSuchFieldException | IllegalAccessException | SecurityException ex) {
            throw new RuntimeException("Reflection error while setting head texture", ex);
        }
    }

    public String getTextures(String s) {
        String[] st = s.split("#");
        try {
            return getConfig().getString("heads." + st[1] + ".texture");
        } catch (Exception ex) {
            DebugPrint.createReport(ex, "Startup (customheads.yml)", false, null);
            return "";
        }
    }

    public ItemStack setTexture(String tex, ItemStack is) throws IllegalAccessException, NoSuchFieldException {
        SkullMeta sm = (SkullMeta) is.getItemMeta();
        GameProfile gm = new GameProfile(UUID.nameUUIDFromBytes(tex.getBytes()), "HPXHead");
        gm.getProperties().put("textures", new Property("texture", tex.replaceAll("=", "")));

        Field profileField;
        profileField = sm.getClass().getDeclaredField("profile");

        profileField.setAccessible(true);
        profileField.set(sm, gm);
        is.setItemMeta(sm);
        return is;
    }

    public void addChristmasHype() {
        int hype = getConfig().getInt("options.christmas-hype");
        ++hype;
        getConfig().set("options.christmas-hype", hype);
        save();
    }

    public void grabProfile(String id) {
        grabProfile(id, null, false);
    }

    // texture lookups need to be protected from spam
    HashMap<String, Long> lookups = new HashMap<>();

    public boolean grabProfile(String id, CommandSender callback, boolean forceAdd) {
        Long last = lookups.get(id);
        long now = System.currentTimeMillis();
        if(last != null && last > now - 180000) {
            if(callback != null) {
                callback.sendMessage(ChatColor.RED + "/addhead spam protection - try again in a few minutes");
            }
            return false;
        } else {
            lookups.put(id, now);
        }
        grabProfile(id, 3, callback, forceAdd, forceAdd ? 5 : 20 * 20);
        return true;
    }

    public String grabUUID(String username, int tries, CommandSender callback) {
        String uuid = null;
        BufferedReader reader = null;
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream(), "UTF8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if(sb.length() == 0) {
                    sb.append("\n");
                }
                sb.append(line);
            }
            String json = sb.toString();
            JSONObject resp = (JSONObject) JSONValue.parse(json);
            if(resp == null || resp.isEmpty()) {
                HeadsPlus.getInstance().getLogger().warning("Failed to grab data for user " + username + " - invalid username.");
                if(callback != null) {
                    callback.sendMessage(ChatColor.RED + "Error: Failed to grab data for user " + username + "!");
                }
                return null;
            } else if(resp.containsKey("error")) {
                // Retry
                if(tries > 0) {
                    grabUUID(username, tries - 1, callback);
                } else if(callback != null) {
                    callback.sendMessage(ChatColor.RED + "Error: Failed to grab data for user " + username + "!");
                }
                return null;
            } else {
                uuid = String.valueOf(resp.get("id")); // Trying to parse this as a UUID will cause an IllegalArgumentException
            }
        } catch (IOException e) {
            DebugPrint.createReport(e, "Retreiving UUID (addhead)", true, callback);
        }
        return uuid;
    }

    protected void grabProfile(String id, int tries, CommandSender callback, boolean forceAdd, int delay) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(HeadsPlus.getInstance(), () -> {
                    BufferedReader reader = null;
            try {
                if (id == null) return;
                URL uRL = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + id.replace("-", ""));

                reader = new BufferedReader(new InputStreamReader(uRL.openConnection().getInputStream(), "UTF8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if(sb.length() == 0) {
                        sb.append("\n");
                    }
                    sb.append(line);
                }
                String json = sb.toString();

                JSONObject resp = (JSONObject) JSONValue.parse(json);
                if(resp == null || resp.isEmpty()) {
                    HeadsPlus.getInstance().getLogger().warning("Failed to grab data for user " + id + " - invalid id");
                    if(callback != null) {
                        callback.sendMessage(ChatColor.RED + "Error: Failed to grab data for user " + Bukkit.getOfflinePlayer(id).getName());
                    }
                    return;
                } else if(resp.containsKey("error")) {
                    // retry
                    if(tries > 0) {
                        grabProfile(id, tries - 1, callback, forceAdd, 30 * 20);
                    } else if(callback != null) {
                        callback.sendMessage(ChatColor.RED + "Error: Failed to grab data for user " + Bukkit.getOfflinePlayer(id).getName());
                    }
                    return;
                }

                Object o = resp.get("properties");
                if(o instanceof List) {
                    for(Object o2 : (List) o) {
                        if(o2 instanceof Map) {
                           Map m = (Map) o2;
                           if("textures".equals(m.get("name")) && m.containsKey("value")) {
                               String encoded = m.get("value").toString();
                               String decoded = new String(Base64.getDecoder().decode(encoded));
                               JSONObject resp2 = (JSONObject) JSONValue.parse(decoded);
                               if((o2 = resp2.get("textures")) instanceof Map
                                       && (o2 = ((Map) o2).get("SKIN")) instanceof Map
                                       && ((Map) o2).containsKey("url")) {
                                   String texUrl = ((Map) o2).get("url").toString();
                                   int last = texUrl.lastIndexOf('/');
                                   if(last != -1) {
                                       texUrl = texUrl.substring(last + 1);
                                       String name = resp.get("name").toString();
                                       if(!allHeadsCache.contains(texUrl)) {
                                           addHead(texUrl, true,
                                                   HeadsPlus.getInstance().getConfig().getString("plugin.autograb.title").replace("{player}", name),
                                                   HeadsPlus.getInstance().getConfig().getString("plugin.autograb.section"), 
                                                   HeadsPlus.getInstance().getConfig().getString("plugin.autograb.price"), 
                                                   forceAdd || HeadsPlus.getInstance().getConfig().getBoolean("plugin.autograb.add-as-enabled"));
                                            if(callback != null) {
                                                callback.sendMessage(HeadsPlus.getInstance().getMessagesConfig().getString("head-added")
                                                        .replace("{player}", name)
                                                        .replace("{header}", HeadsPlus.getInstance().getMenus().getConfig().getString("profile.header")));
                                            }
                                       } else if (forceAdd && enableHead(texUrl)){
                                           if(callback != null) {
                                                callback.sendMessage(HeadsPlus.getInstance().getMessagesConfig().getString("head-added")
                                                        .replace("{player}", name)
                                                        .replace("{header}", HeadsPlus.getInstance().getMenus().getConfig().getString("profile.header")));
                                            }
                                       } else if(callback != null) {
                                           callback.sendMessage(HeadsPlus.getInstance().getMessagesConfig().getString("head-already-added")
                                                    .replace("{player}", name)
                                                    .replace("{header}", HeadsPlus.getInstance().getMenus().getConfig().getString("profile.header")));
                                       }
                                   }
                               }
                           }
                        }
                    }
                }
            } catch (Exception ex) {
                if(ex instanceof IOException && ex.getMessage().contains("Server returned HTTP response code: 429 for URL")) {
                    grabProfile(id, tries - 1, callback, forceAdd, 30 * 20);
                } else {
                    DebugPrint.createReport(ex, "Retreiving profile (addhead)", true, callback);
                }
            } finally {
                if(reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }, delay);
    }

    /**
     * Enable player head texture, if not enabled already
     * @param texture
     * @return true if the texture exists and was not previously enabled.
     */
    public boolean enableHead(String texture) {
        ConfigurationSection heads = getConfig().getConfigurationSection("heads");
        for(String k : heads.getKeys(false)) {
            if(texture.equals(heads.getString(k + ".texture"))) {
                if(!heads.getBoolean(k + ".database", true)) {
                    heads.set(k + ".database", true);
                    List<String> list = sections.get(heads.getString(k + ".section"));
                    if (list != null) {
                        list.add(k);
                        headsCache.put(k, getSkull(k));
                    }
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    public void addHead(String texture, boolean encode, String displayname, String section, String price, boolean enable) {
        // find a name that's open
        List l = sections.get(section);
        int i = l == null ? 0 : l.size() + 1;
        ConfigurationSection heads = getConfig().getConfigurationSection("heads");
        String key;
        while(heads.contains(key = section + "_" + i)) ++i;
        ConfigurationSection head = heads.createSection(key);
        if(!enable) {
            head.set("database", enable);
        }
        head.set("encode", encode);
        if(price.matches("^([0-9]*\\.)?[0-9]+$")) {
            head.set("price", Double.parseDouble(price));
        } else {
            head.set("price", price);
        }
        head.set("section", section);
        head.set("texture", texture);
        head.set("displayname", displayname);
        if(enable) {
            List<String> list = sections.get(section);
            if (list != null) {
                list.add(key);
                headsCache.put(key, getSkull(key));
            }
        }
		allHeadsCache.add(texture);
        delaySave();
    }

    int autosaveTask = -1;

    void delaySave() {
        if (autosaveTask == -1 && HeadsPlus.getInstance().isEnabled()) {
            autosaveTask = Bukkit.getScheduler().runTaskLaterAsynchronously(HeadsPlus.getInstance(), ()->{
                save();
                autosaveTask = -1;
            }, 5 * 60).getTaskId();
        }
    }

    public void flushSave() {
        if (autosaveTask != -1) {
            Bukkit.getScheduler().cancelTask(autosaveTask);
            save();
            autosaveTask = -1;
        }
    }
}
