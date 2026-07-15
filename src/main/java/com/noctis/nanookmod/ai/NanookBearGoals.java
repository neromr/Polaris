package com.noctis.nanookmod.ai;

import com.noctis.nanookmod.util.NanookBearData;
import com.noctis.nanookmod.util.NanookBearData.BearState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.UUID;

/**
 * Contenedor de todos los goals propios del oso domesticado. Se anaden
 * una sola vez por entidad (ver GoalUtil.hasNanookGoals) y cada uno
 * comprueba el estado guardado en NanookBearData antes de actuar, asi
 * que conviven sin pisarse: solo uno puede estar "activo" a la vez
 * porque comparten la bandera Flag.MOVE (o Flag.TARGET, segun el caso).
 */
public final class NanookBearGoals {

    private NanookBearGoals() {}

    /** Prioridad 1: quedarse quieto cuando el dueno lo ordena. */
    public static class SitGoal extends Goal {
        private final PolarBear bear;

        public SitGoal(PolarBear bear) {
            this.bear = bear;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return NanookBearData.isTamed(bear) && NanookBearData.getState(bear) == BearState.SIT;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void tick() {
            bear.getNavigation().stop();
        }
    }

    /** Prioridad 2: seguir al dueno cuando el estado es FOLLOW. */
    public static class FollowOwnerGoal extends Goal {
        private static final double START_DISTANCE = 10.0;
        private static final double STOP_DISTANCE = 3.0;
        private static final double TELEPORT_DISTANCE = 24.0;

        private final PolarBear bear;
        private Player owner;

        public FollowOwnerGoal(PolarBear bear) {
            this.bear = bear;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!NanookBearData.isTamed(bear) || NanookBearData.getState(bear) != BearState.FOLLOW) return false;
            if (bear.isVehicle()) return false;
            UUID ownerId = NanookBearData.getOwner(bear);
            if (ownerId == null) return false;
            Player found = bear.level().getPlayerByUUID(ownerId);
            if (found == null || found.isSpectator() || !found.isAlive()) return false;
            if (bear.distanceTo(found) < START_DISTANCE) return false;
            this.owner = found;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (owner == null || bear.isVehicle()) return false;
            if (!NanookBearData.isTamed(bear) || NanookBearData.getState(bear) != BearState.FOLLOW) return false;
            return bear.distanceTo(owner) > STOP_DISTANCE;
        }

        @Override
        public void start() {
            bear.getNavigation().moveTo(owner, 1.2);
        }

        @Override
        public void tick() {
            bear.getLookControl().setLookAt(owner, 10.0F, 30.0F);
            if (bear.distanceTo(owner) >= TELEPORT_DISTANCE) {
                bear.teleportTo(owner.getX(), owner.getY(), owner.getZ());
                return;
            }
            if (bear.getNavigation().isDone()) {
                bear.getNavigation().moveTo(owner, 1.2);
            }
        }

        @Override
        public void stop() {
            owner = null;
            bear.getNavigation().stop();
        }
    }

    /** Prioridad 3: deambular libremente cuando el estado es WANDER. */
    public static class WanderGoal extends WaterAvoidingRandomStrollGoal {
        private final PolarBear bear;

        public WanderGoal(PolarBear bear) {
            super(bear, 1.0);
            this.bear = bear;
        }

        @Override
        public boolean canUse() {
            if (!NanookBearData.isTamed(bear) || NanookBearData.getState(bear) != BearState.WANDER) return false;
            if (bear.isVehicle()) return false;
            return super.canUse();
        }
    }

    /** Prioridad 1 (target): defiende al dueno atacando a quien lo hiera. */
    public static class DefendOwnerGoal extends Goal {
        private final PolarBear bear;

        public DefendOwnerGoal(PolarBear bear) {
            this.bear = bear;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (!eligible(bear)) return false;
            Player owner = findOwner(bear);
            if (owner == null) return false;
            LivingEntity attacker = owner.getLastHurtByMob();
            return attacker != null && attacker.isAlive() && attacker != bear;
        }

        @Override
        public void start() {
            Player owner = findOwner(bear);
            if (owner != null) bear.setTarget(owner.getLastHurtByMob());
        }
    }

    /** Prioridad 2 (target): ataca a lo que el dueno ataque. */
    public static class AssistOwnerGoal extends Goal {
        private final PolarBear bear;

        public AssistOwnerGoal(PolarBear bear) {
            this.bear = bear;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (!eligible(bear)) return false;
            Player owner = findOwner(bear);
            if (owner == null) return false;
            LivingEntity attacked = owner.getLastHurtMob();
            return attacked != null && attacked.isAlive() && attacked != bear;
        }

        @Override
        public void start() {
            Player owner = findOwner(bear);
            if (owner != null) bear.setTarget(owner.getLastHurtMob());
        }
    }

    private static boolean eligible(PolarBear bear) {
        return NanookBearData.isTamed(bear) && NanookBearData.getState(bear) != BearState.SIT && !bear.isVehicle();
    }

    private static Player findOwner(PolarBear bear) {
        UUID ownerId = NanookBearData.getOwner(bear);
        return ownerId == null ? null : bear.level().getPlayerByUUID(ownerId);
    }
}
