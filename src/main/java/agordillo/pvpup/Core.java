package agordillo.pvpup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.World;
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
				YamlConfiguration config = Manager.getFileConfig();
				Location location = player.getLocation();
				switch (args[0]) {
				case "setSpawn":
					ConfigurationSection section = config.createSection("Spawn");
					section.set("x", location.getX());
					section.set("y", location.getY());
					section.set("z", location.getZ());
					section.set("pitch", location.getPitch());
					section.set("world", location.getWorld().getName());
					section.set("yaw", location.getYaw());
					Manager.SaveConfig(config);
					break;
				case "setDeathSpawn":
					ConfigurationSection section1 = config.createSection("DeathSpawn_"+location.getWorld().getName());
					section1.set("x", location.getX());
					section1.set("y", location.getY());
					section1.set("z", location.getZ());
					section1.set("pitch", location.getPitch());
					section1.set("world", location.getWorld().getName());
					section1.set("yaw", location.getYaw());
					Manager.SaveConfig(config);
					break;
				case "join":
					if(Manager.join(player)) {
						ConfigurationSection section2 = config.getConfigurationSection("Spawn");
						World mundo  = getServer().getWorld(section2.getString("world"));
						Location loc = new Location(
								mundo,
								section2.getDouble("x"),
								section2.getDouble("y"),
								section2.getDouble("z"),
								Float.parseFloat(section2.get("yaw").toString()),
								Float.parseFloat(section2.get("pitch").toString()));
						player.teleport(loc);
						player.sendMessage("Has entrado al modo pvpUP");
					}else {
						player.sendMessage("ya estas dentro del modo pvpUP");
					}
					break;
				case "leave":
					Location anterior = Manager.leave(player);
					if(anterior==null){
						player.sendMessage("No estas dentro del modo pvpUP");
					}else {
						player.teleport(anterior);
						player.sendMessage("Has salido del modo pvpUP");
					}
					break;
				}
				return true;
			}
		} else {
			sender.sendMessage("Usted debe ser un jugador!");
		}
		return false;
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
