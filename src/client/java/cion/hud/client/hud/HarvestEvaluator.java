package cion.hud.client.hud;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.TintedGlassBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class HarvestEvaluator {
    public enum Result { CAN_HARVEST, CANT_HARVEST, NEEDS_SPECIAL }

    private static final ResourceKey<Enchantment> SILK_TOUCH =
            ResourceKey.create(Registries.ENCHANTMENT, Identifier.withDefaultNamespace("silk_touch"));

    private HarvestEvaluator() {}

    public static Result evaluate(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        if (state.getDestroySpeed(level, pos) < 0.0F) return Result.CANT_HARVEST;
        if (state.requiresCorrectToolForDrops() && !player.hasCorrectToolForDrops(state)) {
            return Result.CANT_HARVEST;
        }
        if (state.getBlock().getLootTable().isEmpty()) return Result.CANT_HARVEST;
        return dropsBlockItem(state, player) ? Result.CAN_HARVEST : Result.NEEDS_SPECIAL;
    }

    private static boolean dropsBlockItem(BlockState state, Player player) {
        Block b = state.getBlock();
        ItemStack tool = player.getMainHandItem();

        if (hasSilkTouch(tool, player.level())) return true;

        if (b instanceof LeavesBlock) return tool.is(Items.SHEARS);
        if (b == Blocks.COBWEB) return tool.is(Items.SHEARS) || tool.is(ItemTags.SWORDS);

        if (b instanceof StainedGlassBlock || b instanceof StainedGlassPaneBlock || b instanceof TintedGlassBlock) return false;
        if (b == Blocks.GLASS || b == Blocks.GLASS_PANE) return false;

        if (b instanceof IceBlock || b instanceof FrostedIceBlock) return false;

        if (b instanceof GrassBlock) return false;
        if (b == Blocks.MYCELIUM || b == Blocks.PODZOL || b == Blocks.DIRT_PATH || b == Blocks.FARMLAND) return false;

        if (b == Blocks.STONE) return false;
        if (b == Blocks.GLOWSTONE || b == Blocks.SEA_LANTERN || b == Blocks.MELON || b == Blocks.BOOKSHELF) return false;
        if (b == Blocks.SPAWNER) return false;

        if (b == Blocks.SCULK || b == Blocks.SCULK_VEIN || b == Blocks.SCULK_CATALYST
                || b == Blocks.SCULK_SENSOR || b == Blocks.SCULK_SHRIEKER) return false;

        if (b instanceof DropExperienceBlock) return false;
        if (b instanceof RedStoneOreBlock) return false;

        return true;
    }

    private static boolean hasSilkTouch(ItemStack stack, Level level) {
        if (stack.isEmpty()) return false;
        try {
            Holder<Enchantment> holder = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(SILK_TOUCH);
            return EnchantmentHelper.getItemEnchantmentLevel(holder, stack) > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
