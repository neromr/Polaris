package com.noctis.nanookmod.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * Guardamos todo el estado propio del mod usando getPersistentData(),
 * el CompoundTag que Forge/Vanilla ya adjunta a cada entidad y que se
 * guarda solo con el mundo. Evitamos asi tener que crear un sistema de
 * Capabilities completo solo para tres o cuatro valores.
 *
 * IMPORTANTE: todas las claves llevan el prefijo "nanookmod_" para no
 * chocar nunca con NBT de otros mods.
 */
public final class NanookBearData {

    private NanookBearData() {}

    public enum BearState { FOLLOW, SIT, WANDER }

    private static final String TAMED = "nanookmod_tamed";
    private static final String OWNER = "nanookmod_owner";
    private static final String STATE = "nanookmod_state";
    private static final String BLESSING = "nanookmod_blessing";

    public static boolean isTamed(PolarBear bear) {
        return bear.getPersistentData().getBoolean(TAMED);
    }

    public static void setTamed(PolarBear bear, UUID owner) {
        CompoundTag tag = bear.getPersistentData();
        tag.putBoolean(TAMED, true);
        tag.putUUID(OWNER, owner);
        tag.putByte(STATE, (byte) BearState.FOLLOW.ordinal());
        bear.setPersistenceRequired();
    }

    public static UUID getOwner(PolarBear bear) {
        CompoundTag tag = bear.getPersistentData();
        return tag.hasUUID(OWNER) ? tag.getUUID(OWNER) : null;
    }

    public static boolean isOwner(PolarBear bear, Player player) {
        UUID owner = getOwner(bear);
        return owner != null && owner.equals(player.getUUID());
    }

    public static BearState getState(PolarBear bear) {
        int ordinal = bear.getPersistentData().getByte(STATE);
        BearState[] values = BearState.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : BearState.FOLLOW;
    }

    public static void setState(PolarBear bear, BearState state) {
        bear.getPersistentData().putByte(STATE, (byte) state.ordinal());
    }

    /** Cicla Seguir -> Quieto -> Deambular -> Seguir... y devuelve el nuevo estado. */
    public static BearState cycleState(PolarBear bear) {
        BearState next = switch (getState(bear)) {
            case FOLLOW -> BearState.SIT;
            case SIT -> BearState.WANDER;
            case WANDER -> BearState.FOLLOW;
        };
        setState(bear, next);
        return next;
    }

    public static boolean hasBlessing(Player player) {
        return player.getPersistentData().getBoolean(BLESSING);
    }

    public static void grantBlessing(Player player) {
        player.getPersistentData().putBoolean(BLESSING, true);
    }
}
