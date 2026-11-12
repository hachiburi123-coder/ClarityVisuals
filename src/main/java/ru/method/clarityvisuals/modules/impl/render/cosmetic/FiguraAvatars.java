package ru.method.clarityvisuals.modules.impl.render.cosmetic;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import net.minecraft.class_310;

public final class FiguraAvatars {
    private static final String RESOURCE_ROOT = "figura_avatars";

    /**
     * Каталог 3D-скинов: показываем только полноценные скины персонажей.
     * Скрыты шляпы (head-*), оружие (weapon-*) и прочие не-скины/сломанные модели.
     * Файлы пользователя на диске не удаляются — фильтр только для каталога и установщика.
     */
    private static final Set<String> HIDDEN = Set.of(
        // не-скины: головные уборы и аксессуары
        "beret", "fall crown", "flower hat", "mushroom cap", "mushroom cap (crimson)",
        "oversized leather hat", "pumpkin hat", "bunny-asset", "shark tail", "simplewings",
        "cool coat", "cool coat - slim body", "can+wheelchair", "wheelchair",
        // не-скины: оружие и прочее
        "axe", "cool scythe", "floating_swords_v2", "sword_by_salerra", "frieren staff",
        "axolotl!", "crazy rabbit_1", "dreamy fox companion", "turtle pet", "untitled goose",
        "goat", "leaf",
        // сломанные/тестовые модели
        "android", "baron11", "beardie", "cotton", "depth", "model test", "mothi", "mothli",
        "wynn-extra-bends-1.3b");

