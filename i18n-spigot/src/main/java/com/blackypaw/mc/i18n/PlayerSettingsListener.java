package com.blackypaw.mc.i18n;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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
        String mctag = e.getPacket().getStrings().readSafely(0); // usually is en_US

        plugin.getLogger().info(player.getName() + "'s language is now " + mctag);
        Locale locale = getPlayerLocaleByMcTag(mctag);
        i18n.storeLocale( player.getUniqueId(), locale );
    }

    private static Locale getPlayerLocaleByMcTag(String mctag)
    {
        int separatorPos = mctag.indexOf('_');
        String bcp47tag = mctag.substring(0, separatorPos) + '-'
                + mctag.substring(separatorPos + 1).toUpperCase(Locale.ROOT);

        Locale locale = Locale.forLanguageTag(bcp47tag);
        return locale;
    }

    public void onPacketSending(PacketEvent e) {}

    private I18NSpigotAdapter plugin;
    private I18NSpigotImpl i18n;

}