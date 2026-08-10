package org.core.coreSystem.cores.VOL5.Scout.Skill;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.core.cool.Cool;
import org.core.coreSystem.absCoreSystem.SkillBase;
import org.core.coreSystem.cores.VOL5.Scout.coreSystem.Scout;

import java.time.Duration;
import java.util.UUID;

public class F implements SkillBase {
    private final Scout config;
    private final JavaPlugin plugin;
    private final Cool cool;

    public F(Scout config, JavaPlugin plugin, Cool cool) {
        this.config = config;
        this.plugin = plugin;
        this.cool = cool;
    }

    private void sendSubTitle(Player player, String msg, NamedTextColor color) {
        Title title = Title.title(
                Component.empty(),
                Component.text(msg).color(color),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(300), Duration.ofMillis(200))
        );
        player.showTitle(title);
    }

    @Override
    public void Trigger(Player player) {
        UUID uuid = player.getUniqueId();

        if (config.isOverclocked.getOrDefault(uuid, false)) {
            return;
        }

        config.isOverclocked.put(uuid, true);

        World world = player.getWorld();
        Location loc = player.getLocation();

        world.playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 2.0f);
        world.playSound(loc, Sound.ENTITY_WARDEN_SONIC_CHARGE, 1.0f, 1.5f);

        world.spawnParticle(Particle.SONIC_BOOM, loc.add(0, 1, 0), 1);
        world.spawnParticle(Particle.ELECTRIC_SPARK, loc, 50, 0.5, 1.0, 0.5, 0.1);
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 30, 0.5, 1.0, 0.5, 0.05);

        sendSubTitle(player, "Overclock Activated", NamedTextColor.AQUA);

        long durationMs = 10000L;
        long durationTicks = 200L;

        cool.setCooldown(player, durationMs, "OVERCLOCK", "boss");

        cool.updateCooldown(player, "R", 0L);
        cool.updateCooldown(player, "Q", 0L);

        new BukkitRunnable() {
            long ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead() || !config.isOverclocked.getOrDefault(uuid, false)) {
                    config.isOverclocked.put(uuid, false);
                    cool.setCooldown(player, 0L, "OVERCLOCK");
                    this.cancel();
                    return;
                }

                if (ticks >= durationTicks) {
                    config.isOverclocked.put(uuid, false);
                    world.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.5f, 1.5f);
                    sendSubTitle(player, "Overclock Ended", NamedTextColor.GRAY);
                    this.cancel();
                    return;
                }

                if (ticks % 5 == 0) {
                    world.spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0, 1, 0), 3, 0.4, 0.5, 0.4, 0.01);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}