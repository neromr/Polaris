package com.noctis.nanookmod;

import com.noctis.nanookmod.item.NanookSpearItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Clase principal del mod "La Bendicion de Nanook".
 *
 * Resumen de la arquitectura (ver README.md para el detalle completo):
 *  - NanookBearData: guarda el estado de cada oso (domesticado, dueno,
 *    orden actual) en los datos persistentes de la propia entidad (NBT),
 *    y la Bendicion de cada jugador en sus datos persistentes.
 *  - GoalUtil + NanookBearGoals: quitan el comportamiento agresivo propio
 *    del oso polar vanilla y anaden el comportamiento de mascota (seguir,
 *    defender, quedarse quieto, deambular).
 *  - NanookEventHandler: interaccion (domesticar/alimentar/montar/ordenar),
 *    aplicacion de los goals al cargar el mundo, bloqueo de ataques de
 *    cualquier oso contra un jugador bendecido, y el movimiento al montar.
 */
@Mod(NanookMod.MOD_ID)
public class NanookMod {

    public static final String MOD_ID = "nanookmod";

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final RegistryObject<Item> NANOOK_SPEAR = ITEMS.register("nanook_spear",
            () -> new NanookSpearItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public NanookMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(NANOOK_SPEAR);
        }
    }
}
