package agordillo.pvpup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Manager {
	private static Manager manager;
	private static Map<Player,Location> players;
	private static Map<Player,Integer> levels; 
	private static Map<Player,Player> lastTouch;
	private File dir;
	private static File stats;
	private static File configFile;
	private static ItemStack returnLobby;
	public static final String STAT_KILLS = "Kills";
	public static final String STAT_DEATH = "Death";
	public static final String STAT_MAX_LEVEL = "MaxLevel";
	public static final String STAT_MONEY = "Money";
	
	private Manager() {
		dir = new File("plugins/pvpUP");
		stats = new File("plugins/pvpUP/usersdata.yml");
		configFile = new File("plugins/pvpUP/config.yml");
		if(!dir.exists()) {
			dir.mkdir();
		}
		try {
			if(!stats.exists()) {
				stats.createNewFile();
				crearEstadisticas();
			}
			if(!configFile.exists()) {
				configFile.createNewFile();
				initConfig();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		lastTouch = new HashMap<>();
		players = new HashMap<>();
		levels = new HashMap<>();
		initItems();
	}
	
	private void initConfig() {
		YamlConfiguration config = getFileConfig();
		config.createSection("BlockCommands").set("all",true);
		config.set("whiteList",false);
		config.set("MoneyByKill",50);
		SaveConfig(config);
	}

	private void initItems() {
		returnLobby = new ItemStack(Material.IRON_DOOR);
		ItemMeta meta = returnLobby.getItemMeta();
		meta.setDisplayName("Lobby");
		returnLobby.setItemMeta(meta);
	}

	public static Manager registerManager() {
		if(manager == null) {
			manager = new Manager();
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
	
	private static void crearEstadisticas() {
		YamlConfiguration config = getFileStats();
		config.createSection(STAT_KILLS);
		config.createSection(STAT_DEATH);
		config.createSection(STAT_MAX_LEVEL);
		config.createSection(STAT_MONEY);
		saveStats(config);
	}

	public static void joinArena(Player jugador) {
		jugador.getInventory().clear();
		levels.put(jugador,jugador.getLevel());
		jugador.setLevel(0);
		jugador.getInventory().setItem(8, returnLobby);
	}
	
	private static void itemsConfiguracion(Player jugador) {
		jugador.getInventory().clear();
	}

	public static YamlConfiguration getFileConfig() {
		return YamlConfiguration.loadConfiguration(configFile);
	}
	
	public static YamlConfiguration getFileStats() {
		return YamlConfiguration.loadConfiguration(stats);
	}
	
	public static void SaveConfig(YamlConfiguration config) {
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveStats(YamlConfiguration fstats) {
		try {
			fstats.save(stats);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Location leave(Player jugador) {
		if(players.containsKey(jugador)) {
			Location anterior = players.get(jugador);
			jugador.giveExpLevels(levels.get(jugador));
			players.remove(jugador);
			return anterior;
		}else {
			return null;
		}
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
	
	
}