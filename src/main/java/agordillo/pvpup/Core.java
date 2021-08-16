package agordillo.pvpup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin {

	@Override
	public void onDisable() {
		getLogger().info("PvpUp desactivado!");
		super.onDisable();
	}

	@Override
	public void onEnable() {
		Manager.registerManager();
		getServer().getPluginManager().registerEvents(new Listeners(this), this);
		getLogger().info("PvpUp activado!");
		super.onEnable();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			String comando = command.getName();
			Player player = (Player) sender;
			if (comando.equals("pvpUp") || label.equals("pu")) {
				ConfigurationSection section;
				YamlConfiguration config = Manager.getFileConfig();
				Location location = player.getLocation();
				switch (args[0]) {
				case "setSpawn":
					section = config.createSection("Spawn");
					setLocation(location, section);
					Manager.SaveConfig(config);
					break;
				case "setDeathSpawn":
					section = config.createSection("DeathSpawn_"+location.getWorld().getName());
					setLocation(location, section);
					Manager.SaveConfig(config);
					break;
				case "join":
					if(Manager.join(player)) {
						Location loc = Manager.getLocation(this, config.getConfigurationSection("Spawn"));
						player.teleport(loc);
						player.sendMessage("Has entrado al modo pvpUP");
					}else {
						player.sendMessage("ya estas dentro del modo pvpUP");
					}
					break;
				case "leave":
					Manager.leave(player);
				}
				return true;
			}
		} else {
			sender.sendMessage("Usted debe ser un jugador!");
		}
		return false;
	}

	private void setLocation(Location location, ConfigurationSection section) {
		section.set("x", location.getX());
		section.set("y", location.getY());
		section.set("z", location.getZ());
		section.set("pitch", location.getPitch());
		section.set("world", location.getWorld().getName());
		section.set("yaw", location.getYaw());
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		String comando = command.getName();
		if (comando.equals("pvpUp") || alias.equals("pu")) {
			List<String> arguments = new LinkedList<>(Arrays.asList("join","leave","setSpawn","setDeathSpawn"));
			if (args.length == 1) {
                if (!args[0].equals("")) {
                	arguments.removeIf(a -> !a.toLowerCase().startsWith(args[0].toLowerCase()));
                } 
                Collections.sort(arguments);
			}
			return arguments;
		}
		return new ArrayList<>();
	}
	
	

}
