package me.olddragon.seenem;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import mkremins.fanciful.FancyMessage;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SeenEm extends JavaPlugin {
  private SimpleDateFormat date_format;
  private String session_url;

  public boolean onCommand(CommandSender sender, Command command, String label,
    String[] args) {
    String cmd = command.getName().toLowerCase();

    if (cmd.equalsIgnoreCase("seenem")) {
      if (args.length == 0) {
        sender.sendMessage(ChatColor.GOLD + "Seen'Em Commands");
        sender.sendMessage(ChatColor.BLUE + "/seenem [player] - See when a player was first/last on.");
        sender.sendMessage(ChatColor.BLUE + "/seenem-search [partial] - Search for a player by partial name.");
        sender.sendMessage(ChatColor.BLUE + "/seenem-rsearch [regex] - Search for a player by regular expression.");
      }
    } else if ((cmd.equalsIgnoreCase("seenem-search")) && (args.length == 1))
      showPlayersInfo(sender, findPlayers(args[0]));
    else if ((cmd.equalsIgnoreCase("seenem-rsearch")) && (args.length == 1))
      showPlayersInfo(sender, findPlayersRegex(args[0]));
    else {
      return false;
    }

    return true;
  }

  protected String formatDate(long time) {
    return this.date_format.format(new Date(time));
  }

  protected String diffDate(long start, long end) {
    long seconds = (end - start) / 1000L;
    long[] diff = new long[4];
    if ((start != 0L) && (end != 0L)) {
      diff[3] = seconds % 60L;
      seconds /= 60L;
      diff[2] = seconds % 60L;
      seconds /= 60L;
      diff[1] = seconds % 24L;
      diff[0] = seconds / 24L;
    }
    return String.format(
      "%d day%s, %d hour%s, %d minute%s, %d second%s",
      new Object[] { Long.valueOf(diff[0]), diff[0] > 1L ? "s" : "",
        Long.valueOf(diff[1]), diff[1] > 1L ? "s" : "",
        Long.valueOf(diff[2]), diff[2] > 1L ? "s" : "",
        Long.valueOf(diff[3]), diff[3] > 1L ? "s" : ""
    });
  }
  
  private List<OfflinePlayer> getPlayers() {
    return Arrays.asList(getServer().getOfflinePlayers());
  }

  private List<OfflinePlayer> findPlayersRegex(String regex) {
    List<OfflinePlayer> players = getPlayers();
    List<OfflinePlayer> results = new LinkedList<>();
    Pattern p = Pattern.compile(regex, 2);
    for (OfflinePlayer player : players) {
      if (p.matcher(player.getName()).matches()) {
        results.add(player);
      }
    }
    return results;
  }

  private List<OfflinePlayer> findPlayers(String name) {
    List<OfflinePlayer> players = Arrays.asList(getServer().getOfflinePlayers());
    List<OfflinePlayer> results = new LinkedList<>();
    name = name.toLowerCase();
    for (OfflinePlayer player : players) {
      if (player.getName().toLowerCase().contains(name)) {
        results.add(player);
      }
    }
    return results;
  }

  protected void showPlayersInfo(CommandSender sender, List<OfflinePlayer> players) {
    for (OfflinePlayer player : players) {
      showPlayerInfo(sender, player);
      sender.sendMessage("");
    }
    if (players.size() == 0) {
      sender.sendMessage("No Matches Found");
    }
  }

  protected void showPlayerInfo(CommandSender sender, OfflinePlayer player) {
    long first = player.getFirstPlayed();
    long last = player.getLastPlayed();
    
    sendMessage(
      sender,
      "Player: %1$s [%2$s] [%3$s]",
      player.getName(),
      player.isOnline() ? "Online" : "Offline",
      player.getUniqueId().toString()
    );
    sender.sendMessage("  First Seen: " + (first == 0L ? "Never" : formatDate(first)));
    sender.sendMessage("  Last Seen: " + (last == 0L ? "Never" : formatDate(last)));
    sender.sendMessage("  Played for: " + diffDate(first, last));
    sender.sendMessage("  Off for: " + diffDate(last, System.currentTimeMillis()));
    
    FancyMessage msg = new FancyMessage("  ")
      .then("Session")
      .link(String.format(session_url, player.getUniqueId().toString().replaceAll("-", "")));
    if (sender instanceof Player) {
      msg.send((Player)sender);
    } else {
      sender.sendMessage(msg.toOldMessageFormat());
    }
  }
  
  public void sendMessage(CommandSender sender, String format, Object...args) {
    sender.sendMessage(String.format(format, args));
  }

  public void onEnable() {
    this.date_format = new SimpleDateFormat(getConfig().getString("DateFormat", "yyyy-MM-dd HH:mm"));
    this.session_url = getConfig().getString("SessionURL", "https://sessionserver.mojang.com/session/minecraft/profile/%1$s");
  }
}