    /** true — модель показывается в каталоге 3D-скинов (и устанавливается инсталлятором). */
    public static boolean isCatalogAvatar(String name) {
        if (name == null) {
            return false;
        }
        String key = name.trim().toLowerCase(Locale.ROOT);
        if (key.isEmpty()) {
            return false;
        }
        return !key.startsWith("head-") && !key.startsWith("weapon-") && !HIDDEN.contains(key);
    }

    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);
    private static final AtomicInteger INSTALLED = new AtomicInteger();
    private static volatile boolean finished;
    private static volatile Throwable lastError;

    private FiguraAvatars() {
    }

    public static Path avatarsDir() {
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1697 == null) {
            return null;
        }
        return mc.field_1697.toPath().resolve("figura").resolve("avatars").normalize();
    }

    public static Path appliedKeyPath() {
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1697 == null) {
            return null;
        }
        return mc.field_1697.toPath().resolve("figura").resolve("current_avatar.txt").normalize();
    }

    public static void ensureInstalled() {
        if (!RUNNING.compareAndSet(false, true)) {
            return;
        }
        Thread thread = new Thread(() -> {
            try {
                INSTALLED.set(0);
                finished = false;
                lastError = null;
                installNow();
                finished = true;
            }
            catch (Throwable t) {
                finished = false;
                lastError = t;
            }
            finally {
                RUNNING.set(false);
            }
        }, "Figura-Installer");
        thread.setDaemon(true);
        thread.start();
    }

    public static boolean isFinished() {
        return finished;
    }

    public static int getInstalled() {
        return INSTALLED.get();
    }

    public static Throwable getLastError() {
        return lastError;
    }

    private static void installNow() throws Exception {
        Path avatarsDir = FiguraAvatars.avatarsDir();
        if (avatarsDir == null) {
            return;
        }
        Files.createDirectories(avatarsDir, new FileAttribute[0]);
        ClassLoader loader = FiguraAvatars.class.getClassLoader();
        Enumeration<URL> roots = loader.getResources(RESOURCE_ROOT);
        boolean found = false;
        while (roots.hasMoreElements()) {
            found = true;
            copyResourceRoot(roots.nextElement(), avatarsDir);
        }
        if (!found) {
            copyFromCodeSource(avatarsDir);
        }
    }

    private static void copyResourceRoot(URL root, Path avatarsDir) throws Exception {
        String protocol = root.getProtocol();
        if ("file".equalsIgnoreCase(protocol)) {
            copyDirectory(Path.of(root.toURI()).normalize(), avatarsDir);
        } else if ("jar".equalsIgnoreCase(protocol)) {
            JarURLConnection conn = (JarURLConnection)root.openConnection();
            String entryName = conn.getEntryName();
            if (entryName == null || entryName.isEmpty()) {
                entryName = RESOURCE_ROOT;
            }
            try (JarFile jar = conn.getJarFile();) {
                copyFromJar(jar, entryName, avatarsDir);
            }
        }
    }

    private static void copyFromCodeSource(Path avatarsDir) throws Exception {
        URL location = FiguraAvatars.class.getProtectionDomain().getCodeSource().getLocation();
        if (location == null) {
            return;
        }
        Path path = Path.of(location.toURI()).normalize();
        if (Files.isDirectory(path, new LinkOption[0])) {
            Path root = path.resolve(RESOURCE_ROOT).normalize();
            if (Files.isDirectory(root, new LinkOption[0])) {
                copyDirectory(root, avatarsDir);
            }
            return;
        }
        if (Files.isRegularFile(path, new LinkOption[0])) {
            try (JarFile jar = new JarFile(path.toFile());) {
                copyFromJar(jar, RESOURCE_ROOT, avatarsDir);
            }
        }
    }

    private static void copyDirectory(Path root, Path avatarsDir) throws Exception {
        if (!Files.isDirectory(root, new LinkOption[0])) {
            return;
        }
        try (Stream<Path> stream = Files.walk(root, new FileVisitOption[0]);) {
            java.util.Iterator<Path> it = stream.filter(x -> Files.isRegularFile(x, new LinkOption[0])).iterator();
            while (it.hasNext()) {
                Path file = it.next();
                copyFile(file, root.relativize(file), avatarsDir);
            }
        }
    }

    private static void copyFile(Path file, Path rel, Path avatarsDir) throws Exception {
        if (rel.isAbsolute() || rel.startsWith("..")) {
            return;
        }
        // пропускаем скрытые аватары (первый сегмент = имя каталога аватара)
        if (rel.getNameCount() > 1 && !FiguraAvatars.isCatalogAvatar(rel.getName(0).toString())) {
            return;
        }
        Path out = avatarsDir.resolve(rel).normalize();
        if (!out.startsWith(avatarsDir)) {
            return;
        }
        Files.createDirectories(out.getParent(), new FileAttribute[0]);
        if (!Files.exists(out, new LinkOption[0]) || Files.size(out) != Files.size(file)) {
            Files.copy(file, out, StandardCopyOption.REPLACE_EXISTING);
            INSTALLED.incrementAndGet();
        }
    }

    private static void copyFromJar(JarFile jar, String rootEntry, Path avatarsDir) throws Exception {
        String prefix = rootEntry.endsWith("/") ? rootEntry : rootEntry + "/";
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry == null || entry.isDirectory()) {
                continue;
            }
            String name = entry.getName();
            if (!name.startsWith(prefix)) {
                continue;
            }
            Path relPath = Path.of(name.substring(prefix.length()), new String[0]).normalize();
            if (relPath.isAbsolute() || relPath.startsWith("..")) {
                continue;
            }
            // пропускаем скрытые аватары (первый сегмент = имя каталога аватара)
            if (relPath.getNameCount() > 1 && !FiguraAvatars.isCatalogAvatar(relPath.getName(0).toString())) {
                continue;
            }
            Path out = avatarsDir.resolve(relPath).normalize();
            if (!out.startsWith(avatarsDir)) {
                continue;
            }
            Files.createDirectories(out.getParent(), new FileAttribute[0]);
            if (Files.exists(out, new LinkOption[0]) && Files.size(out) == entry.getSize()) {
                continue;
            }
            try (InputStream in = jar.getInputStream(entry);) {
                Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
            }
            INSTALLED.incrementAndGet();
        }
    }

    public static List<AvatarEntry> listAvatars() {
        ArrayList<AvatarEntry> result = new ArrayList<AvatarEntry>();
        Path avatarsDir = FiguraAvatars.avatarsDir();
        if (avatarsDir == null || !Files.isDirectory(avatarsDir, new LinkOption[0])) {
            return result;
        }
        try (Stream<Path> dirs = Files.list(avatarsDir);) {
            dirs.filter(x -> Files.isDirectory(x, new LinkOption[0])).filter(dir -> FiguraAvatars.isCatalogAvatar(dir.getFileName().toString())).sorted((a, b) -> a.getFileName().toString().compareToIgnoreCase(b.getFileName().toString())).forEach(dir -> result.add(new AvatarEntry(dir.getFileName().toString(), dir)));
        }
        catch (Exception exception) {
        }
        return result;
    }

    public static String loadAppliedKey() {
        try {
            Path path = FiguraAvatars.appliedKeyPath();
            if (path != null && Files.isRegularFile(path, new LinkOption[0])) {
                return Files.readString(path, StandardCharsets.UTF_8).trim();
            }
        }
        catch (Exception exception) {
        }
        return "";
    }

    public static void saveAppliedKey(String key) {
        try {
            Path path = FiguraAvatars.appliedKeyPath();
            if (path == null) {
                return;
            }
            Files.createDirectories(path.getParent(), new FileAttribute[0]);
            Files.writeString(path, (CharSequence)(key == null ? "" : key), StandardCharsets.UTF_8);
        }
        catch (Exception exception) {
        }
    }

    public static String applyAvatar(Path dir) {
        try {
            Class<?> manager = Class.forName("org.figuramc.figura.avatar.AvatarManager");
            for (Method m : manager.getDeclaredMethods()) {
                if (!m.getName().equals("loadLocalAvatar") || !Modifier.isStatic(m.getModifiers())) {
                    continue;
                }
                Class<?>[] params = m.getParameterTypes();
                if (params.length != 1 || !Path.class.isAssignableFrom(params[0])) {
                    continue;
                }
                m.setAccessible(true);
                m.invoke(null, dir);
                return null;
            }
            return "Figura API not found";
        }
        catch (ClassNotFoundException e) {
            return "Figura mod is not installed";
        }
        catch (Exception e) {
            return "Error: " + e.getClass().getSimpleName();
        }
    }

    public static String clearAvatar() {
        try {
            Class<?> manager = Class.forName("org.figuramc.figura.avatar.AvatarManager");
            for (Method m : manager.getDeclaredMethods()) {
                if (!m.getName().equals("loadLocalAvatar") || !Modifier.isStatic(m.getModifiers())) {
                    continue;
                }
                Class<?>[] params = m.getParameterTypes();
                if (params.length != 2 || !Path.class.isAssignableFrom(params[0]) || params[1] != Boolean.TYPE) {
                    continue;
                }
                m.setAccessible(true);
                m.invoke(null, Path.of("none", new String[0]), true);
                break;
            }
            for (Method m : manager.getDeclaredMethods()) {
                String name = m.getName().toLowerCase();
                if ((!name.contains("clear") && !name.contains("reset") && !name.contains("remove")) || !Modifier.isStatic(m.getModifiers())) {
                    continue;
                }
                m.setAccessible(true);
                m.invoke(null, new Object[0]);
                break;
            }
            return null;
        }
        catch (ClassNotFoundException e) {
            return "Figura mod is not installed";
        }
        catch (Exception e) {
            return "Error: " + e.getClass().getSimpleName();
        }
    }

    public record AvatarEntry(String name, Path dir) {
    }
}
