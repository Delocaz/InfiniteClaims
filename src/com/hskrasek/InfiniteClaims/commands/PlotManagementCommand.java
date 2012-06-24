package com.hskrasek.InfiniteClaims.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.hskrasek.InfiniteClaims.InfiniteClaims;
import com.hskrasek.InfiniteClaims.utils.InfiniteClaimsUtilities;

public class PlotManagementCommand extends IClaimsCommand 
{
	InfiniteClaims plugin;
	InfiniteClaimsUtilities icUtils;

	public PlotManagementCommand(InfiniteClaims plugin) 
	{
		super(plugin);
		this.plugin = plugin;
		this.icUtils = plugin.getIcUtils();
		this.setName("Plot Management");
		this.setCommandUsage(String.format("%s/iclaims %s{addmember,removemember,info} ...", ChatColor.YELLOW, ChatColor.RED));
		this.setArgRange(2, 3);
		this.addKey("iclaimsmodify");
		this.addKey("icmodify");
		this.addKey("icm");
		Map<String, Boolean> children = new HashMap<String, Boolean>();
		children.put("iclaims.plot.manage.addmember", true);
		children.put("iclaims.plot.manage.removemember", true);
		children.put("iclaims.plot.manage.info", true);
		Permission mod = new Permission("iclaims.plot.manage", "Manage various parts of your plot, requires addmember/removemember, see below.", PermissionDefault.OP, children);
		this.setPermission(mod);
		this.addCommandExample(String.format("%s/iclaims addmember %s?", ChatColor.YELLOW, ChatColor.RED));
		this.addCommandExample(String.format("%s/iclaims removemember %s?", ChatColor.YELLOW, ChatColor.RED));
		this.addCommandExample(String.format("%s/iclaims info %s?", ChatColor.YELLOW, ChatColor.RED));
	}

	public void runCommand(CommandSender sender, List<String> args) 
	{
		//This command does nothing but provide reference to the separate plot management commands
	}

}
