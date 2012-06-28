package com.hskrasek.InfiniteClaims;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.hskrasek.InfiniteClaims.commands.HelpCommand;
import com.hskrasek.InfiniteClaims.commands.NewPlotCommand;
import com.hskrasek.InfiniteClaims.commands.PlotListCommand;
import com.hskrasek.InfiniteClaims.commands.PlotManagementAddCommand;
import com.hskrasek.InfiniteClaims.commands.PlotManagementCommand;
import com.hskrasek.InfiniteClaims.commands.PlotManagementInfoCommand;
import com.hskrasek.InfiniteClaims.commands.PlotManagementRemoveCommand;
import com.hskrasek.InfiniteClaims.commands.PlotTeleportCommand;
import com.hskrasek.InfiniteClaims.commands.ReloadCommand;
import com.hskrasek.InfiniteClaims.configuration.InfiniteClaimsConfig;
import com.hskrasek.InfiniteClaims.configuration.InfiniteClaimsPlotConfig;
import com.hskrasek.InfiniteClaims.listeners.InfiniteClaimsAutoListener;
import com.hskrasek.InfiniteClaims.listeners.InfiniteClaimsNewWorld;
import com.hskrasek.InfiniteClaims.utils.InfiniteClaimsUtilities;
import com.hskrasek.InfiniteClaims.utils.LegacyConversion;
import com.pneumaticraft.commandhandler.CommandHandler;
import com.sk89q.wepif.PermissionsResolverManager;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class InfiniteClaims extends JavaPlugin
{
	public InfiniteClaimsLogger log;
	private CommandHandler commandHandler;
	private InfiniteClaimsPerms permissionsInterface;
	protected PluginManager pluginManager;
	protected InfiniteClaimsConfig config;
	public InfiniteClaimsUtilities icUtils;
	public int roadOffsetX = 4;
	public int roadOffsetZ = 4;
	public int plotHeight = 0;
	public int maxPlots = 1;
	public String ownerSignPrefix;
	public String signPlacementMethod;
	public ChatColor prefixColor;
	public ChatColor ownerColor;
	public boolean DEBUGGING;
	private String pluginPrefix = ChatColor.WHITE + "[" + ChatColor.RED + "InfiniteClaims" + ChatColor.WHITE + "] ";
	public PermissionsResolverManager permissionManager;
	
	public void onDisable()
	{
		log.info("Disabled");
		
		log = null;
		config = null;
		icUtils = null;
		permissionsInterface = null;
		commandHandler = null;
	}

	public void onEnable()
	{
		pluginManager = this.getServer().getPluginManager();
		log = new InfiniteClaimsLogger("Minecraft", this);
		icUtils = new InfiniteClaimsUtilities(this);
		config = new InfiniteClaimsConfig(new File(this.getDataFolder().getAbsolutePath() + File.separator + "config.yml"), this);
		permissionsInterface = new InfiniteClaimsPerms(this);
		commandHandler = new CommandHandler(this,permissionsInterface);
		pluginManager.registerEvents(new InfiniteClaimsAutoListener(this), this);
		pluginManager.registerEvents(new InfiniteClaimsNewWorld(this), this);
		ownerSignPrefix = config.getString("signs.prefix");
		signPlacementMethod = config.getString("signs.placement");
		prefixColor = ChatColor.getByChar(config.getString("signs.prefix-color"));
		ownerColor = ChatColor.getByChar(config.getString("signs.owner-color"));
		plotHeight = config.getInt("plots.height");
		maxPlots = config.getInt("plots.max-plots");
		DEBUGGING = config.getBoolean("debugging");
		this.registerCommands();
		this.setupPlotWorlds();
		
		if(config.getString("version").equalsIgnoreCase("2.0.1"))
		{
			this.config.setString("version", "2.0.2");
		}
		else if(config.getString("version") == null || !config.getString("version").equalsIgnoreCase("2.0.2"))
		{
			this.log.info("You were using an older version of InfiniteClaims, scheduling a legacy conversion when the server has a chance.");
			this.getServer().getScheduler().scheduleAsyncDelayedTask(this, new LegacyConversion(this), 40);
			this.config.setString("version", "2.0.2");
		}
		
		this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Updater(this), 40, 432000);
		
		log.info("Enabled");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		ArrayList<String> allArgs = new ArrayList<String>(Arrays.asList(args));
		allArgs.add(0, cmd.getName());
		return this.commandHandler.locateAndRunCommand(sender, allArgs);
	}
	
	private void registerCommands()
	{
		this.commandHandler.registerCommand(new PlotTeleportCommand(this));
		this.commandHandler.registerCommand(new HelpCommand(this));
		this.commandHandler.registerCommand(new PlotManagementCommand(this));
		this.commandHandler.registerCommand(new PlotManagementAddCommand(this));
		this.commandHandler.registerCommand(new PlotManagementRemoveCommand(this));
		this.commandHandler.registerCommand(new PlotManagementInfoCommand(this));
		this.commandHandler.registerCommand(new NewPlotCommand(this));
		this.commandHandler.registerCommand(new PlotListCommand(this));
		this.commandHandler.registerCommand(new ReloadCommand(this));
	}
	
	private void setupPlotWorlds()
	{
		List<World> plotWorlds = icUtils.getInfiniteClaimsWorlds();
		
		for(World plotWorld : plotWorlds)
		{
			@SuppressWarnings("unused")
			InfiniteClaimsPlotConfig plotFile = new InfiniteClaimsPlotConfig(this, plotWorld);
		}
	}

	public String getPluginPrefix() 
	{
		return pluginPrefix;
	}
	
	public CommandHandler getCommandHandler()
	{
		return commandHandler;
	}

	public InfiniteClaimsLogger getLog() 
	{
		return log;
	}

	public InfiniteClaimsUtilities getIcUtils() 
	{
		return icUtils;
	}

	public WorldGuardPlugin getWorldGuard()
	{
	    Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
	
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) 
	    {
	    	log.severe("WorldEdit MUST BE INSTALLED!");
	        return null;
	    }
	    return (WorldGuardPlugin) plugin;
	}

	public WorldEditPlugin getWorldEdit()
	{
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldEdit");
	
		if(plugin == null || !(plugin instanceof WorldEditPlugin)) 
		{
			log.severe("WorlEdit MUST BE INSTALLED");
			return null;
		}
		return (WorldEditPlugin)plugin;
	}

	public InfiniteClaimsPerms getPermissions() {
		return permissionsInterface;
	}
	
	private class Updater implements Runnable
	{
		InfiniteClaims plugin;
		
		public Updater(InfiniteClaims plugin)
		{
			this.plugin = plugin;
		}
		
		public void run()
		{
			try
			{
				final String address = "http://updates.hunterskrasek.com/updates.php?version=" + this.plugin.getDescription().getVersion();
				final URL url = new URL(address.replace(" ", "%20"));
				final URLConnection urlConnection = url.openConnection();
				urlConnection.setConnectTimeout(8000);
	            urlConnection.setReadTimeout(15000);
	            urlConnection.setRequestProperty("Version", this.plugin.getDescription().getVersion());
	            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
	            String tempVersion;
	            
	            if((tempVersion = bufferedReader.readLine()) != null)
	            {
	            	if(!this.plugin.getDescription().getVersion().equals(tempVersion))
	            	{
	            		this.plugin.log.info("Found a different version available: " + tempVersion);
	            		this.plugin.log.info("You can get it at http://dev.bukkit.org/server-mods/infiniteclaims");
	            	}
	            	bufferedReader.close();
	            	urlConnection.getInputStream().close();
	            	return;
	            }
	            else
	            {
	            	bufferedReader.close();
	                urlConnection.getInputStream().close();
	            }
	            System.out.println("Done!");
			}
			catch(final Exception e)
			{
				this.plugin.log.warning("Error: Could not check if InfiniteClaims is up-to-date, will try again later.");
			}
		}
		
	}
}