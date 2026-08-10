package org.core.coreSystem.cores.VOL6.Jester.Skill;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.core.cool.Cool;
import org.core.coreSystem.absCoreSystem.SkillBase;
import org.core.coreSystem.cores.VOL6.Jester.coreSystem.Jester;

public class R implements SkillBase {

    private final Jester config;
    private final JavaPlugin plugin;
    private final Cool cool;

    public R(Jester config, JavaPlugin plugin, Cool cool){
        this.config = config;
        this.plugin = plugin;
        this.cool = cool;
    }

    @Override
    public void Trigger(Player player) {

    }
}
