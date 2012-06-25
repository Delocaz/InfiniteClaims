package com.hskrasek.InfiniteClaims.configuration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConfigurationOptions;

import uk.co.jacekk.bukkit.infiniteplots.InfinitePlotsGenerator;

import com.hskrasek.InfiniteClaims.InfiniteClaims;

public class InfiniteClaimsPlotConfig 
{
	private YamlConfiguration plot;
	private File plotFile;
	private YamlConfigurationOptions plotOptions;
	private HashMap<String, Object> plotDefaults = new HashMap<String, Object>();
	private InfiniteClaims plugin;
	private World plotWorld;
	
	public InfiniteClaimsPlotConfig(InfiniteClaims plugin, World plotWorld)
	{
		plot = new YamlConfiguration();
		this.plugin = plugin;
		this.plotWorld = plotWorld;
		if(plotWorld.getGenerator() instanceof InfinitePlotsGenerator)
		{
			plotFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + plotWorld.getName() + File.separator + "plots.yml");
		}
		else
		{
			if(plugin.DEBUGGING)
			{
				plugin.log.debug("Tried going to a plot in a nonplot world. Prevent plots.yml creation.");
			}
			return;
		}
		
		plotOptions = plot.options();
		plotDefaults.put("plots", "");
		
		String header = this.getHeader();
		plotOptions.header(header);
		
		if(!plotFile.exists())
		{
			plotOptions.copyHeader(true);
			for(String key : plotDefaults.keySet())
			{
				plot.set(key, plotDefaults.get(key));
			}
			
			try
			{
				plot.save(plotFile);
			}
			catch(IOException e)
			{
				this.plugin.log.severe("Could not create a plot file for the World '" + plotWorld.getName() + "'. Disabling InfiniteClaims!");
				this.plugin.getPluginLoader().disablePlugin(plugin);
				e.printStackTrace();
			}
		}
		else
		{
			try
			{
				plot.load(plotFile);
			}
			catch(Exception e)
			{
				this.plugin.log.severe("Could not load the plots for the World '" + plotWorld.getName() + "'. Disabling InfiniteClaims!");
				this.plugin.getPluginLoader().disablePlugin(plugin);
				e.printStackTrace();
			}
		}
	}
	
	public void setPlot(String playerName, String plotName, List<Double> coords)
	{
		plot.set("plots." + playerName + "." + plotName + ".x", coords.get(0));
		plot.set("plots." + playerName + "." + plotName + ".z", coords.get(1));
		this.save();
	}
	
	public Location getPlot(String playerName, String plotName)
	{
		double x = plot.getDouble("plots." + playerName + "." + plotName + ".x");
		double y = plugin.plotHeight + 2;
		double z = plot.getDouble("plots." + playerName + "." + plotName + ".z");
		
		if(plugin.DEBUGGING)
		{
			plugin.log.debug("==Get Plot Debug==");
			plugin.log.debug("Player Name: " + playerName);
			plugin.log.debug("Plot Name: " + plotName);
			plugin.log.debug("X: " + x);
			plugin.log.debug("Y: " + x);
			plugin.log.debug("Z: " + x);
		}
		
		return new Location(this.plotWorld, x, y, z, 180, 0);
	}
	
	private String getHeader()
	{
		String header = "##################################################################\n"
			+ "                     InfiniteClaims Plot File                    #\n"
			+ "This file contains all players plot within this world. Please do #\n"
			+ "not modify it unless absolutely need to. If you do not verify    #\n"
			+ "that the format is correct, you face breaking plot teleportation.#\n"
			+ "You can verify YAML format at: http://tinyurl.com/yamlic         #\n"
			+ "##################################################################\n";
		
		return header;
	}
	
	private void save()
	{
		try 
		{
			plot.save(plotFile);
		} 
		catch (IOException e) 
		{
			this.plugin.log.severe("Could not save the plot file for '" + plotWorld.getName() +"'.");
			e.printStackTrace();
		}
	}
}
