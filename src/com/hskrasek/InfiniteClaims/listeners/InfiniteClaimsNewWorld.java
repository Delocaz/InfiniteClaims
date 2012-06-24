package com.hskrasek.InfiniteClaims.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;

import uk.co.jacekk.bukkit.infiniteplots.InfinitePlotsGenerator;

import com.hskrasek.InfiniteClaims.InfiniteClaims;
import com.hskrasek.InfiniteClaims.configuration.InfiniteClaimsPlotConfig;

public class InfiniteClaimsNewWorld implements Listener
{
	private InfiniteClaims plugin;
	
	public InfiniteClaimsNewWorld(InfiniteClaims plugin)
	{
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onNewWorldCreation(WorldLoadEvent newWorld)
	{
		ChunkGenerator cg = newWorld.getWorld().getGenerator();
		if(cg instanceof InfinitePlotsGenerator)
		{
			this.plugin.log.info("Creating a plots file for new InfinitePlots world " + newWorld.getWorld().getName());
			InfiniteClaimsPlotConfig plotFile = new InfiniteClaimsPlotConfig(this.plugin, newWorld.getWorld());
			this.plugin.log.info("Plot file created!");
		}
	}
}
