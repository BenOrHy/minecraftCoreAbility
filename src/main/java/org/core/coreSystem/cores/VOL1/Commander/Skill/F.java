package org.core.coreSystem.cores.VOL1.Commander.Skill;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.core.cool.Cool;
import org.core.effect.crowdControl.ForceDamage;
import org.core.coreSystem.absCoreSystem.SkillBase;
import org.core.coreSystem.cores.VOL1.Commander.coreSystem.Commander;

import java.time.Duration;
import java.util.HashSet;

public class F implements SkillBase {

    private final Commander config;
    private final JavaPlugin plugin;
    private final Cool cool;

    public F(Commander config, JavaPlugin plugin, Cool cool) {
        this.config = config;
        this.plugin = plugin;
        this.cool = cool;
    }

    @Override
    public void Trigger(Player player){
        if(!config.comBlocks.getOrDefault(player.getUniqueId(), new HashSet<>()).isEmpty()){

            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 0.8f, 1.5f);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.5f);

            for(FallingBlock fb : config.comBlocks.getOrDefault(player.getUniqueId(), new HashSet<>())){
                Location center = fb.getLocation().clone().add(0, 0.5, 0);

                player.getWorld().spawnParticle(Particle.SQUID_INK, center, 30, 0.5, 0.5, 0.5, 0.05);
                player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, center, 50, 1.0, 1.0, 1.0, 0.1);

                circleParticle(player, center);
                commandReceiver_1(player, fb);
            }
        }else{
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.5f, 0.8f);

            Title title = Title.title(
                    Component.empty(),
                    Component.text("com-block uninstalled").color(NamedTextColor.RED),
                    Title.Times.times(Duration.ZERO, Duration.ofMillis(300), Duration.ofMillis(200))
            );
            player.showTitle(title);

            long cools = 500L;
            cool.updateCooldown(player, "F", cools);
        }
    }

    public void circleParticle(Player player, Location center){
        double maxRadius = 6.0;
        int maxTicks = 6;

        Particle.DustOptions waveDust = new Particle.DustOptions(Color.fromRGB(138, 43, 226), 1.2f);
        Particle.DustOptions voidDust = new Particle.DustOptions(Color.fromRGB(0, 0, 0), 1.5f);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks > maxTicks) {
                    this.cancel();
                    return;
                }

                double currentRadius = (maxRadius / maxTicks) * ticks;

                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle) * currentRadius;
                    double z = Math.sin(angle) * currentRadius;
                    Location particleLocation = center.clone().add(x, 0, z);

                    player.getWorld().spawnParticle(Particle.DUST, particleLocation, 1, 0, 0, 0, 0, waveDust);

                    if (Math.random() < 0.2) {
                        player.getWorld().spawnParticle(Particle.DUST, particleLocation, 1, 0.1, 0.1, 0.1, 0, voidDust);
                    }
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void commandReceiver_1(Player player, FallingBlock fb) {
        World world = player.getWorld();
        Location center = fb.getLocation();

        for (Entity entity : world.getNearbyEntities(center, 6, 6, 6)) {
            if (entity.equals(player) || !(entity instanceof LivingEntity)) continue;

            fb.getWorld().playSound(fb.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.8f, 2.0f);
            fb.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 0.5f);

            Location start = fb.getLocation().clone().add(0, 0.5, 0);
            Vector dir = entity.getLocation().clone().add(0, 1.2, 0).toVector().subtract(start.toVector()).normalize();
            double maxDistance = start.distance(entity.getLocation().clone().add(0, 1, 0));

            attackLine(player, maxDistance, start, dir);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (world.getNearbyEntities(center, 1, 1, 1).contains(entity) || !fb.isValid()) {
                        if(fb.isValid()) {
                            commandReceiver_2(player, center, entity);
                        }
                        this.cancel();
                    } else {
                        Vector direction = center.toVector().subtract(entity.getLocation().toVector()).normalize().multiply(1.0);
                        entity.setVelocity(direction);

                        if (Math.random() < 0.3) {
                            world.spawnParticle(Particle.REVERSE_PORTAL, entity.getLocation().add(0, 1, 0), 5, 0.3, 0.5, 0.3, 0.05);
                        }
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    }

    public void commandReceiver_2(Player player, Location center, Entity entity) {
        World world = player.getWorld();

        double amp = config.f_Skill_amp * player.getPersistentDataContainer().getOrDefault(new NamespacedKey(plugin, "F"), PersistentDataType.LONG, 0L);
        double damage = config.f_Skill_Damage * (1 + amp);

        world.spawnParticle(Particle.EXPLOSION, center, 3, 0.5, 0.5, 0.5, 1.0);
        world.spawnParticle(Particle.SQUID_INK, center, 50, 1.0, 1.0, 1.0, 0.1);
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.1f, 0.8f);
        world.playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.6f, 1.2f);

        DamageSource source = DamageSource.builder(DamageType.MAGIC)
                .withCausingEntity(player)
                .withDirectEntity(player)
                .build();

        ForceDamage forceDamage = new ForceDamage((LivingEntity) entity, damage, source, false);
        forceDamage.applyEffect(player);
    }

    public void attackLine(Player player, double maxDistance, Location start, Vector direction){
        double step = 0.2;

        Particle.DustOptions coreDust = new Particle.DustOptions(Color.fromRGB(200, 100, 255), 0.6f);
        Particle.DustOptions outerDust = new Particle.DustOptions(Color.fromRGB(100, 0, 255), 0.8f);

        for (double i = 0; i <= maxDistance; i += step) {
            Location point = start.clone().add(direction.clone().multiply(i));

            player.spawnParticle(Particle.DUST, point, 1, 0.02, 0.02, 0.02, 0, coreDust);
            player.spawnParticle(Particle.DUST, point, 1, 0.05, 0.05, 0.05, 0, outerDust);

            if (Math.random() < 0.15) {
                player.spawnParticle(Particle.WITCH, point, 1, 0.1, 0.1, 0.1, 0);
            }
        }
    }
}