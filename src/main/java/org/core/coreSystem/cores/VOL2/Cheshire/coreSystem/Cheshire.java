package org.core.coreSystem.cores.VOL2.Cheshire.coreSystem;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class Cheshire {

    //CoolHashmap
    public HashMap<UUID, Long> R_COOLDOWN = new HashMap<>();
    public HashMap<UUID, Long> Q_COOLDOWN = new HashMap<>();
    public HashMap<UUID, Long> F_COOLDOWN = new HashMap<>();

    public long frozenCool = 10000;

    //passive


    //R
    public double r_Skill_amp = 0.3;
    public double r_Skill_damage = 5;
    public long r_Skill_Cool = 3000;

    //Q
    public double q_Skill_amp = 0.1;
    public double q_Skill_damage = 7;
    public long q_Skill_Cool = 0;

    //F
    public double f_Skill_amp = 0.2;
    public double f_Skill_damage = 0;
    public long f_Skill_Cool = 0;

    public void variableReset(Player player){
        R_COOLDOWN.remove(player.getUniqueId());
        Q_COOLDOWN.remove(player.getUniqueId());
        F_COOLDOWN.remove(player.getUniqueId());
    }
}
