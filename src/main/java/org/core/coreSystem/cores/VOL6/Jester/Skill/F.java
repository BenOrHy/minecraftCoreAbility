package org.core.coreSystem.cores.VOL6.Jester.Skill;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.core.cool.Cool;
import org.core.coreSystem.absCoreSystem.SkillBase;
import org.core.coreSystem.cores.VOL6.Jester.coreSystem.Jester;
import org.core.effect.crowdControl.ForceDamage;
import org.core.effect.crowdControl.Invulnerable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class F implements SkillBase {

    private final Jester config;
    private final JavaPlugin plugin;
    private final Cool cool;
    private final NamespacedKey keyF;

    private final Predicate<Player> specialSkipTrickShowStage;
    private final Consumer<Player> forceEndTrickShow;

    public F(Jester config, JavaPlugin plugin, Cool cool, Predicate<Player> specialSkipTrickShowStage, Consumer<Player> forceEndTrickShow){
        this.config = config;
        this.plugin = plugin;
        this.cool = cool;
        this.keyF = new NamespacedKey(plugin, "F");
        this.specialSkipTrickShowStage = specialSkipTrickShowStage;
        this.forceEndTrickShow = forceEndTrickShow;
    }

    @Override
    public void Trigger(Player player){
        if (!config.canUseSkill.getOrDefault(player.getUniqueId(), false)) {
            Vector dashVector = player.getLocation().getDirection().normalize().multiply(1.2).setY(0.2);
            player.setVelocity(dashVector);

            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.5f);
            player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);

            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 24, 1, false, false, true));

            Invulnerable invuln = new Invulnerable(player, 1200L);
            invuln.applyEffect(player);
        } else {
            Material handMat = player.getInventory().getItemInMainHand().getType();

            if (handMat == Material.CHAINMAIL_HELMET) {

                Vector dashVector = player.getLocation().getDirection().normalize().multiply(1.2).setY(0.2);
                player.setVelocity(dashVector);

                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.5f);
                player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);

                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 24, 1, false, false, true));

                Invulnerable invuln = new Invulnerable(player, 1200L);
                invuln.applyEffect(player);

                long level = player.getPersistentDataContainer().getOrDefault(keyF, PersistentDataType.LONG, 0L);
                double ampMultiplier = 1.0 + (config.f_Skill_amp * level);
                double damage = config.f_Skill_damage * ampMultiplier;

                DamageSource source = DamageSource.builder(DamageType.PLAYER_ATTACK)
                        .withCausingEntity(player)
                        .withDirectEntity(player)
                        .build();

                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1.0f, 0.5f);
                player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation(), 50, 3.0, 1.0, 3.0, 0.1);

                for (org.bukkit.entity.Entity entity : player.getNearbyEntities(6, 6, 6)) {
                    if (entity instanceof LivingEntity target && entity != player) {
                        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, false, true));

                        ForceDamage forceDamage = new ForceDamage(target, damage, source, true);
                        forceDamage.applyEffect(player);
                    }
                }

                forceEndTrickShow.accept(player);
                cool.setCooldown(player, 12000L, "TRICK SHOW Cooldown", "boss");
            } else {
                boolean success = specialSkipTrickShowStage.test(player);
                if (!success) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 0.3f);
                }
            }
        }
    }
}