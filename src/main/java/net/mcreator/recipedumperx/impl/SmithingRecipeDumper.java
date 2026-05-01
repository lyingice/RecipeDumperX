package net.mcreator.recipedumperx.impl;

import net.mcreator.recipedumperx.api.IRecipeDumper;
import net.mcreator.recipedumperx.api.IRecipeInputs;
import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;

@IRecipeDumper.For(SmithingTransformRecipe.class)
public class SmithingRecipeDumper implements IRecipeDumper<SmithingTransformRecipe> {

    private final MapCodec<SmithingTransformRecipe> codec = new SmithingTransformRecipe.Serializer().codec();

    @Override
    public void setInputs(SmithingTransformRecipe recipe, IRecipeInputs inputs) {
        // 通过 Codec 序列化成 NBT，然后从中提取三个槽位的数据
        codec.encoder().encodeStart(NbtOps.INSTANCE, recipe)
                .result()
                .ifPresent(tag -> {
                    CompoundTag compoundTag = (CompoundTag) tag;
                    inputs.addInput(1, Ingredient.CODEC.parse(NbtOps.INSTANCE, compoundTag.get("template")).result().orElseThrow());
                    inputs.addInput(2, Ingredient.CODEC.parse(NbtOps.INSTANCE, compoundTag.get("base")).result().orElseThrow());
                    inputs.addInput(3, Ingredient.CODEC.parse(NbtOps.INSTANCE, compoundTag.get("addition")).result().orElseThrow());
                });
    }

    @Override
    public String getRecipeTypeName(SmithingTransformRecipe recipe) {
        return "smithing_transform";  // 如果用 MC百科导入工具，改成 "smithing" 即可
    }
}