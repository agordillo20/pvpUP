package agordillo.pvpup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.math.Stats;

public class Listeners implements Listener {
	private JavaPlugin plugin;
	
	public Listeners(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void CambioNvl(PlayerLevelChangeEvent event) {
		Player jugador = event.getPlayer();
		World mundo = plugin.getServer().getWorld(Manager.getFileConfig().getConfigurationSection("DeathSpawn_"+jugador.getWorld().getName()).getString("world"));
		if (Manager.getFileConfig().contains("DeathSpawn_"+jugador.getWorld().getName()) && 
				mundo.equals(jugador.getWorld())) {
			int nivel = event.getNewLevel();
			ItemStack[] kit = Kits.getKit(String.valueOf(nivel));
			Kits.putKit(kit, jugador);
			int diferencia = nivel - event.getOldLevel();
			if (diferencia > 0) {
				jugador.sendMessage("Has subido " + diferencia + " nivel");
			} else {
				diferencia *= -1;
				jugador.sendMessage("Has perdido "+((diferencia == 1)?"1 nivel":diferencia + " niveles"));
			}
		}
	}
	
	@EventHandler
	public void escucharComandos(PlayerCommandPreprocessEvent event) {
		String comando = event.getMessage();
		YamlConfiguration config = Manager.getFileConfig();
		ConfigurationSection bloqued = config.getConfigurationSection("BlockCommands");
		if(bloqued.contains("all")){
			event.setCancelled(config.getBoolean("whiteList"));
		}else {
			for(String key:bloqued.getKeys(false)) {
				if(comando.equalsIgnoreCase(key)) {
					event.setCancelled(config.getBoolean("whiteList"));
				}
			}
		}
	}

	private void kills(Player killer,Player killed) {
		nivel(killer, killed);
		killer.setHealth(killer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
		killed.setHealth(killer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
		FileConfiguration config = Manager.getFileConfig();
		ConfigurationSection section = config.getConfigurationSection("DeathSpawn_"+killer.getWorld().getName());
		World mundo = plugin.getServer().getWorld(section.getString("world"));
		Location loc = new Location(
			mundo, 
			section.getDouble("x"), 
			section.getDouble("y"),
			section.getDouble("z"),
			Float.parseFloat(section.get("yaw").toString()),
			Float.parseFloat(section.get("pitch").toString())
		);
		killed.teleport(loc);
		setStats(killer,killed);
	}
	
	private void setStats(Player killer, Player killed){
		String idKilled = killed.getUniqueId().toString();
		stats(idKilled,Manager.STAT_DEATH);
		if(killer!=null) {
			String idKiller = killer.getUniqueId().toString();
			stats(idKiller,Manager.STAT_KILLS);
			ConfigurationSection money = Manager.getFileStats().getConfigurationSection(Manager.STAT_MONEY);
			int totalm = Manager.getFileConfig().getInt("MoneyByKill");
			if(money.contains(idKiller)) {
				totalm += money.getInt(idKiller);
			}
			money.set(idKiller, totalm);
			ConfigurationSection level = Manager.getFileStats().getConfigurationSection(Manager.STAT_MAX_LEVEL);
			if(level.contains(idKiller)) {
				int maxL = level.getInt(idKiller);
				if(maxL<killer.getLevel()) {
					level.set(idKiller, killer.getLevel());
				}
			}else {
				level.set(idKiller,killer.getLevel());
			}
		}
		Manager.saveStats();
	}

	private void stats(String UidPlayer,String tipo) {
		ConfigurationSection section = Manager.getFileStats().getConfigurationSection(tipo);
		int total = 1;
		if(section.contains(UidPlayer)) {
			total += section.getInt(UidPlayer);
		}
		section.set(UidPlayer, total);
	}

	@EventHandler
	public void ataqueJugador(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			Player jugador = (Player)event.getEntity();
			Player atacante = (Player)event.getDamager();
			if(Manager.getPlayers().containsKey(atacante) && Manager.getPlayers().containsKey(jugador)) {
				if(Manager.getFileConfig().contains("DeathSpawn_"+jugador.getWorld().getName())){
					Manager.setLastTouch(atacante,jugador);
					if(evaluacionVida(jugador,event.getDamage())) {
						event.setCancelled(true);
						kills(atacante,jugador);
					}
				}
			}
			
		}
	}
	
	private boolean evaluacionVida(Player jugador, double d) {
		return jugador.getHealth()-d<=0;
	}

	@EventHandler
	public void observarUtilizacionItems(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if(event.getAction().equals(Action.RIGHT_CLICK_AIR) && Manager.getPlayers().containsKey(player)) {
			//arenas
			if(Manager.getFileConfig().contains("DeathSpawn_"+player.getWorld().getName())) {
					switch(player.getInventory().getItemInMainHand().getType()){
					case MAP:
						Manager.leaveArena(player);
						break;
					default:
						plugin.getLogger().info("item no controlado ->"+player.getInventory().getItemInMainHand().getType());
						break;
					}
			//spawn
			}else if(Manager.getFileConfig().getConfigurationSection("Spawn").getString("world").equals(player.getWorld().getName())) {
				switch(player.getInventory().getItemInMainHand().getType()){
				case MAP:
					List<String> mapas = new ArrayList<>();
					for(String key:Manager.getFileConfig().getKeys(false)) {
						if(key.startsWith("DeathSpawn_")) {
							mapas.add(key);
						}
					}
					String mapa = mapas.get(new Random().nextInt(mapas.size()));
					Location loc = Manager.getLocation(Manager.getFileConfig().getConfigurationSection(mapa));
					player.teleport(loc);
					break;
				
				default:
					plugin.getLogger().info("item no controlado ->"+player.getInventory().getItemInMainHand().getType());
					break;
			}
		}
		}
	}
	
	@EventHandler
	public void impedirVerMapa(PlayerItemHeldEvent event) {
		if(event.getPlayer().getInventory().getItem(event.getNewSlot()).getType().equals(Material.MAP)) {
			event.setCancelled(true);
		}
		
	}
	
	@EventHandler
	public void observarCaidaAgua(PlayerMoveEvent event) {
		Player jugador = event.getPlayer();
		Material material = jugador.getLocation().getBlock().getType();
		if(Manager.getFileConfig().contains("DeathSpawn_"+jugador.getWorld().getName())) {
			 if (material == Material.STATIONARY_WATER || material == Material.WATER) {
				 ConfigurationSection section = Manager.getFileConfig().getConfigurationSection("DeathSpawn_"+jugador.getWorld().getName());
				 Location loc = Manager.getLocation(section);
				 jugador.teleport(loc);
				 jugador.setHealth(jugador.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
				 jugador.giveExpLevels(-1);
				 Player killer = Manager.lastTouch(jugador);
				 if(killer!=null) {
					 killer.giveExpLevels(1);
					 Manager.resetTouch(jugador);
				 }
				setStats(killer, killer);
			 }
		}
		
	}
	
	@EventHandler
	public void entradaJugadorSpawn(PlayerChangedWorldEvent event) {
		Player jugador = event.getPlayer();
		World mundo = plugin.getServer().getWorld(Manager.getFileConfig().getConfigurationSection("Spawn").getString("world"));
		if(jugador.getWorld().equals(mundo)) {
			Manager.join(event.getPlayer());
		}else if(event.getFrom().equals(mundo) && Manager.getFileConfig().contains("DeathSpawn_"+jugador.getWorld().getName())) {
			Manager.joinArena(jugador);
			jugador.setLevel(0);
			Kits.putKit(Kits.getKit("0"),jugador);
		}
	}

	private void nivel(Player killer, Player killed) {
		if (killer != null) {
			killer.giveExpLevels(1);
		}
		int xpKilled = killed.getLevel();
		if (xpKilled > 0) {
			ConfigurationSection section = Manager.getFileConfig().getConfigurationSection("LevelLossRange");
			for(String key:section.getKeys(false)) {
				String[] split = key.split("-");
				String min = split[0];
				String max = split[1];
				int minimo = Integer.valueOf(min);
				if(!max.equals("~")) {
					int maximo = Integer.valueOf(max);
					if(xpKilled>=minimo && xpKilled<=maximo) {
						killed.giveExpLevels(((section.getInt(key))*(-1)));
						break;
					}
				}else {
					if(xpKilled>=minimo) {
						killed.giveExpLevels(((section.getInt(key))*(-1)));
						break;
					}
				}
			}
		}else if(xpKilled==0){
			Kits.putKit(Kits.getKit("0"),killed);
		}
	}

}
