package net.licks92.WirelessRedstone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.licks92.WirelessRedstone.Channel.IWirelessPoint;
import net.licks92.WirelessRedstone.Channel.WirelessChannel;
import net.licks92.WirelessRedstone.Channel.WirelessReceiver;
import net.licks92.WirelessRedstone.Channel.WirelessScreen;
import net.licks92.WirelessRedstone.Channel.WirelessTransmitter;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class WireBox
{
	private final WirelessRedstone plugin;
	private List<Location> receiverlistcachelocation;
	private List<IWirelessPoint> allPointsListCache;

	public WireBox(WirelessRedstone wirelessRedstone)
	{
		this.plugin = wirelessRedstone;
	}

	public boolean isTransmitter(String data)
	{
		List<String> tags = WirelessRedstone.strings.tagsTransmitter;
		for (int i = 0; i < tags.size(); i++)
		{
			if(data == tags.get(i))
			{
				return true;
			}
		}
		return false;
	}

	public boolean isReceiver(String data)
	{
		List<String> tags = WirelessRedstone.strings.tagsReceiver;
		for (int i = 0; i < tags.size(); i++)
		{
			if(data == tags.get(i))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isScreen(String data)
	{
		List<String> tags = WirelessRedstone.strings.tagsScreen;
		for (int i = 0; i < tags.size(); i++)
		{
			if(data == tags.get(i))
			{
				return true;
			}
		}
		return false;
	}

	public WirelessChannel getChannel(String channel)
	{
		return WirelessRedstone.config.getWirelessChannel(channel);
	}

	public boolean hasAccessToChannel(Player player, String channelname)
	{
		if (getChannel(channelname) != null)
		{
			if(this.plugin.permissionsHandler.hasPermission(player, "WirelessRedstone.admin"))
			{
				return true;
			}
			else if(getChannel(channelname).getOwners().contains(player.getName()))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		return true;
	}

	public boolean SaveChannel(WirelessChannel channel)
	{
		WirelessRedstone.config.setWirelessChannel(channel.getName(), channel);
		WirelessRedstone.config.save();
		return true;
	}

	public boolean addWirelessReceiver(String cname, Block cblock, Player player)
	{
		Location loc = cblock.getLocation();
		Boolean isWallSign = (cblock.getType() == Material.WALL_SIGN) ? true : false;
		if (isWallSign)
		{
			isWallSign = true;
			if(!isValidWallLocation(cblock))
			{
				player.sendMessage(WirelessRedstone.strings.playerCannotCreateReceiverOnBlock);
				return false;
			}
		}
		else
		{
			if(!isValidLocation(cblock))
			{
				player.sendMessage(WirelessRedstone.strings.playerCannotCreateReceiverOnBlock);
				return false;
			}
		}
		if (WirelessRedstone.config.get("WirelessChannels." + cname) == null)
		{
			if(cname.contains("."))
			{
				player.sendMessage(WirelessRedstone.strings.playerCannotCreateChannel + " : Name contains invalid caracters !");
				return false;
			}
			WirelessChannel channel = new WirelessChannel();
			channel.addOwner(player.getName());
			channel.setName(cname);
			WirelessReceiver receiver = new WirelessReceiver();
			receiver.setOwner(player.getName());
			receiver.setWorld(loc.getWorld().getName());
			receiver.setX(loc.getBlockX());
			receiver.setY(loc.getBlockY());
			receiver.setZ(loc.getBlockZ());
			receiver.setDirection(cblock.getData());
			receiver.setisWallSign(isWallSign);
			channel.addReceiver(receiver);
			WirelessRedstone.config.set("WirelessChannels." + cname,channel);
			WirelessRedstone.config.save();
			player.sendMessage(WirelessRedstone.strings.playerCreatedChannel);
			this.UpdateCache();
			return true;
		}
		else
		{
			if(cname.contains("."))
			{
				player.sendMessage(ChatColor.RED + "[WirelessRedstone] It's not recommended to have a channel that the name contains an invalid caracter, you should remove it my friend!");
				return false;
			}
			Object tempobject = WirelessRedstone.config.get("WirelessChannels." + cname);
			if (tempobject instanceof WirelessChannel)
			{
				WirelessChannel channel = (WirelessChannel) tempobject;
				WirelessReceiver receiver = new WirelessReceiver();
				receiver.setOwner(player.getName());
				receiver.setWorld(loc.getWorld().getName());
				receiver.setX(loc.getBlockX());
				receiver.setY(loc.getBlockY());
				receiver.setZ(loc.getBlockZ());
				receiver.setDirection(cblock.getData());
				receiver.setisWallSign(isWallSign);
				channel.addReceiver(receiver);
				WirelessRedstone.config.set("WirelessChannels." + cname, channel);
				WirelessRedstone.config.save();
				player.sendMessage(WirelessRedstone.strings.playerExtendedChannel);
				this.UpdateCache();
				return true;
			}
		}
		return false;
	}
	
	public boolean addWirelessTransmitter(String cname, Block cblock, Player player)
	{
		Location loc = cblock.getLocation();
		Boolean isWallSign = false;
		if (cblock.getType() == Material.WALL_SIGN) {
			isWallSign = true;
		}
		
		WirelessChannel channel = WirelessRedstone.config.getWirelessChannel(cname);
		if (channel == null)
		{
			if(cname.contains("."))
			{
				player.sendMessage(WirelessRedstone.strings.playerCannotCreateChannel + " : Name contains invalid caracters !");
				return false;
			}
			channel = new WirelessChannel();
			channel.addOwner(player.getName());
			channel.setName(cname);
			WirelessTransmitter transmitter = new WirelessTransmitter();
			transmitter.setOwner(player.getName());
			transmitter.setWorld(loc.getWorld().getName());
			transmitter.setX(loc.getBlockX());
			transmitter.setY(loc.getBlockY());
			transmitter.setZ(loc.getBlockZ());
			transmitter.setDirection(cblock.getData());
			transmitter.setisWallSign(isWallSign);
			channel.addTransmitter(transmitter);
			WirelessRedstone.config.setWirelessChannel(cname, channel);
			WirelessRedstone.config.save();
			player.sendMessage(WirelessRedstone.strings.playerCreatedChannel);
			this.UpdateCache();
			return true;
		}
		else
		{
			if(cname.contains("."))
			{
				player.sendMessage(ChatColor.RED + "[WirelessRedstone] It's not recommended to have a channel that the name contains an invalid caracter, you should destroy it my friend!");
				return false;
			}
			WirelessTransmitter transmitter = new WirelessTransmitter();
			transmitter.setOwner(player.getName());
			transmitter.setWorld(loc.getWorld().getName());
			transmitter.setX(loc.getBlockX());
			transmitter.setY(loc.getBlockY());
			transmitter.setZ(loc.getBlockZ());
			transmitter.setDirection(cblock.getData());
			transmitter.setisWallSign(isWallSign);
			channel.addTransmitter(transmitter);
			WirelessRedstone.config.setWirelessChannel(cname, channel);
			WirelessRedstone.config.save();
			player.sendMessage(WirelessRedstone.strings.playerExtendedChannel);
			this.UpdateCache();
			return true;
		}
	}
	
	public boolean addWirelessScreen(String cname, Block cblock, Player player)
	{
		Location loc = cblock.getLocation();
		Boolean isWallSign = false;
		if (cblock.getType() == Material.WALL_SIGN)
		{
			isWallSign = true;
		}
		if (WirelessRedstone.config.get("WirelessChannels." + cname) == null)
		{
			if(cname.contains("."))
			{
				player.sendMessage(WirelessRedstone.strings.playerCannotCreateChannel + " : Name contains invalid caracters !");
				return false;
			}
			WirelessChannel channel = new WirelessChannel();
			channel.addOwner(player.getName());
			channel.setName(cname);
			WirelessScreen screen = new WirelessScreen();
			screen.setOwner(player.getName());
			screen.setWorld(loc.getWorld().getName());
			screen.setX(loc.getBlockX());
			screen.setY(loc.getBlockY());
			screen.setZ(loc.getBlockZ());
			screen.setDirection(cblock.getData());
			screen.setisWallSign(isWallSign);
			channel.addScreen(screen);
			WirelessRedstone.config.set("WirelessChannels." + cname,channel);
			WirelessRedstone.config.save();
			player.sendMessage(WirelessRedstone.strings.playerCreatedChannel);
			this.UpdateCache();
			return true;
		}
		else
		{
			if(cname.contains("."))
			{
				player.sendMessage(ChatColor.RED + "[WirelessRedstone] It's not recommended to have a channel that the name contains an invalid caracter, you should destroy it my friend!");
				return false;
			}
			Object tempobject = WirelessRedstone.config.get("WirelessChannels." + cname);
			if (tempobject instanceof WirelessChannel)
			{
				WirelessChannel channel = (WirelessChannel) tempobject;
				WirelessScreen screen = new WirelessScreen();
				screen.setOwner(player.getName());
				screen.setWorld(loc.getWorld().getName());
				screen.setX(loc.getBlockX());
				screen.setY(loc.getBlockY());
				screen.setZ(loc.getBlockZ());
				screen.setDirection(cblock.getData());
				screen.setisWallSign(isWallSign);
				channel.addScreen(screen);
				WirelessRedstone.config.set("WirelessChannels." + cname, channel);
				WirelessRedstone.config.save();
				player.sendMessage(WirelessRedstone.strings.playerExtendedChannel);
				this.UpdateCache();
				return true;
			}
		}
		return false;
	}
	
	public boolean isValidWallLocation(Block block)
	{
		BlockFace face = BlockFace.DOWN;
		switch(block.getData())
		{
		case 0x2: //South
			face = BlockFace.WEST;
			break;
			
		case 0x3: //North
			face = BlockFace.EAST;
			break;
			
		case 0x4: //east
			face = BlockFace.SOUTH;
			break;
			
		case 0x5: //West
			face = BlockFace.NORTH;
			break;
		}
		Block tempBlock = block.getRelative(face);
		
		if (tempBlock.getType() == Material.AIR
				|| tempBlock.getType() == Material.PISTON_BASE
				|| tempBlock.getType() == Material.PISTON_EXTENSION
				|| tempBlock.getType() == Material.PISTON_MOVING_PIECE
				|| tempBlock.getType() == Material.PISTON_STICKY_BASE
				|| tempBlock.getType() == Material.GLOWSTONE
				|| tempBlock.getType() == Material.REDSTONE_LAMP_ON
				|| tempBlock.getType() == Material.REDSTONE_LAMP_OFF)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	public boolean isValidLocation(Block block)
	{
		if(block == null)
			return false;
		
		Block tempBlock = block.getRelative(BlockFace.DOWN);
		
		if (tempBlock.getType() == Material.AIR
				|| tempBlock.getType() == Material.PISTON_BASE
				|| tempBlock.getType() == Material.PISTON_EXTENSION
				|| tempBlock.getType() == Material.PISTON_MOVING_PIECE
				|| tempBlock.getType() == Material.PISTON_STICKY_BASE
				|| tempBlock.getType() == Material.GLOWSTONE
				|| tempBlock.getType() == Material.REDSTONE_LAMP_ON
				|| tempBlock.getType() == Material.REDSTONE_LAMP_OFF)
		{
			return false;
		}
		else
			return true;
	}

	public ArrayList<Location> getReceiverLocations(WirelessChannel channel)
	{
		ArrayList<Location> returnlist = new ArrayList<Location>();
		for (WirelessReceiver receiver : channel.getReceivers())
		{
			returnlist.add(this.getPointLocation(receiver));
		}
		return returnlist;
	}
	
	public ArrayList<Location> getReceiverLocations(String channelname)
	{
		WirelessChannel channel = this.plugin.WireBox.getChannel(channelname);
		if(channel == null)
			return new ArrayList<Location>();
		
		return getReceiverLocations(channel);
	}
	
	public ArrayList<Location> getScreenLocations(WirelessChannel channel)
	{
		ArrayList<Location> returnlist = new ArrayList<Location>();
		for(WirelessScreen screen : channel.getScreens())
		{
			returnlist.add(this.getPointLocation(screen));
		}
		return returnlist;
	}
	
	public ArrayList<Location> getScreenLocations(String channelname)
	{
		WirelessChannel channel = this.plugin.WireBox.getChannel(channelname);
		if(channel == null)
			return new ArrayList<Location>();
		
		return getScreenLocations(channel);
	}
	
	public boolean isActive(WirelessChannel channel)
	{
		for(WirelessTransmitter transmitter : channel.getTransmitters())
		{
			Location tempLoc = new Location(plugin.getServer().getWorld(transmitter.getWorld()),
					transmitter.getX(),
					transmitter.getY(),
					transmitter.getZ());
			if(tempLoc.getBlock().isBlockIndirectlyPowered() || tempLoc.getBlock().isBlockPowered())
			{
				return true;
			}
		}
		return false;
	}

	public void removeReceiverAt(final Location loc, final boolean byplayer)
	{
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable()
		{
			public void run()
			{
				for (WirelessChannel channel : getChannels())
				{
					for (WirelessReceiver receiver : channel.getReceivers())
					{
						if (receiver.getX() == loc.getBlockX()
								&& receiver.getY() == loc.getBlockY()
								&& receiver.getZ() == loc.getBlockZ())
						{
							channel.removeReceiverAt(loc);
							SaveChannel(channel);
							if (!byplayer)
							{
								for (String owner : channel.getOwners())
								{
									try
									{
										if (plugin.getServer().getPlayer(owner).isOnline())
										{
											plugin.getServer().getPlayer(owner).sendMessage("One of your signs on channel: "
												    + channel.getName()
													+ " is broken by nature.");
										}
									}
									catch (Exception ex)
									{
										// NA
									}
								}
							}
							return;
						}
					}
				}
			}
		});
	}

	public boolean removeWirelessReceiver(String cname, Location loc)
	{
		WirelessChannel channel = WirelessRedstone.config.getWirelessChannel(cname);
		if(channel != null)
		{
			channel.removeReceiverAt(loc);
			WirelessRedstone.config.setWirelessChannel(cname, channel);
			WirelessRedstone.config.save();
			this.UpdateCache();
			return true;
		}
		return true;
	}

	public boolean removeWirelessTransmitter(String cname, Location loc)
	{
		WirelessChannel channel = WirelessRedstone.config.getWirelessChannel(cname);
		if (channel != null)
		{
			channel.removeTransmitterAt(loc);
			WirelessRedstone.config.setWirelessChannel(cname, channel);
			WirelessRedstone.config.save();
			this.UpdateCache();
			return true;
		}
		return true;
	}
	
	public boolean removeWirelessScreen(String cname, Location loc)
	{
		WirelessChannel channel = WirelessRedstone.config.getWirelessChannel(cname);
		if (channel != null)
		{
			channel.removeScreenAt(loc);
			WirelessRedstone.config.setWirelessChannel(cname, channel);
			WirelessRedstone.config.save();
			this.UpdateCache();
			return true;
		}
		return true;
	}

	public boolean removeChannel(String cname)
	{
		WirelessChannel channel = WirelessRedstone.config.getWirelessChannel(cname);
		if (channel != null)
		{
			this.removeSigns(channel);
			WirelessRedstone.config.setWirelessChannel(cname, null);
			WirelessRedstone.config.save();
			this.UpdateCache();
			return true;
		}
		return true;
	}

	public Collection<WirelessChannel> getChannels()
	{
		return WirelessRedstone.config.getAllChannels();
	}

	private void removeSigns(WirelessChannel channel)
	{
		try
		{
			for (IWirelessPoint point : channel.getReceivers())
			{
				this.getPointLocation(point).getBlock().setType(Material.AIR);
			}
		}
		catch(NullPointerException ex)
		{
			//When there isn't any receiver, it'll throw this exception.
		}

		try
		{
			for (IWirelessPoint point : channel.getTransmitters())
			{
				this.getPointLocation(point).getBlock().setType(Material.AIR);
			}
		}
		catch(NullPointerException ex)
		{
			//When there isn't any transmitter, it'll throw this exception.
		}
	}

	public boolean containsChannel(String name)
	{
		return (WirelessRedstone.config.getWirelessChannel(name) != null);
	}

	public List<IWirelessPoint> getAllSigns()
	{
		return allPointsListCache;
	}

	public void UpdateAllSignsList()
	{
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable()
		{
			public void run()
			{
				ArrayList<IWirelessPoint> returnlist = new ArrayList<IWirelessPoint>();
				for (WirelessChannel channel : WirelessRedstone.config.getAllChannels())
				{
					try
					{
						for (IWirelessPoint point : channel.getReceivers())
						{
							returnlist.add(point);
						}

						for (IWirelessPoint point : channel.getTransmitters())
						{
							returnlist.add(point);
						}
						
						for (IWirelessPoint point : channel.getScreens())
						{
							returnlist.add(point);
						}
					}
					catch (Exception e)
					{

					}
				}
				allPointsListCache = returnlist;
			}
		}, 0L);
	}

	public List<Location> getAllReceiverLocations()
	{
		return receiverlistcachelocation;
	}

	public void UpdateReceiverLocations()
	{
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable()
		{
			public void run()
			{
				List<Location> returnlist = new ArrayList<Location>();
				for (WirelessChannel channel : WirelessRedstone.config.getAllChannels())
				{
					try
					{
						for (WirelessReceiver point : channel.getReceivers())
						{
							Location floc = getPointLocation(point);
							returnlist.add(floc);
						}
					}
					catch (Exception e)
					{
						
					}
				}
				receiverlistcachelocation = returnlist;
			}
		}, 0L);
	}

	public void UpdateCache()
	{
		UpdateReceiverLocations();
		UpdateAllSignsList();
	}

	public Location getPointLocation(IWirelessPoint point)
	{
		return new Location(plugin.getServer().getWorld(point.getWorld()),
				point.getX(), point.getY(), point.getZ());
	}

	public void UpdateChacheNoThread()
	{
		ArrayList<Location> returnlist = new ArrayList<Location>();
		for (WirelessChannel channel : WirelessRedstone.config.getAllChannels())
		{
			try
			{
				for (WirelessReceiver point : channel.getReceivers())
				{
					Location floc = getPointLocation(point);
					returnlist.add(floc);
				}
			}
			catch (Exception e)
			{
				
			}
		}

		receiverlistcachelocation = returnlist;

		ArrayList<IWirelessPoint> returnlist2 = new ArrayList<IWirelessPoint>();
		for (WirelessChannel channel : WirelessRedstone.config.getAllChannels()) {
			for (IWirelessPoint point : channel.getReceivers()) {
				returnlist2.add(point);
			}

			for (IWirelessPoint point : channel.getTransmitters()) {
				returnlist2.add(point);
			}
		}
		allPointsListCache = returnlist2;
	}

	public void signWarning(Block block, int code)
	{
		Sign sign = (Sign) block.getState();
		switch(code)
		{
		case 1:
			sign.setLine(2, "Bad block");
			sign.setLine(3, "Behind sign");
			sign.update();
			break;
			
		default:
			break;
		}
	}
}