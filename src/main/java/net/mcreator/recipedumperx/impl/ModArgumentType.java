package net.mcreator.recipedumperx.impl;

import com.google.common.collect.Lists;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.ModList;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public enum ModArgumentType implements ArgumentType<IModInfo> {
    INSTANCE;

    private final Collection<String> EXAMPLES = Lists.newArrayList("minecraft", "thermal");
    private final SimpleCommandExceptionType commandExceptionType = new SimpleCommandExceptionType(new LiteralMessage("No such a mod"));

    @Override
    public IModInfo parse(StringReader reader) throws CommandSyntaxException {
        String modId = reader.readUnquotedString();
        return ModList.get().getMods()
                .stream()
                .filter(modInfo -> modInfo.getModId().equals(modId))
                .findFirst()
                .orElseThrow(() -> commandExceptionType.createWithContext(reader));
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(ModList.get().getMods().stream().map(IModInfo::getModId), builder);
    }
}