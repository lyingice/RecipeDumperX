package net.mcreator.recipedumperx.impl;

import net.mcreator.recipedumperx.api.IRecipeDumper;
import net.mcreator.recipedumperx.api.IRecipeInputs;
import net.mcreator.recipedumperx.api.IRecipeOutputs;
import net.mcreator.recipedumperx.api.RecipeDumpException;
import com.google.common.collect.Iterators;
import com.google.gson.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.neoforged.neoforgespi.language.IModInfo;
import net.minecraft.world.item.crafting.RecipeHolder;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DumpRecipeCommand {
    private static final Map<Class<? extends Recipe<?>>, IRecipeDumper<Recipe<?>>> DUMPERS = new HashMap<>();
    private static final Set<ResourceLocation> ERROR_RECIPES = new HashSet<>();

    @SuppressWarnings("unchecked")
    public static void addRecipeDumper(Class<? extends Recipe<?>> recipeClass, IRecipeDumper<?> recipeDumper) {
        DUMPERS.put(recipeClass, ((IRecipeDumper<Recipe<?>>) recipeDumper));
    }

    public static int executeCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        IModInfo modInfo = context.getArgument("mod", IModInfo.class);
        String modId = modInfo.getModId();
        CommandSourceStack source = context.getSource();
        RecipeManager recipeManager = source.getPlayer().level().getRecipeManager();
        JsonArray recipesArray = DumpRecipeCommand.dumpAllRecipes(recipeManager, modId);
        JsonObject result = new JsonObject();
        result.add("recipes", recipesArray);
        JsonArray errorArray = new JsonArray();
        ERROR_RECIPES.forEach(id -> errorArray.add(id.toString()));
        result.add("error", errorArray);
        // 导出到客户端运行目录下的 export 文件夹
        File gameDir = new File(".");
        outputJson(new File(gameDir, String.format("export/dump_recipes_%s.json", modId)).toPath(), result);
        int recipesCount = Iterators.size(recipesArray.iterator());
        source.sendSuccess(() -> Component.literal("Dump recipes successfully! See export Directory."), false);
        source.sendSuccess(() -> Component.literal(String.format("%s recipes dumped, %s recipes skipped", recipesCount, ERROR_RECIPES.size())), false);
        ERROR_RECIPES.clear();
        return recipesCount;
    }

    public static JsonArray dumpAllRecipes(RecipeManager recipeManager, String modFilter) {
        JsonArray array = new JsonArray();
        // 客户端兼容：直接遍历 RecipeHolder，从中取出 Recipe 实例
        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            Recipe<?> recipe = holder.value();
            if (holder.id().getNamespace().equals(modFilter) && DUMPERS.containsKey(recipe.getClass())) {
                try {
                    JsonObject recipeJson = dumpRecipe(recipe, holder.id());
                    array.add(recipeJson);

                    // [新增] 锻造台配方兼容处理：同时导出一份 type 为 "smithing" 的副本
                    if (recipe instanceof SmithingTransformRecipe) {
                        JsonObject legacyCopy = recipeJson.deepCopy();
                        legacyCopy.addProperty("type", "smithing");
                        array.add(legacyCopy);
                    }
                } catch (RecipeDumpException e) {
                    ERROR_RECIPES.add(holder.id());
                }
            }
        }
        return array;
    }

    private static void outputJson(Path path, JsonElement element) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, gson.toJson(element).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JsonObject dumpRecipe(Recipe<?> recipe, ResourceLocation recipeId) throws RecipeDumpException {
        JsonObject jsonObject = new JsonObject();
        IRecipeDumper<Recipe<?>> recipeDumper = DUMPERS.get(recipe.getClass());
        jsonObject.addProperty("type", recipeDumper.getRecipeTypeName(recipe));
        jsonObject.addProperty("name", recipeId.toString());
        IRecipeInputs inputs = new RecipeInputs();
        IRecipeOutputs outputs = new RecipeOutputs();
        recipeDumper.setInputs(recipe, inputs);
        recipeDumper.setOutputs(recipe, outputs);
        jsonObject.add("input", inputs.serialize());
        jsonObject.add("output", outputs.serialize());
        recipeDumper.writeExtraInformation(recipe, jsonObject);
        return jsonObject;
    }
}