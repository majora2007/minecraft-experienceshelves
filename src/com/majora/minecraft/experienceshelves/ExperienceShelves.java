package com.majora.minecraft.experienceshelves;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.majora.minecraft.experienceshelves.listeners.PlayerListener;
import com.majora.minecraft.experienceshelves.models.IRepository;
import com.majora.minecraft.experienceshelves.models.JSONRepository;
import com.majora.minecraft.experienceshelves.models.XPVault;
import com.majora.minecraft.experienceshelves.tasks.SendPacketTask;
import com.majora.minecraft.experienceshelves.utils.FileUtils;

public final class ExperienceShelves extends JavaPlugin {
	
	private static Logger consoleLogger = Logger.getLogger("Minecraft");
	private static String prefix;
	
	private static final int	SEND_PACKET_INTERVAL	= 20;
	private static final int	SEND_PACKET_DELAY	= 20;
	
	private static ExperienceShelves instance = null;
	
	private PlayerListener playerListener;
	private IRepository<Location, XPVault> repository;
	//private List<BukkitTask> particlePacketTasks = new ArrayList<BukkitTask>(3);
	
	@Override
	public void onEnable() 
	{
		instance = this;
		
		// Save a copy of the default config.yml if one is not there
        this.saveDefaultConfig();
        
		initializeLoggerPrefix();
		
		final String vaultsFilePath = "" + this.getDataFolder() + "\\vaults.JSON";
		repository = new JSONRepository(vaultsFilePath, getServer());
		
		// Load external permission/group plugins.
		super.onEnable();
		
		this.playerListener = new PlayerListener(this, repository);
		getServer().getPluginManager().registerEvents(this.playerListener, this);
		
		loadProperties();
		
		// First check to see if the file exists, if not, this is our first time launching and we can ignore 
		// this statement.
		loadIfFileExitsts(vaultsFilePath);
		if (this.getConfig().getBoolean("show-particles"))
		{
			scheduleParticleTasksForExistingVaults();
		}
	}

	private void scheduleParticleTasksForExistingVaults() {
		Map<Location, XPVault> rawData = repository.getData();
		Iterator<Entry<Location, XPVault>> it = rawData.entrySet().iterator();
    	
    	while (it.hasNext())
    	{
    		Entry<Location, XPVault> pairs = it.next();
    		scheduleDefaultVaultAnimation(pairs.getValue(), pairs.getKey().getBlock());
    	}	
	}

	private void loadIfFileExitsts(final String vaultsFilePath) {
		if (FileUtils.exists(vaultsFilePath))
		{
			repository.load();
		} else {
			ExperienceShelves.log("No vaults file loaded.");
		}
	}
	
	private void loadProperties() {
		int creationItem = getConfig().getInt("creation-item", -1);
		if (creationItem == -1) getConfig().set("creation-item", 0); // set to default
		
		boolean useInCreativeWorld = getConfig().getBoolean("use-in-creative", false);
		if (useInCreativeWorld == false) getConfig().set("use-in-creative", false);
		
		boolean useParticles = this.getConfig().getBoolean("show-particles", true);
		if (useParticles == true) getConfig().set("show-particles", true);
		
		
		this.saveConfig();
	}

	@Override
	public void onDisable() 
	{
		
		Map<Location, XPVault> rawData = repository.getData();
		Iterator<Entry<Location, XPVault>> it = rawData.entrySet().iterator();
    	
    	while (it.hasNext())
    	{
    		Entry<Location, XPVault> pairs = it.next();
    		
    		for(BukkitTask task : pairs.getValue().getParticleTasks())
			{
				task.cancel();
			}
    	}
		
		repository.save();
	}
	
	private void initializeLoggerPrefix()
	{
		final PluginDescriptionFile pluginDescriptionFile = getDescription();
		prefix = "[" +  pluginDescriptionFile.getName() + "]: ";
	}
	

	public static void log(final String msg)
	{
		ExperienceShelves.consoleLogger.info(prefix + msg);
	}
	
	@EventHandler
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (!(cmd.getName().equalsIgnoreCase("xps") || cmd.getName().equalsIgnoreCase("experienceshelves"))) return false;
		if (sender instanceof Player) return true; // Handling in command preprocess for now
		
		return true;
	}

	public static ExperienceShelves getInstance()
	{
		return instance;
	}
	
	public void scheduleDefaultVaultAnimation(final XPVault vault, final Block block)
	{
		final Object packet = PacketFactory.createParticlePacket( ParticleType.MOB_SPELL_AMBIENT, block, 1.0f, 0.6f, 1.0f, 0.3f, 100 );
		BukkitTask task = new SendPacketTask( this, block.getLocation(), packet ).runTaskTimer( this, SEND_PACKET_DELAY, SEND_PACKET_INTERVAL );
		vault.addParticleTask(task);
		//particlePacketTasks.add( new SendPacketTask( this.plugin, clickedBlock.getLocation(), packet ).runTaskTimer( this.plugin, SEND_PACKET_DELAY + new Random().nextInt( 19 ),  + new Random().nextInt( SEND_PACKET_INTERVAL ) ) );
	}
}
