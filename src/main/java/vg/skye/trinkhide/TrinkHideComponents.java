package vg.skye.trinkhide;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.resources.ResourceLocation;

public class TrinkHideComponents implements EntityComponentInitializer {
    public static final ComponentKey<TrinkHidePlayerComponent> HIDDEN_TRINKETS =
            ComponentRegistry.getOrCreate(ResourceLocation.tryBuild(TrinkHide.MOD_ID, "hidden_trinkets"), TrinkHidePlayerComponent.class);
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry entityComponentFactoryRegistry) {
        entityComponentFactoryRegistry.registerForPlayers(HIDDEN_TRINKETS, TrinkHidePlayerComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}
