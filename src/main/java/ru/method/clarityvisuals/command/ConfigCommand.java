package ru.method.clarityvisuals.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import net.minecraft.class_1074;
import net.minecraft.class_156;
import net.minecraft.class_2172;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2568;
import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_5250;
import net.minecraft.class_5251;
import org.lwjgl.glfw.GLFW;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.util.ChatFormatting;
import ru.method.clarityvisuals.command.ChatOutput;
import ru.method.clarityvisuals.command.ClientCommand;
import ru.method.clarityvisuals.manager.ConfigManager;
import ru.method.clarityvisuals.command.HelpCommand;

public class ConfigCommand
extends ClientCommand {
    private static final String aj = ".clarity";
    private static final String ak = ".prime";
    private static final String al = "commands.help.desc.config";
    private static final List<String> am = List.of(
        "config save <название>", 
        "config load <название>", 
        "config export <название>", 
        "config import <название>", 
        "config remove <название>", 
        "config list", 
        "config dir"
    );

    public ConfigCommand() {
        super("config", "cfg");
    }

    @Override
    public void a(LiteralArgumentBuilder<class_2172> literalArgumentBuilder) {
        ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)literalArgumentBuilder.executes(commandContext -> {
            ChatFormatting.a(HelpCommand.f(this.t()), class_1074.method_4662((String)"commands.help.desc.config", (Object[])new Object[0]), this.w(), am);
            return 1;
        })).then(this.a("save").then(this.a("config", StringArgumentType.word()).suggests((commandContext, suggestionsBuilder) -> {
            File[] fileArray;
            if (ClarityVisuals.getInstance().getConfigsDir().exists() && ClarityVisuals.getInstance().getConfigsDir().isDirectory() && (fileArray = ConfigCommand.ab()) != null) {
                Arrays.stream(fileArray).map(File::getName).map(ConfigCommand::c).forEach(arg_0 -> ((SuggestionsBuilder)suggestionsBuilder).suggest(arg_0));
            }
            return suggestionsBuilder.buildFuture();
        }).executes(commandContext -> {
            String string = ClarityVisuals.getInstance().getConfigManager().bl((String)commandContext.getArgument("config", String.class));
            try {
                ClarityVisuals.getInstance().getConfigManager().bh(string);
                this.a("config save " + string, class_1074.method_4662((String)"commands.config.save", (Object[])new Object[]{string}), 9741240);
            }
            catch (Exception exception) {
                ClarityVisuals.LOGGER.error("[ConfigCommand] Failed to save config {}", (Object)string, (Object)exception);
                this.a("config save " + string, class_1074.method_4662((String)"commands.config.remove_failed", (Object[])new Object[]{string}), 16281969);
            }
            return 1;
        })))).then(this.a("load").then(this.a("config", StringArgumentType.word()).suggests((commandContext, suggestionsBuilder) -> {
            File[] fileArray;
            if (ClarityVisuals.getInstance().getConfigsDir().exists() && ClarityVisuals.getInstance().getConfigsDir().isDirectory() && (fileArray = ConfigCommand.ab()) != null) {
                Arrays.stream(fileArray).map(File::getName).map(ConfigCommand::c).forEach(arg_0 -> ((SuggestionsBuilder)suggestionsBuilder).suggest(arg_0));
            }
            return suggestionsBuilder.buildFuture();
        }).executes(commandContext -> {
            String string = ClarityVisuals.getInstance().getConfigManager().bl((String)commandContext.getArgument("config", String.class));
            try {
                boolean bl = ClarityVisuals.getInstance().getConfigManager().bi(string);
                if (bl) {
                    this.a("config load " + string, class_1074.method_4662((String)"commands.config.load", (Object[])new Object[]{string}), 9741240);
                } else {
                    this.a("config load " + string, class_1074.method_4662((String)"commands.config.notfound", (Object[])new Object[]{string}), 16281969);
                }
            }
            catch (Exception exception) {
                ClarityVisuals.LOGGER.error("[ConfigCommand] Failed to load config {}", (Object)string, (Object)exception);
                this.a("config load " + string, class_1074.method_4662((String)"commands.config.notfound", (Object[])new Object[]{string}), 16281969);
            }
            return 1;
        })))).then(this.a("export").then(this.a("config", StringArgumentType.word()).suggests((commandContext, suggestionsBuilder) -> {
            File[] fileArray;
            if (ClarityVisuals.getInstance().getConfigsDir().exists() && ClarityVisuals.getInstance().getConfigsDir().isDirectory() && (fileArray = ConfigCommand.ab()) != null) {
                Arrays.stream(fileArray).map(File::getName).map(ConfigCommand::c).forEach(arg_0 -> ((SuggestionsBuilder)suggestionsBuilder).suggest(arg_0));
            }
            return suggestionsBuilder.buildFuture();
        }).executes(commandContext -> {
            String string = ClarityVisuals.getInstance().getConfigManager().bl((String)commandContext.getArgument("config", String.class));
            File file = ConfigCommand.d(string);
            if (!file.exists()) {
                this.a("config export " + string, "Конфиг не найден: " + string, 16281969);
                return 1;
            }
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                String base64 = Base64.getEncoder().encodeToString(bytes);
                long handle = class_310.method_1551().method_22683().method_4490();
                GLFW.glfwSetClipboardString(handle, base64);
                this.a("config export " + string, "Конфиг '" + string + "' скопирован в буфер обмена!", 9741240);
            } catch (Exception e) {
                this.a("config export " + string, "Ошибка при экспорте конфига: " + e.getMessage(), 16281969);
            }
            return 1;
        })))).then(this.a("import").then(this.a("config", StringArgumentType.word()).executes(commandContext -> {
            String string = ClarityVisuals.getInstance().getConfigManager().bl((String)commandContext.getArgument("config", String.class));
            try {
                long handle = class_310.method_1551().method_22683().method_4490();
                String clipboard = GLFW.glfwGetClipboardString(handle);
                if (clipboard == null || clipboard.isBlank()) {
                    this.a("config import " + string, "Буфер обмена пуст!", 16281969);
                    return 1;
                }
                byte[] data;
                try {
                    data = Base64.getDecoder().decode(clipboard.trim());
                } catch (IllegalArgumentException ex) {
                    data = clipboard.getBytes(StandardCharsets.UTF_8);
                }
                File target = new File(ClarityVisuals.getInstance().getConfigsDir(), string + ".clarity");
                Files.write(target.toPath(), data);
                boolean loaded = ClarityVisuals.getInstance().getConfigManager().bi(string);
                if (loaded) {
                    this.a("config import " + string, "Конфиг '" + string + "' успешно импортирован и загружен!", 9741240);
                } else {
                    this.a("config import " + string, "Файл сохранен, но не удалось применить", 16281969);
                }
            } catch (Exception e) {
                this.a("config import " + string, "Ошибка при импорте: " + e.getMessage(), 16281969);
            }
            return 1;
        })))).then(this.a("remove").then(this.a("config", StringArgumentType.word()).suggests((commandContext, suggestionsBuilder) -> {
            File[] fileArray;
            if (ClarityVisuals.getInstance().getConfigsDir().exists() && ClarityVisuals.getInstance().getConfigsDir().isDirectory() && (fileArray = ConfigCommand.ab()) != null) {
                Arrays.stream(fileArray).map(File::getName).map(ConfigCommand::c).forEach(arg_0 -> ((SuggestionsBuilder)suggestionsBuilder).suggest(arg_0));
            }
            return suggestionsBuilder.buildFuture();
        }).executes(commandContext -> {
            File file;
            ConfigManager configManager = ClarityVisuals.getInstance().getConfigManager();
            String string = configManager.bl((String)commandContext.getArgument("config", String.class));
            if ("default".equalsIgnoreCase(string)) {
                this.a("config remove " + string, class_1074.method_4662((String)"commands.config.default_locked", (Object[])new Object[0]), 16096779);
                return 1;
            }
            String string2 = configManager.bl(configManager.dQ());
            if (string.equalsIgnoreCase(string2)) {
                try {
                    configManager.bh("default");
                }
                catch (Exception exception) {
                    ClarityVisuals.LOGGER.error("[ConfigCommand] Failed to switch current config before deleting {}", (Object)string, (Object)exception);
                    this.a("config remove " + string, class_1074.method_4662((String)"commands.config.remove_failed", (Object[])new Object[]{string}), 16281969);
                    return 1;
                }
            }
            if (!(file = ConfigCommand.d(string)).exists()) {
                this.a("config remove " + string, class_1074.method_4662((String)"commands.config.notfound", (Object[])new Object[]{string}), 16281969);
                return 1;
            }
            try {
                Files.deleteIfExists(file.toPath());
                this.a("config remove " + string, class_1074.method_4662((String)"commands.config.removed", (Object[])new Object[]{string}), 16317180);
            }
            catch (IOException iOException) {
                ClarityVisuals.LOGGER.error("[ConfigCommand] Failed to remove config {}", (Object)string, (Object)iOException);
                this.a("config remove " + string, class_1074.method_4662((String)"commands.config.remove_failed", (Object[])new Object[]{string}), 16281969);
            }
            return 1;
        })))).then(this.a("dir").executes(commandContext -> {
            this.aa();
            return 1;
        }))).then(this.a("list").executes(commandContext -> {
            File[] fileArray = ConfigCommand.ab();
            String string = ClarityVisuals.getInstance().getCommandManager().fj();
            if (fileArray == null || fileArray.length == 0) {
                this.a("config list", class_1074.method_4662((String)"commands.config.empty", (Object[])new Object[0]), 9741240);
            } else {
                Arrays.sort(fileArray, (file, file2) -> file.getName().compareToIgnoreCase(file2.getName()));
                class_5250 class_52502 = ChatFormatting.x("config list", class_1074.method_4662((String)"commands.help.desc.config", (Object[])new Object[0]));
                ChatFormatting.a(class_52502, ChatFormatting.z(class_1074.method_4662((String)"commands.ui.entries", (Object[])new Object[0]), String.valueOf(fileArray.length)));
                for (File file3 : fileArray) {
                    String string2 = ConfigCommand.c(file3.getName());
                    class_5250 class_52503 = ChatFormatting.y(string2, "");
                    if (!"default".equalsIgnoreCase(string2)) {
                        class_52503.method_10852((class_2561)ChatFormatting.cI(string + "config remove " + string2));
                    } else {
                        class_52503.method_10852((class_2561)ChatFormatting.a(class_1074.method_4662((String)"commands.ui.default", (Object[])new Object[0]), 9741240, null, null, class_1074.method_4662((String)"commands.config.default_locked", (Object[])new Object[0])));
                    }
                    ChatFormatting.a(class_52502, class_52503);
                }
                ChatOutput.b(class_52502);
            }
            return 1;
        }));
    }

    private void aa() {
        File file = ClarityVisuals.getInstance().getConfigsDir();
        if (!file.exists()) {
            file.mkdirs();
        }
        class_156.method_668().method_672(file);
        class_5250 class_52502 = class_2561.method_43470((String)file.getAbsolutePath()).method_10862(class_2583.field_24360.method_27703(class_5251.method_27717((int)ChatFormatting.bh(8246268))).method_10958(new class_2558(class_2558.class_2559.field_11746, file.getAbsolutePath())).method_10949(new class_2568(class_2568.class_5247.field_24342, class_2561.method_43470((String)file.getAbsolutePath()))));
        class_5250 class_52503 = ChatFormatting.x("config dir", class_1074.method_4662((String)"commands.help.desc.config", (Object[])new Object[0]));
        class_5250 class_52504 = ChatFormatting.z(class_1074.method_4662((String)"commands.ui.folder", (Object[])new Object[0]), "");
        class_52504.method_10852((class_2561)class_52502);
        ChatFormatting.a(class_52503, class_52504);
        ChatOutput.b(class_52503);
    }

    private void a(String string, String string2, int n) {
        ChatFormatting.b(string, class_1074.method_4662((String)"commands.help.desc.config", (Object[])new Object[0]), string2, n);
    }

    private static File[] ab() {
        return ClarityVisuals.getInstance().getConfigsDir().listFiles((file, string) -> {
            String string2 = string.toLowerCase();
            return string2.endsWith(".clarity") || string2.endsWith(".prime");
        });
    }

    private static String c(String string) {
        String string2 = string.toLowerCase();
        if (string2.endsWith(".clarity")) {
            return string.substring(0, string.length() - ".clarity".length());
        }
        if (string2.endsWith(".prime")) {
            return string.substring(0, string.length() - ".prime".length());
        }
        return string;
    }

    private static File d(String string) {
        File file = ClarityVisuals.getInstance().getConfigsDir();
        File file2 = new File(file, string + ".clarity");
        if (file2.exists()) {
            return file2;
        }
        return new File(file, string + ".prime");
    }
}
