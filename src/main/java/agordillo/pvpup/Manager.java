package agordillo.pvpup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class Manager {
	private static Manager manager;
	private static Map<Player,Location> players;
	private static Map<Player,Integer> levels; 
	private static Map<Player,Player> lastTouch;
	private File dir;
	private static File stats;
	private static File configFile;
	private static ItemStack[] itemsArena;
	public static final String STAT_KILLS = "Kills";
	public static final String STAT_DEATH = "Death";
	public static final String STAT_MAX_LEVEL = "MaxLevel";
	public static final String STAT_MONEY = "Money";
	private static Plugin plugin;
	private static YamlConfiguration yamlConfig;
	private static YamlConfiguration yamlStats;
	
	private Manager(Plugin plugin) {
		dir = new File("plugins/pvpUP");
		stats = new File("plugins/pvpUP/usersdata.yml");
		configFile = new File("plugins/pvpUP/config.yml");
		lastTouch = new HashMap<>();
		players = new HashMap<>();
		levels = new HashMap<>();
		Manager.plugin = plugin;
		initFiles();
		initItems();
		
	}
	
	private void initFiles() {
		try {
			checkFile(dir,true);
			boolean estats = checkFile(stats,false);
			boolean sconfig = checkFile(configFile,false);
			loadConfig();
			if(estats) {
				initStats();
			}
			if(sconfig) {
				initConfig();
			}
		}catch(Exception e) {
			plugin.getLogger().log(Level.SEVERE,e.getMessage());
		}
		
	}
	
	private void initItems() {
		itemsArena = new ItemStack[2];
		ItemStack returnLobby = new ItemStack(Material.MAP);
		ItemMeta meta = returnLobby.getItemMeta();
		meta.setDisplayName("Lobby");
		returnLobby.setItemMeta(meta);
		itemsArena[0] = returnLobby;
	}
	
	private void loadConfig() {
		yamlStats = YamlConfiguration.loadConfiguration(stats);
		yamlConfig = YamlConfiguration.loadConfiguration(configFile);
	}
	
	private boolean checkFile(File file,boolean dir) throws IOException {
		return dir?file.mkdir():file.createNewFile();
	}
	
	

	private void initConfig() {
		yamlConfig.createSection("BlockCommands").set("all",true);
		yamlConfig.set("whiteList",false);
		yamlConfig.set("MoneyByKill",50);
		ConfigurationSection section = yamlConfig.createSection("LevelLossRange");
		section.set("1-10",1);
		section.set("11-20",2);
		section.set("21-50",4);
		section.set("51-~",8);
		SaveConfig();
	}

	private static void initStats() {
		yamlStats.createSection(STAT_KILLS);
		yamlStats.createSection(STAT_DEATH);
		yamlStats.createSection(STAT_MAX_LEVEL);
		yamlStats.createSection(STAT_MONEY);
		saveStats();
	}
	
	public static Manager registerManager(Plugin plugin) {
		if(manager == null) {
			manager = new Manager(plugin);
		}
		return manager;
	}
	
	public static boolean join(Player jugador) {
		if(!players.containsKey(jugador)) {
			players.put(jugador,jugador.getLocation());
			itemsConfiguracion(jugador);
			return true;
		}else {
			return false;
		}
	}

	public static void joinArena(Player jugador) {
		jugador.getInventory().clear();
		levels.put(jugador,jugador.getLevel());
		jugador.getInventory().setItem(8, itemsArena[0]);
	}
	
	private static void itemsConfiguracion(Player jugador) {
		jugador.getInventory().clear();
		ItemStack selector = new ItemStack(Material.MAP);
		ItemMeta meta = selector.getItemMeta();
		meta.setDisplayName("Mapa random");
		selector.setItemMeta(meta);
		jugador.getInventory().addItem(selector);
	}

	public static YamlConfiguration getFileConfig() {
		return yamlConfig;
	}
	
	public static YamlConfiguration getFileStats() {
		return yamlStats;
	}
	
	public static void SaveConfig() {
		try {
			yamlConfig.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveStats() {
		try {
			yamlStats.save(stats);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void leave(Player jugador) {
		if(players.containsKey(jugador)) {
			Location anterior = players.get(jugador);
			jugador.giveExpLevels(levels.get(jugador));
			players.remove(jugador);
			jugador.teleport(anterior);
			jugador.sendMessage("Has salido del modo pvpUP");
		}else {
			jugador.sendMessage("No estas dentro del modo pvpUP");
		}
	}
	
	public static void leaveArena(Player jugador) {
		jugador.getInventory().removeItem(itemsArena);
		ConfigurationSection section = yamlConfig.getConfigurationSection("Spawn");
		Location loc = getLocation(section);
		jugador.teleport(loc);
	}
	
	//TODO:revisar para incluir synchronized 
	public static Map<Player, Location> getPlayers(){
		return players;
	}
	
	public static Player lastTouch(Player death) {
		return lastTouch.get(death);
	}
	
	/*
	 * @param touch el jugador que ha tocado por ultima vez
	 * @param receibed el jugador que ha recibido el daño por ultima vez
	*/
	public static void setLastTouch(Player touch,Player receibed) {
		lastTouch.put(receibed, touch);
	}
	
	public static void resetTouch(Player objetive) {
		lastTouch.remove(objetive);
	}
	
	public static int getLevels(Player jugador) {
		return levels.get(jugador);
	}
	
	
	public static Location getLocation(ConfigurationSection section) {
		World mundo  = plugin.getServer().getWorld(section.getString("world"));
		return new Location(
				mundo,
				section.getDouble("x"),
				section.getDouble("y"),
				section.getDouble("z"),
				Float.parseFloat(section.get("yaw").toString()),
				Float.parseFloat(section.get("pitch").toString()));
	}
	
}