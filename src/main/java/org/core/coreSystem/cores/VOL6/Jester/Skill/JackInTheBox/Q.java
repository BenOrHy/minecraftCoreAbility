package org.core.coreSystem.cores.VOL6.Jester.Skill.JackInTheBox;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.core.cool.Cool;
import org.core.coreSystem.absCoreSystem.SkillBase;
import org.core.coreSystem.cores.VOL6.Jester.coreSystem.Jester;

import java.util.function.Consumer;

public class Q implements SkillBase {
    private final Jester config;
    private final JavaPlugin plugin;
    private final Cool cool;

    private final Consumer<Player> onSkipStage;

    private final Q_trident Trident;
    private final Q_rocket Rocket;
    private final Q_shield Shield;
    private final Q_stick Stick;
    private final Q_crown Crown;

    public Q(Jester config, JavaPlugin plugin, Cool cool, Consumer<Player> onSkipStage){
        this.config = config;
        this.plugin = plugin;
        this.cool = cool;
        this.onSkipStage = onSkipStage;

        this.Trident = new Q_trident(config, plugin, cool, onSkipStage);
        this.Rocket = new Q_rocket(config, plugin, cool, onSkipStage);
        this.Shield = new Q_shield(config, plugin, cool, onSkipStage);
        this.Stick = new Q_stick(config, plugin, cool, onSkipStage);
        this.Crown = new Q_crown(config, plugin, cool, onSkipStage);
    }

    @Override
    public void Trigger(Player player) {
        if (!config.canUseSkill.getOrDefault(player.getUniqueId(), false)) return;

        Material handMat = config.currentQWeapon.getOrDefault(player.getUniqueId(), player.getInventory().getItemInMainHand().getType());

        String coolKey = config.getWeaponQCoolKey(handMat);

        if (coolKey == null) return;
        if (cool.isReloading(player, coolKey)) return;

        switch (handMat) {
            case TRIDENT:
                Trident.Trigger(player);
                break;
            case FIREWORK_ROCKET:
                Rocket.Trigger(player);
                break;
            case SHIELD:
                Shield.Trigger(player);
                break;
            case STICK:
                Stick.Trigger(player);
                break;
            case CHAINMAIL_HELMET:
                Crown.Trigger(player);
                break;
            default:
                break;
        }
    }
}