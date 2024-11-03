package computer.heather.universalfacades;

import org.jetbrains.annotations.NotNull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class WrenchItem extends Item {
    
    public WrenchItem(Properties p_41383_) {
        super(p_41383_);
        
    }

    public static final TagKey<Block> BLACKLIST = TagKey.create(
        // The registry key. The type of the registry must match the generic type of the tag.
        Registries.BLOCK,
        // The location of the tag. This will put our tag at data/universalfacades/tags/block/blacklist.json.
        ResourceLocation.fromNamespaceAndPath("universalfacades", "blacklist")
    );


    @NotNull
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (!level.isClientSide && player != null) {
            BlockPos pos = context.getClickedPos();
            BlockState blockState = level.getBlockState(pos);
            Block block = blockState.getBlock();
            if (block != null) {
                if (blockState.is(BLACKLIST)) {
                    //Support for a blacklist (empty by default) in case of some bad interactions.
                    player.sendSystemMessage(Component.translatable("item.universalfacades.wrench.blacklist", Component.translatable(block.getDescriptionId())));
                    return InteractionResult.CONSUME;
                }

                if (player.isCrouching()) {
                    //Save the targeted blockstate to the wrench.
                    CompoundTag tag = NbtUtils.writeBlockState(blockState);
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    player.sendSystemMessage(Component.translatable("item.universalfacades.wrench.applytowrench", Component.translatable(block.getDescriptionId())));
                }
                else {
                    if (stack.has(DataComponents.CUSTOM_DATA)) {
                        //Load the facade blockstate from the wrench.
                        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
                        BlockState facadeState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), data.copyTag());

                        //Facade blockstate must not match target one
                        if (facadeState.equals(blockState)) {
                            player.sendSystemMessage(Component.translatable("item.universalfacades.wrench.matching"));
                            return InteractionResult.CONSUME;
                        }

                        //TODO actually apply
                        player.sendSystemMessage(Component.translatable("item.universalfacades.wrench.applytoblock",
                            Component.translatable(facadeState.getBlock().getDescriptionId()), Component.translatable(block.getDescriptionId())));
                    }
                    else {
                        //TODO - actually clear
                        player.sendSystemMessage(Component.translatable("item.universalfacades.wrench.empty", Component.translatable(block.getDescriptionId())));
                    }
                }
            }
            
        }
        
        return InteractionResult.CONSUME;
    }
}