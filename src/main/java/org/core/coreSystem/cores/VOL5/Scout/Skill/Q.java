package org.core.coreSystem.cores.VOL5.Scout.Skill;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.core.cool.Cool;
import org.core.coreSystem.absCoreSystem.SkillBase;
import org.core.coreSystem.cores.VOL5.Scout.coreSystem.Scout;
import org.core.effect.crowdControl.Invulnerable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Q implements SkillBase {
    private final Scout config;
    private final JavaPlugin plugin;
    private final Cool cool;

    public Q(Scout config, JavaPlugin plugin, Cool cool) {
        this.config = config;
        this.plugin = plugin;
        this.cool = cool;
    }

    @Override
    public void Trigger(Player player) {
        UUID uuid = player.getUniqueId();

        boolean isOverclocked = config.isOverclocked.getOrDefault(uuid, false);
        long cooldown = isOverclocked ? 2000L : config.q_Skill_Cool;
        cool.setCooldown(player, cooldown, "Q");

        World world = player.getWorld();
        Location startLoc = player.getLocation().clone();
        Vector direction = startLoc.getDirection().normalize();

        world.playSound(startLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);
        world.playSound(startLoc, Sound.ITEM_TRIDENT_RIPTIDE_1, 1.0f, 1.2f);

        Vector dashVec = direction.clone().multiply(1.6);
        player.setVelocity(dashVec);

        Invulnerable invulnerable = new Invulnerable(player, 600);
        invulnerable.applyEffect(player);

        NamespacedKey qKey = new NamespacedKey(plugin, "Q");
        long level = player.getPersistentDataContainer().getOrDefault(qKey, PersistentDataType.LONG, 0L);
        double qAmp = config.q_Skill_amp * level;
        double healAmount = 1.0 + qAmp;

        new BukkitRunnable() {
            int ticks = 0;
            final int maxDuration = 60;
            final Set<Location> pathPoints = new HashSet<>();

            @Override
            public void run() {
                if (ticks < 5) {
                    pathPoints.add(player.getLocation().clone().add(0, 1, 0));
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, player.getLocation().add(0, 1, 0), 3, 0.1, 0.1, 0.1, 0.05);
                }

                if (ticks < maxDuration) {
                    boolean healedThisTick = false;

                    for (Location loc : pathPoints) {
                        if (ticks % 3 == 0) {
                            world.spawnParticle(Particle.REVERSE_PORTAL, loc, 2, 0.3, 0.5, 0.3, 0.02);
                        }

                        for (Entity e : world.getNearbyEntities(loc, 1.5, 1.5, 1.5)) {
                            if (e instanceof LivingEntity target && !target.isDead() && !(target instanceof org.bukkit.entity.ArmorStand) && !target.hasMetadata("NPC")) {

                                if (target.equals(player)) {
                                    if (ticks % 10 == 0 && !healedThisTick) {
                                        AttributeInstance maxHp = player.getAttribute(Attribute.MAX_HEALTH);
                                        if (maxHp != null) {
                                            double newHp = Math.min(maxHp.getValue(), player.getHealth() + healAmount);
                                            player.setHealth(newHp);

                                            world.spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 2, 0.4, 0.4, 0.4, 0);
                                        }
                                        healedThisTick = true;
                                    }
                                }
                                else {
                                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1, false, false, false));
                                    target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0, false, false, false));

                                    if (ticks % 5 == 0) {
                                        world.playSound(target.getLocation(), Sound.BLOCK_SCULK_SENSOR_CLICKING, 0.5f, 2.0f);
                                    }
                                }
                            }
                        }
                    }
                    ticks++;
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}