package com.hskrasek.InfiniteClaims.listeners;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import com.hskrasek.InfiniteClaims.InfiniteClaims;

import uk.co.jacekk.bukkit.infiniteplots.InfinitePlotsGenerator;

public class InfiniteClaimsAutoListener implements Listener
{
	InfiniteClaims plugin;

	public InfiniteClaimsAutoListener(InfiniteClaims instance)
	{
		this.plugin = instance;
	}
	
	@EventHandler
	public void onPlayerChangedWorld(PlayerChangedWorldEvent changedWorld)
	{
		Player p = changedWorld.getPlayer();
		World w = p.getWorld();
		ChunkGenerator cg = w.getGenerator();
		
		if(plugin.DEBUGGING)
		{
			plugin.log.debug("World is using InfinitePlots: " + (cg instanceof InfinitePlotsGenerator != false));
			plugin.log.debug("Player " + p.getName() + " has permissions 'iclaims.plot.auto': " + plugin.getPermissions().hasPermission(p, "iclaims.plot.auto", false));
		}
		
		if(cg instanceof InfinitePlotsGenerator && plugin.getPermissions().hasPermission(p, "iclaims.plot.auto", false))
		{
			int plotSize = ((InfinitePlotsGenerator)cg).getPlotSize();
			if(plugin.DEBUGGING)
			{
				plugin.log.debug("Finding Player: " + p.getName() +" a plot");
			}
			plugin.icUtils.plotAssigner(w, p, plugin.plotHeight, plotSize);
		}
		else if(cg instanceof InfinitePlotsGenerator && !plugin.getPermissions().hasPermission(p, "iclaims.plot.auto", false))
		{
			
		}
	}
}


