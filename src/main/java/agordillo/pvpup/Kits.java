package agordillo.pvpup;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Kits {
	private static Kits kits;
	private static Map<String,ItemStack[]> kit = new HashMap<>();
	
	private ItemStack[] nv0 = new ItemStack[5];
	private ItemStack[] nv1 = new ItemStack[5];
	private ItemStack[] nv3 = new ItemStack[5];
	private ItemStack[] nv5 = new ItemStack[5];
	private ItemStack[] nv7 = new ItemStack[5];
	private ItemStack[] nv10 = new ItemStack[5];
	
	public static ItemStack[] getKit(String lvl) {
		get();
		String nvl = lvl;
		ItemStack[] items;
		do {
			items = kit.get(nvl);
			nvl = String.valueOf((Integer.valueOf(nvl)-1));
		}while(items==null);
		return items;
	}
	
	public static void putKit(ItemStack[] kit,Player jugador) {
		if(kit.length<1) {
			jugador.getInventory().setItemInMainHand(kit[0]);
		}else {
			ItemStack[] armadura = new ItemStack[kit.length-1];
			for(int i=1;i<kit.length;i++) {
				if(kit[i]!=null) {
					armadura[i-1] = kit[i];
				}
			}
			jugador.getInventory().setArmorContents(armadura);
			jugador.getInventory().setItem(0, kit[0]);
			jugador.getInventory().setHeldItemSlot(0);
		}
		
	}

	private static Kits get() {
		if(kits == null) {
			kits = new Kits();
		}
		return kits;
	}
	
	private Kits() {
		initKits();
	}
	
	private void initKits() {
		nv0[0] = new ItemStack(Material.WOOD_SWORD);
		nv0[1] = null;
		nv0[2] = null;
		nv0[3] = null;
		nv0[4] = null;
		kit.put("0",nv0);
		nv1 = nv0.clone();
		nv1[0] = new ItemStack(Material.WOOD_AXE);
		kit.put("1",nv1);
		nv3 = nv1.clone();
		nv3[3] = new ItemStack(Material.LEATHER_CHESTPLATE);
		kit.put("3",nv3);
		nv5 = nv3.clone();
		nv5[2] = new ItemStack(Material.LEATHER_LEGGINGS);
		kit.put("5",nv5);
		nv7 = nv5.clone();
		nv7[4] = new ItemStack(Material.LEATHER_HELMET);
		nv7[1] = new ItemStack(Material.LEATHER_BOOTS);
		kit.put("7",nv7);
		nv10 = nv7.clone();
		nv10[0]  = new ItemStack(Material.IRON_SWORD);
		kit.put("10",nv10);
		/*kit.put("15",);
		kit.put("20",);
		kit.put("25",);
		kit.put("30",);
		kit.put("50",);
		kit.put("100",);*/
	}
	
}
