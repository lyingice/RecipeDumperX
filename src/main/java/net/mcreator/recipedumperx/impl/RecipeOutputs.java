package net.mcreator.recipedumperx.impl;

import net.mcreator.recipedumperx.api.IRecipeOutputs;
import net.mcreator.recipedumperx.api.RecipeDumpException;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class RecipeOutputs implements IRecipeOutputs {
    Int2ObjectMap<ItemStack> outputs = new Int2ObjectArrayMap<>();

    @Override
    public void addOutput(int slot, ItemStack stack) {
        outputs.put(slot, stack);
    }

    @Override
    public JsonObject serialize() throws RecipeDumpException {
        JsonObject json = new JsonObject();
        try {
            for (Int2ObjectMap.Entry<ItemStack> entry : outputs.int2ObjectEntrySet()) {
                JsonObject stackJson = new JsonObject();
                ItemStack stack = entry.getValue();

                // 基础信息：物品ID和数量
                stackJson.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                stackJson.addProperty("count", stack.getCount());

                // 处理 NBT：使用 DataComponentPatch 自动获取所有组件差异
                DataComponentPatch componentsPatch = stack.getComponentsPatch();
                if (!componentsPatch.isEmpty()) {
                    Tag nbtTag = DataComponentPatch.CODEC.encodeStart(NbtOps.INSTANCE, componentsPatch).getOrThrow();
                    stackJson.addProperty("nbt", nbtTag.toString());
                }

                json.add(String.valueOf(entry.getIntKey()), stackJson);
            }
        } catch (Throwable throwable) {
            throw new RecipeDumpException();
        }
        return json;
    }
}