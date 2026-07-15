package com.noctis.nanookmod.event;

import com.noctis.nanookmod.NanookMod;
import com.noctis.nanookmod.ai.GoalUtil;
import com.noctis.nanookmod.ai.NanookBearGoals;
import com.noctis.nanookmod.util.NanookBearData;
import com.noctis.nanookmod.util.NanookBearData.BearState;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = NanookMod.MOD_ID)
public class NanookEventHandler {

    /** El doble de dificil que el mob mas dificil de domesticar en vanilla
     *  (el loro, con 10% de probabilidad por semilla -> aqui 5%). */
    private static final float TAME_CHANCE = 0.05F;

    // ------------------------------------------------------------------
    // 1) Interaccion: domesticar, alimentar, ordenar y montar
    // ------------------------------------------------------------------
    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof PolarBear bear)) return;
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        ServerLevel level = (ServerLevel) player.level();
        ItemStack stack = player.getItemInHand(event.getHand());

        if (!NanookBearData.isTamed(bear)) {
            tryTame(event, bear, player, stack, level);
            return;
        }

        boolean isOwner = NanookBearData.isOwner(bear, player);

        // Agacharse + click al dueno = ciclar orden (Seguir/Quieto/Deambular)
        if (isOwner && player.isShiftKeyDown()) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            BearState newState = NanookBearData.cycleState(bear);
            if (player instanceof ServerPlayer sp) {
                sp.displayClientMessage(Component.translatable("nanookmod.command." + newState.name().toLowerCase()), true);
            }
            return;
        }

        // Alimentar con bacalao crudo cura al oso ya domesticado
        if (isOwner && stack.is(Items.COD) && bear.getHealth() < bear.getMaxHealth()) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            if (!player.getAbilities().instabuild) stack.shrink(1);
            bear.heal(4.0F);
            level.sendParticles(ParticleTypes.HEART, bear.getX(), bear.getY() + 1, bear.getZ(), 4, 0.4, 0.4, 0.4, 0.0);
            return;
        }

        // Montar: el dueno siempre puede subir; un segundo jugador solo si
        // el dueno ya esta montado (monta "sobre" el dueno para lograr el
        // segundo asiento sin tener que alterar la clase PolarBear).
        if (!player.isShiftKeyDown() && stack.isEmpty()) {
            if (isOwner && bear.getPassengers().isEmpty()) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                player.startRiding(bear);
            } else if (!isOwner && bear.isVehicle()) {
                Entity firstPassenger = bear.getPassengers().get(0);
                if (firstPassenger instanceof Player ownerRiding
                        && ownerRiding.getUUID().equals(NanookBearData.getOwner(bear))
                        && ownerRiding.getPassengers().isEmpty()) {
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    player.startRiding(ownerRiding);
                }
            }
        }
    }

    private static void tryTame(PlayerInteractEvent.EntityInteract event, PolarBear bear, Player player,
                                 ItemStack stack, ServerLevel level) {
        if (!stack.is(Items.COD)) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!player.getAbilities().instabuild) stack.shrink(1);

        if (level.random.nextFloat() < TAME_CHANCE) {
            NanookBearData.setTamed(bear, player.getUUID());
            GoalUtil.removeBearHostileGoals(bear);
            addNanookGoals(bear);
            bear.setTarget(null);
            NanookBearData.grantBlessing(player);

            level.sendParticles(ParticleTypes.HEART, bear.getX(), bear.getY() + 1, bear.getZ(), 10, 0.5, 0.5, 0.5, 0.0);
            level.playSound(null, bear.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 1.0F, 1.0F);

            if (player instanceof ServerPlayer sp) {
                CriteriaTriggers.TAME_ANIMAL.trigger(sp, bear);
                sp.displayClientMessage(Component.translatable("nanookmod.tame.success"), false);
            }
        } else {
            level.sendParticles(ParticleTypes.SMOKE, bear.getX(), bear.getY() + 1, bear.getZ(), 6, 0.5, 0.5, 0.5, 0.0);
        }
    }

    // ------------------------------------------------------------------
    // 2) Al cargar el mundo: reaplicar la IA a los osos ya domesticados
    // ------------------------------------------------------------------
    @SubscribeEvent
    public static void onJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        if (event.getEntity() instanceof PolarBear bear && NanookBearData.isTamed(bear)) {
            if (!GoalUtil.hasNanookGoals(bear)) {
                GoalUtil.removeBearHostileGoals(bear);
                addNanookGoals(bear);
            }
        }
    }

    private static void addNanookGoals(PolarBear bear) {
        GoalUtil.getGoalSelector(bear).addGoal(1, new NanookBearGoals.SitGoal(bear));
        GoalUtil.getGoalSelector(bear).addGoal(2, new NanookBearGoals.FollowOwnerGoal(bear));
        GoalUtil.getGoalSelector(bear).addGoal(3, new NanookBearGoals.WanderGoal(bear));
        GoalUtil.getTargetSelector(bear).addGoal(1, new NanookBearGoals.DefendOwnerGoal(bear));
        GoalUtil.getTargetSelector(bear).addGoal(2, new NanookBearGoals.AssistOwnerGoal(bear));
    }

    // ------------------------------------------------------------------
    // 3) La Bendicion de Nanook: ningun oso polar ataca a quien la tenga,
    //    ni siquiera cerca de una cria ni si el jugador ataca primero.
    // ------------------------------------------------------------------
    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof PolarBear)) return;
        if (event.getNewTarget() instanceof Player player && NanookBearData.hasBlessing(player)) {
            event.setCanceled(true);
        }
    }

    // ------------------------------------------------------------------
    // 4) Movimiento al ser montado (el oso no tiene control de jinete en
    //    vanilla, asi que lo aplicamos manualmente cada tick).
    // ------------------------------------------------------------------
    @SubscribeEvent
    public static void onBearTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof PolarBear bear)) return;
        if (bear.level().isClientSide || !bear.isVehicle()) return;
        if (!(bear.getPassengers().get(0) instanceof Player rider)) return;
        if (!NanookBearData.isTamed(bear)) return;

        bear.setYRot(rider.getYRot());
        bear.yRotO = bear.getYRot();
        bear.yBodyRot = bear.getYRot();
        bear.yHeadRot = bear.getYRot();
        bear.setXRot(rider.getXRot() * 0.5F);

        float forward = rider.zza;
        float strafe = rider.xxa * 0.5F;
        if (forward <= 0.0F) forward *= 0.25F;

        if (Math.abs(forward) > 0.01F || Math.abs(strafe) > 0.01F) {
            double speed = 0.32;
            double yawRad = Math.toRadians(bear.getYRot());
            double sin = Math.sin(yawRad);
            double cos = Math.cos(yawRad);
            double dx = (strafe * cos - forward * sin) * speed;
            double dz = (forward * cos + strafe * sin) * speed;
            bear.move(MoverType.SELF, new Vec3(dx, 0.0, dz));
        }
    }
}
