package vg.skye.trinkhide;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class TrinkHidePlayerComponent implements AutoSyncedComponent {
    private final Player player;
    private List<String> hiddenSlots;

    public TrinkHidePlayerComponent(Player player) {
        this.player = player;
        this.hiddenSlots = List.of();
    }

    public List<String> getHiddenSlots() {
        return List.copyOf(hiddenSlots);
    }

    public void setHiddenSlots(List<String> hiddenSlots) {
        this.hiddenSlots = List.copyOf(hiddenSlots);
        TrinkHideComponents.HIDDEN_TRINKETS.sync(player);
    }

    @Override
    public void readFromNbt(CompoundTag compoundTag) {
        var listTag = compoundTag.getList("HiddenSlots", Tag.TAG_STRING);
        var deser = new ArrayList<String>(listTag.size());
        for (Tag tag : listTag) {
            deser.add(tag.getAsString());
        }
        hiddenSlots = deser;
    }

    @Override
    public void writeToNbt(CompoundTag compoundTag) {
        var listTag = new ListTag();
        for (String hiddenSlot : hiddenSlots) {
            listTag.add(StringTag.valueOf(hiddenSlot));
        }
        compoundTag.put("HiddenSlots", listTag);
    }
}
