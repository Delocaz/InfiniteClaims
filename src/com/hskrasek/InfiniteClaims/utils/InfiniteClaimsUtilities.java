package com.hskrasek.InfiniteClaims.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;

import uk.co.jacekk.bukkit.infiniteplots.InfinitePlotsGenerator;

import com.hskrasek.InfiniteClaims.InfiniteClaims;
import com.hskrasek.InfiniteClaims.configuration.InfiniteClaimsPlotConfig;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.masks.RegionMask;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class InfiniteClaimsUtilities 
{
	InfiniteClaims plugin;
	static Location startLoc;
	static WorldGuardPlugin wgp;
	WorldEditPlugin wep;
	static String pluginPrefix = ChatColor.WHITE + "[" + ChatColor.RED + "InfiniteClaims" + ChatColor.WHITE + "] ";
	static int walkwaySize = 7;
	static int plotHeight;
	File plotFile;
	
	public InfiniteClaimsUtilities(InfiniteClaims instance)
	{
		plugin = instance;
		wgp = plugin.getWorldGuard();
		wep = plugin.getWorldEdit();
		plotHeight = plugin.plotHeight;
	}
	
	// TODO Combine this method and findNewPlot, so that we dont have two methods doing the same thing
	public void plotAssigner(World w, Player p, int y, int plotSize)
	{
		com.sk89q.worldguard.LocalPlayer lp = wgp.wrapPlayer(p);
		startLoc = new Location(w, plugin.roadOffsetX, y, plugin.roadOffsetZ);
		RegionManager rm = wgp.getRegionManager(w);
		int playerRegionCount = rm.getRegionCountOfPlayer(lp);
		Location workingLocation = startLoc; // workingLocation will be used for searching for an empty plot
		
		if(playerRegionCount < 1)
		{
			int regionSpacing = plotSize + walkwaySize;
			int failedAttemptCount = 0;
			boolean owned = true;
			
			Map<String, ProtectedRegion> regions = rm.getRegions();
			Set<String> keySet = regions.keySet();
			Object[] keys = keySet.toArray();
			int failedAttemptMaxCount = keys.length + 1; // finding an owned region counts as a failed attempt, so it's possible to validly have that many failures

			p.sendMessage(pluginPrefix + "Hi " + p.getName() + ".  You don't seem to have a plot. Let me fix that for you!");
			
			while(owned && failedAttemptCount < failedAttemptMaxCount)
			{
				// this block will execute until the owned flag is set to false or until failedAttemptCount reaches the max
				
				owned = false; // ensures the loop will only execute once if no plots are owned.
				Random rnd = new Random();
				int plotDir = rnd.nextInt(8);
				List<Location> checkedLocations = new ArrayList<Location>();
				
				if(plotDir == 0)
				{
					// one plot to the right of current workingLocation
					workingLocation = new Location(w, workingLocation.getX() + regionSpacing, y, workingLocation.getZ());
				}
				else if(plotDir == 1)
				{
					// one plot to the right and up of current workingLocation							
					workingLocation = new Location(w, workingLocation.getX() + regionSpacing, y, workingLocation.getZ() + regionSpacing);		
				}
				else if(plotDir == 2)
				{
					// one plot up of current workingLocation													
					workingLocation = new Location(w, workingLocation.getX(), y, workingLocation.getZ() + regionSpacing);		
				}
				else if(plotDir == 3)
				{
					// one plot to the left and up of current workingLocation													
					workingLocation = new Location(w, workingLocation.getX() - regionSpacing, y, workingLocation.getZ() + regionSpacing);					
				}
				else if(plotDir == 4)
				{
					// one plot to the left of current workingLocation													
					workingLocation = new Location(w, workingLocation.getX() - regionSpacing, y, workingLocation.getZ());					
				}
				else if(plotDir == 5)
				{
					// one plot to the left and down of current workingLocation													
					workingLocation = new Location(w, workingLocation.getX() - regionSpacing, y, workingLocation.getZ() - regionSpacing);					
				}
				else if(plotDir == 6)
				{
					// one plot down of current workingLocation													
					workingLocation = new Location(w, workingLocation.getX(), y, workingLocation.getZ() - regionSpacing);					
				}
				else if(plotDir == 7)
				{
					// one plot to the right and down of current workingLocation							
					workingLocation = new Location(w, workingLocation.getX() + regionSpacing, y, workingLocation.getZ() - regionSpacing);					
				}

				if(!checkedLocations.contains(workingLocation))
				{
					// only check the region if it hasn't already been checked, otherwise it will falsely update the failedAttemptCount
					checkedLocations.add(workingLocation);

					for (Object key : keys)
					{
						ProtectedRegion pr = regions.get(key);	
						owned = pr.contains((int)workingLocation.getX(), (int)workingLocation.getY(), (int)workingLocation.getZ());

						if(owned)
						{
							// if the ProtectedRegion contains the coord's of the workingLocation, then 
							// it's owned and we need to reset workingLocation to a new spot
							failedAttemptCount++;
							break;
						}							
					}							
				}					
			}
			
			if(failedAttemptCount < failedAttemptMaxCount)
			{
				Location bottomRight = workingLocation; // not really needed, I did it just for clarity
                Location bottomLeft = new Location(w, workingLocation.getX() + (plotSize - 1), y, workingLocation.getZ());
                Location topRight = new Location(w, workingLocation.getX(), y, workingLocation.getZ() + (plotSize - 1));
                Location topLeft = new Location(w, workingLocation.getX() + (plotSize - 1), y, workingLocation.getZ() + (plotSize - 1));
				CuboidSelection plot = new CuboidSelection(w, bottomRight, topLeft);
				Region tempRegion = null;
				
				try 
				{
					tempRegion = plot.getRegionSelector().getRegion();
					tempRegion.expand(new Vector(0, w.getMaxHeight(), 0), new Vector(0, (-(plugin.plotHeight)+1), 0));
				} 
				catch (IncompleteRegionException e) 
				{
					p.sendMessage(e.getMessage());
				}
				catch(RegionOperationException e)
				{
					p.sendMessage(e.getMessage());
				}
				
				LocalSession session = wep.getSession(p);
				CuboidRegion weTesting = (CuboidRegion) tempRegion;
				RegionMask rMask = new RegionMask(weTesting);
				
				session.setMask(rMask);
				
				String plotName = p.getName().toLowerCase() + "Plot" + (playerRegionCount + 1); // failedAttemptCount is appended at the end for uniqueness
				p.sendMessage(pluginPrefix + "I've found a plot for you! Naming it: " + ChatColor.YELLOW + "plot" + (playerRegionCount + 1));
				p.sendMessage(pluginPrefix + "You will need this name to return to your plot");
				
				BlockVector minPoint = tempRegion.getMinimumPoint().toBlockVector();
				BlockVector maxPoint = tempRegion.getMaximumPoint().toBlockVector();
				ProtectedRegion playersPlot = new ProtectedCuboidRegion(plotName, minPoint, maxPoint);
				DefaultDomain owner = new DefaultDomain();
				owner.addPlayer(lp);
				playersPlot.setOwners(owner);
				
				
				RegionManager mgr = wgp.getGlobalRegionManager().get(w) ;
				mgr.addRegion(playersPlot);
				try 
				{
					mgr.save();
				} catch (ProtectionDatabaseException e) 
				{
					e.printStackTrace();
				}
				
				if(plugin.signPlacementMethod.equals("entrance"))
				{
					Location entranceLocation1 = new Location(w, bottomRight.getX() + (plotSize / 2) - 2, y + 3, bottomRight.getZ() + (plotSize));
	                Location entranceLocation2 = new Location(w, bottomRight.getX() + (plotSize / 2) + 2, y + 3, bottomRight.getZ() + (plotSize));
	                placeSign(entranceLocation1, plugin.ownerSignPrefix, p.getName(), BlockFace.SOUTH);
	                placeSign(entranceLocation2, plugin.ownerSignPrefix, p.getName(), BlockFace.SOUTH);
				}
				else if(plugin.signPlacementMethod.equals("corners"))
				{
					// creates a sign for the bottom right corner
					Location bottomRightTest = new Location(w, bottomRight.getX() - 1, bottomRight.getY() + 3, bottomRight.getZ() -1);
	                placeSign(bottomRightTest, plugin.ownerSignPrefix, p.getName(), BlockFace.NORTH_WEST);
	                
					// creates a sign for the bottom left corner
	                Location bottomLeftTest = new Location(w, bottomLeft.getX() + 1, bottomLeft.getY() + 3, bottomLeft.getZ() - 1);
	                placeSign(bottomLeftTest, plugin.ownerSignPrefix, p.getName(), BlockFace.NORTH_EAST);

					// creates a sign for the top right corner
	                Location topRightSign = new Location(w, topRight.getX() - 1, topRight.getY() + 3, topRight.getZ() + 1);
	                placeSign(topRightSign, plugin.ownerSignPrefix, p.getName(), BlockFace.SOUTH_WEST);
	                
	                // creates a sign for the top left corner
	                Location topLeftSign = new Location(w, topLeft.getX() + 1, topLeft.getY() + 3, topLeft.getZ() + 1);
	                placeSign(topLeftSign, plugin.ownerSignPrefix, p.getName(), BlockFace.SOUTH_EAST);                
				}
				else if(plugin.signPlacementMethod.equals("both"))
				{
					Location entranceLocation1 = new Location(w, bottomRight.getX() + (plotSize / 2) - 2, y + 3, bottomRight.getZ() + (plotSize));
	                Location entranceLocation2 = new Location(w, bottomRight.getX() + (plotSize / 2) + 2, y + 3, bottomRight.getZ() + (plotSize));
	                placeSign(entranceLocation1, plugin.ownerSignPrefix, p.getName(), BlockFace.SOUTH);
	                placeSign(entranceLocation2, plugin.ownerSignPrefix, p.getName(), BlockFace.SOUTH);
	                
	                // creates a sign for the bottom right corner
					Location bottomRightTest = new Location(w, bottomRight.getX() - 1, bottomRight.getY() + 3, bottomRight.getZ() -1);
	                placeSign(bottomRightTest, plugin.ownerSignPrefix, p.getName(), BlockFace.NORTH_WEST);
	                
					// creates a sign for the bottom left corner
	                Location bottomLeftTest = new Location(w, bottomLeft.getX() + 1, bottomLeft.getY() + 3, bottomLeft.getZ() - 1);
	                placeSign(bottomLeftTest, plugin.ownerSignPrefix, p.getName(), BlockFace.NORTH_EAST);

					// creates a sign for the top right corner
	                Location topRightSign = new Location(w, topRight.getX() - 1, topRight.getY() + 3, topRight.getZ() + 1);
	                placeSign(topRightSign, plugin.ownerSignPrefix, p.getName(), BlockFace.SOUTH_WEST);
	                
	                // creates a sign for the top left corner
	                Location topLeftSign = new Location(w, topLeft.getX() + 1, topLeft.getY() + 3, topLeft.getZ() + 1);
	                placeSign(topLeftSign, plugin.ownerSignPrefix, p.getName(), BlockFace.SOUTH_EAST);    
				}
                
                // teleports player to their plot
				p.teleport(new Location(w, bottomRight.getX() + (plotSize / 2), y + 2, bottomRight.getZ() + (plotSize), 180, 0));
				savePlot(p,"plot"+(playerRegionCount + 1), new Location(w, bottomRight.getX() + (plotSize / 2), y + 1, bottomRight.getZ() + (plotSize)));
				p.sendMessage(pluginPrefix + "You can return to this plot with: ");
				p.sendMessage(ChatColor.YELLOW + "/iclaims plot " + ChatColor.RED + "plot" + (playerRegionCount + 1) + ChatColor.WHITE + " while in the world " + w.getName());
				p.sendMessage(ChatColor.YELLOW + "/iclaims plot " + ChatColor.RED + "plot" + (playerRegionCount + 1) + ChatColor.WHITE + " -w " + ChatColor.RED + w.getName());
			}
			else
			{
				p.sendMessage(pluginPrefix + "Unable to find an unclaimed location.  Please exit the world and try again.  If this continues, please notify an admin.");
			}			
		}
	}
	
	public void findNewPlot(World w, Player p, int y, int plotSize)
	{
		com.sk89q.worldguard.LocalPlayer lp = wgp.wrapPlayer(p);
		startLoc = new Location(w, plugin.roadOffsetX, y, plugin.roadOffsetZ);
		RegionManager rm = wgp.getRegionManager(w);
		int playerRegionCount = rm.getRegionCountOfPlayer(lp);
		Location workingLocation = startLoc;
		
		if(playerRegionCount < plugin.maxPlots)
		{
			int regionSpacing = plotSize + walkwaySize;
			int failedAttemptCount = 0;
			boolean owned = true;
			
			Map<String, ProtectedRegion> regions = rm.getRegions();
			Set<String> keySet = regions.keySet();
			Object[] keys = keySet.toArray();
			int failedAttemptMaxCount = keys.length + 1; // finding an owned region counts as a failed attempt, so it's possible to validly have that many failures

			p.sendMessage(pluginPrefix + ((playerRegionCount == 0) ? "Finding you a plot in " : "Finding you another plot in the world ") + w.getName());
			
			while(owned && failedAttemptCount < failedAttemptMaxCount)
			{
				// this block will execute until the owned flag is set to false or until failedAttemptCount reaches the max
				
				owned = false; // ensures the loop will only execute once if no plots are owned.
				Random rnd = new Random();
				int plotDir = rnd.nextInt(8);
				List<Location> checkedLocations = new ArrayList<Location>();
				
				if(plotDir == 0)
				{
					// one plot to the right of current workingLocation
					workingLocation = new Location(w, workingLocation.getX() + regionSpacing, y, workingLocation.getZ());
				}
				else if(plotDir == 1)
				{
					// one plot to the right and up of current workingLocation							
					workingLocation = new Location(w, workingLocation.getX() + regionSpacing, y, workingLocation.getZ() + regionSpacing);		
				}
				else if(plotDir == 2)
				{
					// one plot up of current workingLocation													
					workingLocation = new Location(w, workingLocation.getX(), y, workingLocation.getZ() + regionSpacing);		
				}
				else if(plotDir == 3)
				{
					// one plot to the left and up of current workingLocation													
					workingLocation = new Location(w, workingLocation.getX() - regionSpacing, y, workingLocation.getZ() + regionSpacing);					
				}
				else if(plotDir == 4)
				{
					// one plot to the left of current workingLocation													
					workingLocation = new Location(w, workingLocation.getX() - regionSpacing, y, workingLocation.getZ());					
				}
				else if(plotDir == 5)
				{
					// one plot to the left and down of current workingLocation													
					workingLocation = new Location(w, workingLocation.getX() - regionSpacing, y, workingLocation.getZ() - regionSpacing);					
				}
				else if(plotDir == 6)
				{
					// one plot down of current workingLocation													
					workingLocation = new Location(w, workingLocation.getX(), y, workingLocation.getZ() - regionSpacing);					
				}
				else if(plotDir == 7)
				{
					// one plot to the right and down of current workingLocation							
					workingLocation = new Location(w, workingLocation.getX() + regionSpacing, y, workingLocation.getZ() - regionSpacing);					
				}

				if(!checkedLocations.contains(workingLocation))
				{
					// only check the region if it hasn't already been checked, otherwise it will falsely update the failedAttemptCount
					checkedLocations.add(workingLocation);

					for (Object key : keys)
					{
						ProtectedRegion pr = regions.get(key);	
						owned = pr.contains((int)workingLocation.getX(), (int)workingLocation.getY(), (int)workingLocation.getZ());

						if(owned)
						{
							// if the ProtectedRegion contains the coord's of the workingLocation, then 
							// it's owned and we need to reset workingLocation to a new spot
							failedAttemptCount++;
							break;
						}							
					}							
				}					
			}
			
			if(failedAttemptCount < failedAttemptMaxCount)
			{
				Location bottomRight = workingLocation; // not really needed, I did it just for clarity
                Location bottomLeft = new Location(w, workingLocation.getX() + (plotSize - 1), y, workingLocation.getZ());
                Location topRight = new Location(w, workingLocation.getX(), y, workingLocation.getZ() + (plotSize - 1));
                Location topLeft = new Location(w, workingLocation.getX() + (plotSize - 1), y, workingLocation.getZ() + (plotSize - 1));
				CuboidSelection plot = new CuboidSelection(w, bottomRight, topLeft);
				Region tempRegion = null;
				
				if(plugin.DEBUGGING)
				{
					plugin.log.debug("==Get Plot Debug==");
					plugin.log.debug("Bottom Right: " + bottomRight.toString());
					plugin.log.debug("Top Left: " + topLeft.toString());
				}
				
				try 
				{
					tempRegion = plot.getRegionSelector().getRegion();
					tempRegion.expand(new Vector(0, w.getMaxHeight(), 0), new Vector(0, (-(plugin.plotHeight)+1), 0));
					
				} 
				catch (IncompleteRegionException e) 
				{
					p.sendMessage(e.getMessage());
				}
				catch(RegionOperationException e)
				{
					p.sendMessage(e.getMessage());
				}
				
				String plotName = p.getName() + "Plot" + (playerRegionCount + 1);
				p.sendMessage(pluginPrefix + "I've found a plot for you! Naming it: " + ChatColor.YELLOW + "plot" + (playerRegionCount + 1));
				p.sendMessage(pluginPrefix + "You will need this name to return to your plot");
				
				BlockVector minPoint = tempRegion.getMinimumPoint().toBlockVector();
				BlockVector maxPoint = tempRegion.getMaximumPoint().toBlockVector();
				ProtectedRegion playersPlot = new ProtectedCuboidRegion(plotName, minPoint, maxPoint);
				DefaultDomain owner = new DefaultDomain();
				owner.addPlayer(lp);
				playersPlot.setOwners(owner);
				
				
				RegionManager mgr = wgp.getGlobalRegionManager().get(w) ;
				mgr.addRegion(playersPlot);
				try 
				{
					mgr.save();
				} catch (ProtectionDatabaseException e) 
				{
					e.printStackTrace();
				}
				
				if(plugin.signPlacementMethod.equals("entrance"))
				{
					Location entranceLocation1 = new Location(w, bottomRight.getX() + (plotSize / 2) - 2, y + 3, bottomRight.getZ() + (plotSize));
	                Location entranceLocation2 = new Location(w, bottomRight.getX() + (plotSize / 2) + 2, y + 3, bottomRight.getZ() + (plotSize));
	                placeSign(entranceLocation1, plugin.ownerSignPrefix, p.getName(), BlockFace.SOUTH);
	                placeSign(entranceLocation2, plugin.ownerSignPrefix, p.getName(), BlockFace.SOUTH);
				}
				else if(plugin.signPlacementMethod.equals("corners"))
				{
					// creates a sign for the bottom right corner
					Location bottomRightTest = new Location(w, bottomRight.getX() - 1, bottomRight.getY() + 3, bottomRight.getZ() -1);
	                placeSign(bottomRightTest, plugin.ownerSignPrefix, p.getName(), BlockFace.NORTH_WEST);
	                
					// creates a sign for the bottom left corner
	                Location bottomLeftTest = new Location(w, bottomLeft.getX() + 1, bottomLeft.getY() + 3, bottomLeft.getZ() - 1);
	                placeSign(bottomLeftTest, plugin.ownerSignPrefix, p.getName(), BlockFace.NORTH_EAST);

					// creates a sign for the top right corner
	                Location topRightSign = new Location(w, topRight.getX() - 1, topRight.getY() + 3, topRight.getZ() + 1);
	                placeSign(topRightSign, plugin.ownerSignPrefix, p.getName(), BlockFace.SOUTH_WEST);
	                
	                // creates a sign for the top left corner
	                Location topLeftSign = new Location(w, topLeft.getX() + 1, topLeft.getY() + 3, topLeft.getZ() + 1);
	                placeSign(topLeftSign, plugin.ownerSignPrefix, p.getName(), BlockFace.SOUTH_EAST);                
				}
				else if(plugin.signPlacementMethod.equals("both"))
				{
					Location entranceLocation1 = new Location(w, bottomRight.getX() + (plotSize / 2) - 2, y + 3, bottomRight.getZ() + (plotSize));
	                Location entranceLocation2 = new Location(w, bottomRight.getX() + (plotSize / 2) + 2, y + 3, bottomRight.getZ() + (plotSize));
	                placeSign(entranceLocation1, plugin.ownerSignPrefix, p.getName(), BlockFace.SOUTH);
	                placeSign(entranceLocation2, plugin.ownerSignPrefix, p.getName(), BlockFace.SOUTH);
	                
	                // creates a sign for the bottom right corner
					Location bottomRightTest = new Location(w, bottomRight.getX() - 1, bottomRight.getY() + 3, bottomRight.getZ() -1);
	                placeSign(bottomRightTest, plugin.ownerSignPrefix, p.getName(), BlockFace.NORTH_WEST);
	                
					// creates a sign for the bottom left corner
	                Location bottomLeftTest = new Location(w, bottomLeft.getX() + 1, bottomLeft.getY() + 3, bottomLeft.getZ() - 1);
	                placeSign(bottomLeftTest, plugin.ownerSignPrefix, p.getName(), BlockFace.NORTH_EAST);

					// creates a sign for the top right corner
	                Location topRightSign = new Location(w, topRight.getX() - 1, topRight.getY() + 3, topRight.getZ() + 1);
	                placeSign(topRightSign, plugin.ownerSignPrefix, p.getName(), BlockFace.SOUTH_WEST);
	                
	                // creates a sign for the top left corner
	                Location topLeftSign = new Location(w, topLeft.getX() + 1, topLeft.getY() + 3, topLeft.getZ() + 1);
	                placeSign(topLeftSign, plugin.ownerSignPrefix, p.getName(), BlockFace.SOUTH_EAST);    
				}
                
                // teleports player to their plot
				p.teleport(new Location(w, bottomRight.getX() + (plotSize / 2), y + 2, bottomRight.getZ() + (plotSize), 180, 0));
				savePlot(p,"plot"+(playerRegionCount + 1), new Location(w, bottomRight.getX() + (plotSize / 2), y + 1, bottomRight.getZ() + (plotSize)));
				p.sendMessage(pluginPrefix + "You can return to this plot with: ");
				p.sendMessage(ChatColor.YELLOW + "/iclaims plot " + ChatColor.RED + "plot" + (playerRegionCount + 1) + ChatColor.WHITE + " while in the world " + w.getName());
				p.sendMessage(ChatColor.YELLOW + "/iclaims plot " + ChatColor.RED + "plot" + (playerRegionCount + 1) + ChatColor.WHITE + " -w " + ChatColor.RED + w.getName());
				p.sendMessage(pluginPrefix + "You can claim " + ChatColor.YELLOW + (mgr.getRegionCountOfPlayer(lp) == plugin.maxPlots ? 0 : (plugin.maxPlots - mgr.getRegionCountOfPlayer(lp))) + ChatColor.WHITE + " more plots in this world.");
			}
			else
			{
				p.sendMessage(pluginPrefix + "Unable to find an unclaimed location.  Please exit the world and try again.  If this continues, please notify an admin.");
			}			
		}
		else
		{
			p.sendMessage(pluginPrefix + "You already have the maximum number of plots allowed on this server. Please use '" + ChatColor.YELLOW + "/iclaims plot" + ChatColor.WHITE + "' to return to an existing plot.");
		}
	}
	
	public void savePlot(Player thePlayer,String plotName, Location theLocation)
	{
		/*
		YamlConfiguration plots = new YamlConfiguration();
		try {
			plots.load(new File(plugin.getDataFolder().getAbsolutePath() + File.separator + theLocation.getWorld().getName() + File.separator + "plots.yml"));
		} catch (FileNotFoundException e1) {
			plugin.log.severe("Could not find the 'plots.yml' file for the world: " + theLocation.getWorld().getName());
			e1.printStackTrace();
		} catch (IOException e1) {
			plugin.log.severe("There was an error reading the 'plots.yml' file for " + theLocation.getWorld().getName() +". Please submit a ticket to the plugin developer.");
			e1.printStackTrace();
		} catch (InvalidConfigurationException e1) {
			plugin.log.severe("The format for 'plots.yml' file for the world " + theLocation.getWorld().getName() + " is incorrect, please submit a ticket to the plugin developer.");
			e1.printStackTrace();
		}
		*/
		
		InfiniteClaimsPlotConfig plotFile = new InfiniteClaimsPlotConfig(this.plugin, theLocation.getWorld());
		List<Double> coords = new ArrayList<Double>();
		coords.add(0, theLocation.getX());
		coords.add(1, theLocation.getZ());
		plotFile.setPlot(thePlayer.getName(), plotName, coords);
	}
	
	/**
	 * TODO Change reset plot to accept a specific plot name, which will then select that specifc WorldGuard region, and regenerate it.
	 * TODO FIX RESET!!!
	 * @param thePlayer
	 */
	public void regeneratePlot(Player thePlayer)
	{
		World claimsWorld = thePlayer.getWorld();
		ChunkGenerator cg = claimsWorld.getGenerator();
		com.sk89q.worldguard.LocalPlayer localPlayer = wgp.wrapPlayer(thePlayer);
		if(cg instanceof InfinitePlotsGenerator == true)
		{
			RegionManager mgr = wgp.getGlobalRegionManager().get(claimsWorld);
			Map<String, ProtectedRegion> regions = mgr.getRegions();
			Set<String> regionsIds = regions.keySet();
			String regionToRegenId = "";
			ProtectedRegion tempRegion = null;
			Region regionToRegenerate = null;
			for(String regionId : regionsIds)
			{
				ProtectedRegion playersPlot = regions.get(regionId);
				if(!playersPlot.isOwner(localPlayer))
				{
					continue;
				}
				else
				{
					if(plugin.DEBUGGING)
					{
						plugin.log.debug("Found region for player: " + thePlayer.getName() + " Region - " + regionId);
					}
					regionToRegenId = regionId;
					break;
				}
			}
			
			tempRegion = mgr.getRegion(regionToRegenId);
			
			regionToRegenerate = selectPlot(thePlayer, localPlayer, tempRegion);
			if(plugin.DEBUGGING)
			{
				plugin.log.debug("Region to Regenerate: " + regionToRegenerate + " Area of the Region: " + regionToRegenerate.getArea());
			}
			BukkitPlayer tempPlayer = new BukkitPlayer(wep, wep.getServerInterface(), thePlayer);
			LocalSession theSession = wep.getSession(thePlayer);
			EditSession editSession = wep.createEditSession(thePlayer);
			Mask mask = theSession.getMask();
			theSession.setMask(null);
			try {
				List<Vector> changes = new ArrayList<Vector>(4);
				changes.add(new Vector(1,0,0));
				changes.add(new Vector(-1,0,0));
				changes.add(new Vector(0,0,-1));
				changes.add(new Vector(0,0,1));
				
				regionToRegenerate.contract(changes.toArray(new Vector[0]));
				plugin.log.debug("Region with contract: " + regionToRegenerate);
			} catch (RegionOperationException e) {
				
				e.printStackTrace();
			}
			tempPlayer.getWorld().regenerate(regionToRegenerate, editSession);
			theSession.setMask(mask);
			
//			Set<Vector2D> chunks = regionToRegenerate.getChunks();
//			Iterator<Vector2D> chunkCords = chunks.iterator();
//			
//			while(chunkCords.hasNext())
//			{
//				Vector2D chunkCoord = chunkCords.next();
//				if(plugin.DEBUGGING)
//				{
//					plugin.log.debug(String.format("Now refreshing chunk (%d,%d)", chunkCoord.getBlockX(), chunkCoord.getBlockZ()));
//				}
//				claimsWorld.refreshChunk(chunkCoord.getBlockX(), chunkCoord.getBlockZ());
//			}
			
			ProtectedRegion test = new ProtectedCuboidRegion(thePlayer.getName()+"plotTest",regionToRegenerate.getMinimumPoint().toBlockVector(), regionToRegenerate.getMaximumPoint().toBlockVector());
			mgr.removeRegion(regionToRegenId);
			mgr.addRegion(test);
			
			try {
				mgr.save();
			} catch (ProtectionDatabaseException e) {
				e.printStackTrace();
			}
			
			thePlayer.sendMessage(pluginPrefix + " Your plot has been reset.");
		}
		else
		{
			thePlayer.sendMessage("Please return to your plot to reset it");
		}
		
	}
	
	public void addMember(Player plotOwner, String playerToAdd, String plotName, World plotWorld)
	{
		ChunkGenerator cg = plotWorld.getGenerator();
		if(cg instanceof InfinitePlotsGenerator == true)
		{
			RegionManager mgr = wgp.getGlobalRegionManager().get(plotWorld);
			String regionId = plotOwner.getName() + plotName;
			
			ProtectedRegion ownersPlot = mgr.getRegion(regionId);
			DefaultDomain members = ownersPlot.getMembers();
			members.addPlayer(playerToAdd);
			
			ownersPlot.setMembers(members);
			
			try 
			{
				mgr.save();
			} catch (ProtectionDatabaseException e) 
			{
				e.printStackTrace();
			}
			
			plotOwner.sendMessage(pluginPrefix + "Added '" + ChatColor.YELLOW + playerToAdd + ChatColor.WHITE + "' to plot: " + ChatColor.YELLOW + plotName);
			
			return;
		}
		else
		{
			plotOwner.sendMessage(pluginPrefix + ChatColor.RED + plotWorld + ChatColor.YELLOW + " is not a valid plot world. Please try again!");
		}
	}
	
	public void removeMember(Player plotOwner, String playerToRemove, String plotName, World plotWorld)
	{
		ChunkGenerator cg = plotWorld.getGenerator();
		if(cg instanceof InfinitePlotsGenerator == true)
		{
			RegionManager mgr = wgp.getGlobalRegionManager().get(plotWorld);
			String regionId = plotOwner.getName() + plotName;
			
			ProtectedRegion ownersPlot = mgr.getRegion(regionId);
			DefaultDomain members = ownersPlot.getMembers();
			
			if(playerToRemove.equalsIgnoreCase("all"))
			{
				// Remove all players from being members of the plot.
				Iterator<String> remAllIter = members.getPlayers().iterator();
				while(remAllIter.hasNext())
				{
					String player = remAllIter.next();
					members.removePlayer(player);
				}
			}
			else
			{
				// Remove specific player
				members.removePlayer(playerToRemove);
			}
			
			ownersPlot.setMembers(members);
			
			try 
			{
				mgr.save();
			} catch (ProtectionDatabaseException e) 
			{
				e.printStackTrace();
			}
			
			plotOwner.sendMessage(pluginPrefix + "Removed '" + ChatColor.YELLOW + playerToRemove + ChatColor.WHITE + "' from plot: " + ChatColor.YELLOW + plotName);
			
			return;
		}
		else
		{
			plotOwner.sendMessage(pluginPrefix + ChatColor.RED + plotWorld + ChatColor.YELLOW + " is not a valid plot world! Please try again!");
		}
	}
	
	private Region selectPlot(Player player, com.sk89q.worldguard.LocalPlayer localPlayer, ProtectedRegion region)
	{
		ProtectedCuboidRegion cuboid = (ProtectedCuboidRegion)region;
		Vector pt1 = cuboid.getMinimumPoint();
		Vector pt2 = cuboid.getMaximumPoint();
		CuboidSelection selection = new CuboidSelection(player.getWorld(), pt1, pt2);
		try {
			return selection.getRegionSelector().getRegion();
		} catch (IncompleteRegionException e) {
			
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void teleportToPlot(Player thePlayer, String plotName, String worldName)
	{
		
		InfiniteClaimsPlotConfig plotFile = new InfiniteClaimsPlotConfig(this.plugin, new WorldCreator(worldName).createWorld());
		
		Location teleLoc = plotFile.getPlot(thePlayer.getName().toLowerCase(), plotName);
		
		if(teleLoc.getX() != 0 && teleLoc.getZ() !=0)
		{
			thePlayer.teleport(teleLoc);
		}
		else
		{
			thePlayer.sendMessage(pluginPrefix + "The specified plot or world is unavailable. Please try again.");
			return;
		}
	}
	
	public void teleportToOtherPlot(Player thePlayer, String otherPlayer, String plotName, String worldName)
	{
		InfiniteClaimsPlotConfig plotFile = new InfiniteClaimsPlotConfig(this.plugin, new WorldCreator(worldName).createWorld());
		
		Location teleLoc = plotFile.getPlot(otherPlayer, plotName);
		
		if(teleLoc.getX() != 0 && teleLoc.getZ() !=0)
		{
			thePlayer.teleport(teleLoc);
		}
		else
		{
			thePlayer.sendMessage(pluginPrefix + "That player does not have plot under that name. Please try again.");
			return;
		}
	}
	
	public List<World> getInfiniteClaimsWorlds()
	{
		List<World> iclaimsWorlds = new ArrayList<World>();
		for(World world : plugin.getServer().getWorlds())
		{
			ChunkGenerator cg = world.getGenerator();
			if(cg instanceof InfinitePlotsGenerator)
			{
				iclaimsWorlds.add(world);
			}
		}
		
		return iclaimsWorlds;
	}
	
	public void placeSign(Location blockLocation, String plotOwnerPrefix, String plotOwner, BlockFace facingDirection)
	{
		Block signBlock;
		Location tempLocation = new Location(blockLocation.getWorld(), blockLocation.getX(), blockLocation.getY() - 1, blockLocation.getZ());
		if(tempLocation.getBlock().getType() == Material.AIR)
		{
			signBlock = tempLocation.getBlock();
		}
		else
		{
			signBlock = blockLocation.getBlock();
		}
		
		this.placeSign(plotOwnerPrefix, plotOwner, signBlock, facingDirection);
	}
	
	private void placeSign(String plotOwnerPrefix, String plotOwner, Block theBlock, BlockFace facingDirection)
	{
		theBlock.setType(Material.SIGN_POST);
		Sign theSign = (Sign)theBlock.getState();
		theSign.setLine(1, plugin.prefixColor + plotOwnerPrefix);
		if(plotOwner.length() > 15)
		{
			String plotOwnerFirst = plotOwner.substring(0, 13);
			String plotOwnerSecond = plotOwner.substring(plotOwner.length() - 1);
			theSign.setLine(2, plugin.ownerColor + plotOwnerFirst);
			theSign.setLine(3, plugin.ownerColor + plotOwnerSecond);
		}
		else
		{
			theSign.setLine(2, plugin.ownerColor + plotOwner);
		}
		
		byte ne = 0xA; // North East
		byte se = 0xE; // South East
		byte sw = 0x2; // South West
		byte nw = 0x6; // North West
//		byte w = 0x4;
		byte s = 0x0;
		
		if(facingDirection == BlockFace.SOUTH_WEST)
		{
			theSign.setRawData(sw);
		}
		if(facingDirection == BlockFace.SOUTH_EAST)
		{
			theSign.setRawData(se);
		}
		if(facingDirection == BlockFace.NORTH_EAST)
		{
			theSign.setRawData(ne);
		}
		if(facingDirection == BlockFace.NORTH_WEST)
		{
			theSign.setRawData(nw);
		}
		if(facingDirection == BlockFace.SOUTH)
		{
			theSign.setRawData(s);
		}
		theSign.update();
	}
}