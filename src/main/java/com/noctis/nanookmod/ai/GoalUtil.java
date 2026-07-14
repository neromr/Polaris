package com.noctis.nanookmod.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

/**
 * PolarBear ya existe como clase vanilla y no podemos extenderla (ya esta
 * registrada), asi que para anadir/quitar IA usamos reflexion para llegar
 * a los campos "goalSelector" y "targetSelector" que Mob declara como
 * protected. ObfuscationReflectionHelper resuelve el nombre mapeado al
 * nombre real en tiempo de ejecucion, tanto en dev como en produccion.
 *
 * Una vez obtenido el GoalSelector, getAvailableGoals() SI es publico,
 * asi que el resto (anadir/quitar goals) se hace con la API normal.
 */
public final class GoalUtil {

    private GoalUtil() {}

    public static GoalSelector getGoalSelector(Mob mob) {
        return ObfuscationReflectionHelper.getPrivateValue(Mob.class, mob, "goalSelector");
    }

    public static GoalSelector getTargetSelector(Mob mob) {
        return ObfuscationReflectionHelper.getPrivateValue(Mob.class, mob, "targetSelector");
    }

    /**
     * Quita SOLO los goals que son clases internas propias de PolarBear
     * (panico, atacar jugadores cerca de una cria, contraataque). Los
     * goals genericos compartidos con otros mobs (MeleeAttackGoal,
     * deambular, mirar al jugador, flotar en agua...) no se tocan.
     */
    public static void removeBearHostileGoals(Mob mob) {
        removeIf(getGoalSelector(mob), g -> g.getClass().getEnclosingClass() == PolarBear.class);
        removeIf(getTargetSelector(mob), g -> g.getClass().getEnclosingClass() == PolarBear.class);
    }

    private static void removeIf(GoalSelector selector, java.util.function.Predicate<Goal> predicate) {
        selector.getAvailableGoals().removeIf(wrapped -> predicate.test(wrapped.getGoal()));
    }

    /** Comprueba si ya se aplicaron los goals de Nanook (para no duplicarlos). */
    public static boolean hasNanookGoals(Mob mob) {
        return getGoalSelector(mob).getAvailableGoals().stream()
                .anyMatch(w -> w.getGoal() instanceof NanookBearGoals.SitGoal);
    }
}
