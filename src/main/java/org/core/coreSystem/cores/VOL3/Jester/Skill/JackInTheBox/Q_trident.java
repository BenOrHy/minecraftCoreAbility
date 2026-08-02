package org.core.coreSystem.cores.VOL3.Jester.Skill.JackInTheBox;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.core.cool.Cool;
import org.core.coreSystem.absCoreSystem.SkillBase;
import org.core.coreSystem.cores.VOL3.Jester.coreSystem.Jester;
import org.core.effect.crowdControl.ForceDamage;
import org.core.effect.crowdControl.Invulnerable;

import java.util.List;
import java.util.function.Consumer;

public class Q_trident implements SkillBase {

    private final Jester config;
    private final JavaPlugin plugin;
    private final Cool cool;
    private final Consumer<Player> onSkipStage;
    private final NamespacedKey keyQ;

    public Q_trident(Jester config, JavaPlugin plugin, Cool cool, Consumer<Player> onSkipStage) {
        this.config = config;
        this.plugin = plugin;
        this.cool = cool;
        this.onSkipStage = onSkipStage;
        this.keyQ = new NamespacedKey(plugin, "Q");
    }

    @Override
    public void Trigger(Player player) {
        config.isOmnislashing.put(player.getUniqueId(), true);

        long level = player.getPersistentDataContainer().getOrDefault(keyQ, PersistentDataType.LONG, 0L);
        double ampMultiplier = 1.0 + (config.q_Skill_amp * level);
        double baseDamage = config.q_Skill_damage * ampMultiplier;

        DamageSource source = DamageSource.builder(DamageType.PLAYER_ATTACK)
                .withCausingEntity(player)
                .withDirectEntity(player)
                .build();

        Invulnerable invuln = new Invulnerable(player, 1500L);
        invuln.applyEffect(player);

        Vector strongDash = player.getLocation().getDirection().normalize().multiply(1.6);
        player.setVelocity(strongDash);
        playRiptideEffect(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline() || player.isDead()) {
                invuln.removeEffect(player);
                config.isOmnislashing.put(player.getUniqueId(), false);
                onSkipStage.accept(player);
                return;
            }

            List<LivingEntity> targets = getValidTargets(player, 4.5);
            if (targets.isEmpty()) {
                invuln.removeEffect(player);
                config.isOmnislashing.put(player.getUniqueId(), false);
                onSkipStage.accept(player);
                return;
            }

            new BukkitRunnable() {
                int strikes = 6;
                LivingEntity lastTarget = null;

                @Override
                public void run() {
                    if (!player.isOnline() || player.isDead()) {
                        invuln.removeEffect(player);
                        config.isOmnislashing.put(player.getUniqueId(), false);
                        onSkipStage.accept(player);
                        this.cancel();
                        return;
                    }

                    if (strikes <= 0) {
                        invuln.removeEffect(player);
                        config.isOmnislashing.put(player.getUniqueId(), false);
                        onSkipStage.accept(player);
                        this.cancel();
                        return;
                    }

                    List<LivingEntity> currentTargets = getValidTargets(player, 5.5);
                    if (currentTargets.isEmpty()) {
                        invuln.removeEffect(player);
                        config.isOmnislashing.put(player.getUniqueId(), false);
                        onSkipStage.accept(player);
                        this.cancel();
                        return;
                    }

                    LivingEntity target = getNextTarget(player, currentTargets, lastTarget);

                    Vector dashToTarget = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();

                    double dashPower = (strikes == 1) ? 1.4 : 1.6;
                    player.setVelocity(dashToTarget.multiply(dashPower));

                    boolean isNewTarget = (lastTarget == null) || !target.equals(lastTarget);
                    double finalDamage = isNewTarget ? (baseDamage * 2.0) : baseDamage;

                    ForceDamage fd = new ForceDamage(target, finalDamage, source, true);
                    fd.applyEffect(player);

                    playRiptideEffect(player);
                    player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, target.getLocation().add(0, 1, 0), 1);
                    player.getWorld().playSound(target.getLocation(), Sound.ITEM_TRIDENT_HIT, 1.0f, 1.5f);
                    player.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f);

                    lastTarget = target;
                    strikes--;
                }
            }.runTaskTimer(plugin, 0L, 3L);

        }, 6L);
    }

    private void playRiptideEffect(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_2, 1.0f, 1.2f);

        Location center = player.getLocation().add(0, 1.0, 0);
        Vector dir = player.getLocation().getDirection().normalize();

        Vector p1 = new Vector(-dir.getZ(), 0, dir.getX());
        if (p1.lengthSquared() == 0) p1 = new Vector(1, 0, 0);
        p1.normalize();
        Vector p2 = dir.clone().crossProduct(p1).normalize();

        for (double step = -1.0; step <= 1.5; step += 0.15) {
            double angle = step * Math.PI * 2.0;
            double radius = 0.7;

            Vector offset1 = p1.clone().multiply(Math.cos(angle) * radius)
                    .add(p2.clone().multiply(Math.sin(angle) * radius))
                    .add(dir.clone().multiply(step));

            Vector offset2 = p1.clone().multiply(Math.cos(angle + Math.PI) * radius)
                    .add(p2.clone().multiply(Math.sin(angle + Math.PI) * radius))
                    .add(dir.clone().multiply(step));

            player.getWorld().spawnParticle(Particle.CLOUD, center.clone().add(offset1), 0, 0, 0, 0, 0);
            player.getWorld().spawnParticle(Particle.CLOUD, center.clone().add(offset2), 0, 0, 0, 0, 0);
        }
    }

    private List<LivingEntity> getValidTargets(Player player, double radius) {
        return player.getNearbyEntities(radius, radius, radius).stream()
                .filter(e -> e instanceof LivingEntity && e != player)
                .map(e -> (LivingEntity) e)
                .toList();
    }

    private LivingEntity getNextTarget(Player player, List<LivingEntity> targets, LivingEntity lastTarget) {
        LivingEntity closestOther = null;
        double closestOtherDist = Double.MAX_VALUE;
        LivingEntity sameTarget = null;

        for (LivingEntity e : targets) {
            if (e.equals(lastTarget)) {
                sameTarget = e;
                continue;
            }

            double dist = e.getLocation().distanceSquared(player.getLocation());
            if (dist < closestOtherDist) {
                closestOtherDist = dist;
                closestOther = e;
            }
        }

        return closestOther != null ? closestOther : sameTarget;
    }
}