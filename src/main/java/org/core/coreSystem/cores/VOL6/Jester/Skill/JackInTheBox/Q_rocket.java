package org.core.coreSystem.cores.VOL6.Jester.Skill.JackInTheBox;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.core.cool.Cool;
import org.core.coreSystem.absCoreSystem.SkillBase;
import org.core.coreSystem.cores.VOL6.Jester.coreSystem.Jester;
import org.core.effect.crowdControl.ForceDamage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class Q_rocket implements SkillBase {

    private final Jester config;
    private final JavaPlugin plugin;
    private final Cool cool;
    private final Consumer<Player> onSkipStage;
    private final NamespacedKey keyQ;

    private final Map<UUID, Integer> comboCount = new HashMap<>();
    private final Map<UUID, Long> lastCastTime = new HashMap<>();

    public Q_rocket(Jester config, JavaPlugin plugin, Cool cool, Consumer<Player> onSkipStage) {
        this.config = config;
        this.plugin = plugin;
        this.cool = cool;
        this.onSkipStage = onSkipStage;
        this.keyQ = new NamespacedKey(plugin, "Q");
    }

    @Override
    public void Trigger(Player player) {
        UUID uuid = player.getUniqueId();

        if (cool.isReloading(player, "Rocket_reuse")) {
            return;
        }
        cool.setCooldown(player, 600L, "Rocket_reuse");

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCastTime.getOrDefault(uuid, 0L) > 6500L) {
            comboCount.put(uuid, 0);
        }
        lastCastTime.put(uuid, currentTime);

        long level = player.getPersistentDataContainer().getOrDefault(keyQ, PersistentDataType.LONG, 0L);
        double ampMultiplier = 1.0 + (config.q_Skill_amp * level);
        double damage = config.q_Skill_damage * ampMultiplier;

        DamageSource source = DamageSource.builder(DamageType.PLAYER_EXPLOSION)
                .withCausingEntity(player)
                .withDirectEntity(player)
                .build();

        int count = comboCount.getOrDefault(uuid, 0);
        World world = player.getWorld();
        Location playerLoc = player.getLocation().clone();

        if (count < 3) {
            comboCount.put(uuid, count + 1);

            world.spawnParticle(Particle.EXPLOSION, playerLoc.clone().add(0, 1, 0), 4, 0.3, 0.3, 0.3, 1);
            world.playSound(playerLoc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);
            world.playSound(playerLoc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.0f);

            for (Entity entity : world.getNearbyEntities(playerLoc, 3.5, 3.5, 3.5)) {
                if (entity.equals(player) || !(entity instanceof LivingEntity)) continue;

                world.spawnParticle(Particle.EXPLOSION, entity.getLocation().clone().add(0, 1, 0), 1, 0, 0, 0, 0);

                ForceDamage forceDamage = new ForceDamage((LivingEntity) entity, damage, source, false);
                forceDamage.applyEffect(player);

                Vector direction = entity.getLocation().toVector().subtract(playerLoc.toVector()).normalize().multiply(1.0);
                direction.setY(0.6);
                entity.setVelocity(direction);
            }

            Vector upward = new Vector(0, 1.1, 0);
            player.setVelocity(upward);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.getPersistentDataContainer().set(new NamespacedKey(plugin, "noFallDamage"), PersistentDataType.BOOLEAN, true);
            }, 1L);

            if (count == 0) {
                new BukkitRunnable() {
                    int delayTicks = 0;

                    @Override
                    public void run() {
                        if (!player.isOnline() || player.isDead() || !comboCount.containsKey(uuid)) {
                            comboCount.remove(uuid);
                            this.cancel();
                            return;
                        }

                        if (player.getInventory().getItemInMainHand().getType() != Material.FIREWORK_ROCKET) {
                            comboCount.remove(uuid);
                            this.cancel();
                            return;
                        }

                        delayTicks++;
                        if (delayTicks > 12) {
                            Block blockBelow = player.getLocation().subtract(0, 0.1, 0).getBlock();
                            if (blockBelow.getType().isSolid() || ((Entity) player).isOnGround()) {
                                comboCount.put(uuid, 3);
                                this.cancel();
                            }
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }
        } else {
            comboCount.remove(uuid);

            world.playSound(playerLoc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.5f, 1.0f);
            world.playSound(playerLoc, Sound.ITEM_CROSSBOW_SHOOT, 1.2f, 1.0f);

            Vector recoil = player.getLocation().getDirection().setY(0);
            if (recoil.lengthSquared() > 0) {
                recoil.normalize().multiply(-0.6);
            } else {
                recoil = new Vector(0, 0, 0);
            }
            recoil.setY(0.4);
            player.setVelocity(recoil);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.getPersistentDataContainer().set(new NamespacedKey(plugin, "noFallDamage"), PersistentDataType.BOOLEAN, true);
            }, 1L);

            Location startLoc = player.getEyeLocation();
            Vector projectileDir = player.getLocation().getDirection().normalize().multiply(2.0);

            Firework firework = world.spawn(startLoc, Firework.class);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder()
                    .withColor(Color.RED, Color.ORANGE)
                    .with(FireworkEffect.Type.BURST)
                    .build());
            meta.setPower(2);
            firework.setFireworkMeta(meta);
            firework.setShotAtAngle(true);
            firework.setVelocity(projectileDir);
            firework.setShooter(player);

            new BukkitRunnable() {
                int distance = 0;

                @Override
                public void run() {
                    if (firework.isDead() || distance > 35 || !player.isOnline()) {
                        if (!firework.isDead()) {
                            detonate(firework.getLocation());
                        }
                        this.cancel();
                        return;
                    }

                    Location currentLoc = firework.getLocation();

                    if (currentLoc.getBlock().getType().isSolid()) {
                        detonate(currentLoc);
                        return;
                    }

                    for (Entity e : world.getNearbyEntities(currentLoc, 1.5, 1.5, 1.5)) {
                        if (e instanceof LivingEntity && e != player && e != firework) {
                            detonate(currentLoc);
                            return;
                        }
                    }

                    distance++;
                }

                private void detonate(Location loc) {
                    firework.remove();

                    world.playSound(loc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 2.0f, 1.0f);
                    world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);

                    world.spawnParticle(Particle.EXPLOSION, loc.clone().add(0, 0.6, 0), 3, 0.3, 0.3, 0.3, 1.0);
                    world.spawnParticle(Particle.FLAME, loc.clone().add(0, 0.6, 0), 44, 0.1, 0.1, 0.1, 0.8);
                    world.spawnParticle(Particle.SMOKE, loc.clone().add(0, 0.6, 0), 44, 0.1, 0.1, 0.1, 0.8);

                    for (Entity e : world.getNearbyEntities(loc, 4.5, 4.5, 4.5)) {
                        if (e instanceof LivingEntity target && target != player) {
                            ForceDamage fd = new ForceDamage(target, damage * 6, source, true);
                            fd.applyEffect(player);

                            Vector direction = target.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(1.4);
                            direction.setY(0.8);
                            target.setVelocity(direction);
                        }
                    }
                    this.cancel();
                }
            }.runTaskTimer(plugin, 0L, 1L);

            onSkipStage.accept(player);
        }
    }
}