package com.blackypaw.mc.i18n;

import com.blackypaw.mc.i18n.event.PlayerSetLanguageEvent;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.Locale;

public class PlayerSettingsListener implements PacketListener
{
    public PlayerSettingsListener(I18NSpigotAdapter plugin, I18NSpigotImpl i18n)
    {
        this.plugin = plugin;
        this.i18n = i18n;
    }

    public Plugin getPlugin() { return plugin; }
    public ListeningWhitelist getReceivingWhitelist()
    {
        return ListeningWhitelist.newBuilder()
                .gamePhase(GamePhase.PLAYING).highest()
                .types(PacketType.Play.Client.SETTINGS)
                .build();
    }
    public ListeningWhitelist getSendingWhitelist()
    {
        return ListeningWhitelist.newBuilder()
                .gamePhase(GamePhase.PLAYING).highest()
                .types(PacketType.Play.Client.SETTINGS)
                .build();
    }

    @Override
    public void onPacketReceiving(PacketEvent e) {
        if (e.getPacket().getType() != PacketType.Play.Client.SETTINGS) {
            return;
        }

        Player player = e.getPlayer();
        Object clientInfo = e.getPacket().getModifier().read(0);

        String mctag = "en_US"; // usually is en_US
        try {
            Field languageField = clientInfo.getClass().getDeclaredField("language"); // might also be "a"
            languageField.setAccessible(true);
            mctag = (String) languageField.get(clientInfo);
        } catch (Exception ex) {
            plugin.getLogger().warning("Failed to identify client's language by settings packet. Plugin is probably incompatible with the server API version. Since it is forked, only Figishe could fix this.");
            ex.printStackTrace();
            return;
        }

        plugin.getLogger().info(player.getName() + "'s language is now " + mctag);
        Locale locale = getPlayerLocaleByMcTag(mctag);
        Locale prevLocale = i18n.getLocale( player.getUniqueId() );
        i18n.storeLocale( player.getUniqueId(), locale );

        // Call a player set language event so that other plugins can resend
        // scoreboards, signs, etc.:
        PlayerSetLanguageEvent event = new PlayerSetLanguageEvent( player, prevLocale, locale );
        Bukkit.getServer().getScheduler().runTask(plugin,
                () -> Bukkit.getServer().getPluginManager().callEvent( event )
        );
    }

    private static Locale getPlayerLocaleByMcTag(String mctag)
    {
        int separatorPos = mctag.indexOf('_');
        if (separatorPos < 0)
        {
            return Locale.forLanguageTag(mctag);
        }

        Locale locale = Locale.forLanguageTag(mctag.substring(0, separatorPos));
        return locale;
    }

    public void onPacketSending(PacketEvent e) {}

    private I18NSpigotAdapter plugin;
    private I18NSpigotImpl i18n;

}
