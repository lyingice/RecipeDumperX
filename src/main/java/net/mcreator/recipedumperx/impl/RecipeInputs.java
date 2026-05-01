package net.mcreator.recipedumperx.impl;

import net.mcreator.recipedumperx.api.IRecipeInputs;
import net.mcreator.recipedumperx.api.RecipeDumpException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.item.crafting.Ingredient;

public class RecipeInputs implements IRecipeInputs {
    Int2ObjectMap<Ingredient> inputs = new Int2ObjectArrayMap<>();
    Int2IntMap counts = new Int2IntArrayMap();

    @Override
    public void addInput(int slot, Ingredient ingredient, int count) {
        // 跳过空原料
        if (ingredient.isEmpty()) {
            return;
        }
        inputs.put(slot, ingredient);
        counts.put(slot, count);
    }

    @Override
    public JsonObject serialize() throws RecipeDumpException {
        JsonObject json = new JsonObject();
        if (inputs.size() != counts.size()) {
            throw new RecipeDumpException();
        }
        try {
            for (Int2ObjectMap.Entry<Ingredient> entry : inputs.int2ObjectEntrySet()) {
                Ingredient ingredient = entry.getValue();

                // 跳过自定义原料类型，避免序列化失败
                if (ingredient.isCustom()) {
                    continue;
                }

                // 使用原版 Codec 序列化为 JSON
                JsonElement ingredientJson = Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, ingredient)
                        .result()
                        .orElseThrow();

                // 添加数量信息
                ingredientJson.getAsJsonObject().addProperty("count", counts.get(entry.getIntKey()));
                json.add(String.valueOf(entry.getIntKey()), ingredientJson);
            }
            return json;
        } catch (Throwable e) {
            throw new RecipeDumpException();
        }
    }
}