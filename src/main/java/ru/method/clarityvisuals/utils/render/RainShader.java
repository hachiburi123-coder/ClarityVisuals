package ru.method.clarityvisuals.utils.render;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

final class RainShader {
    private final int id;
    private final Map<String, Integer> uniformCache = new HashMap<>();

    RainShader(String vshPath, String fshPath) {
        String vsh = read(vshPath);
        String fsh = read(fshPath);
        int v = compile(vsh, GL20.GL_VERTEX_SHADER);
        int f = compile(fsh, GL20.GL_FRAGMENT_SHADER);
        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, v);
        GL20.glAttachShader(program, f);
        GL20.glLinkProgram(program);
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == 0) {
            String log = GL20.glGetProgramInfoLog(program);
            GL20.glDeleteProgram(program);
            GL20.glDeleteShader(v);
            GL20.glDeleteShader(f);
            throw new IllegalStateException("Failed to link rain shader: " + log);
        }
        GL20.glDetachShader(program, v);
        GL20.glDetachShader(program, f);
        GL20.glDeleteShader(v);
        GL20.glDeleteShader(f);
        this.id = program;
    }

    private static String read(String path) {
        try (InputStream in = RainShader.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Missing shader: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read shader: " + path, e);
        }
    }

    private static int compile(String src, int type) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, src);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == 0) {
            String log = GL20.glGetShaderInfoLog(shader);
            GL20.glDeleteShader(shader);
            throw new IllegalStateException("Failed to compile rain shader: " + log);
        }
        return shader;
    }

    void bind() {
        GL20.glUseProgram(this.id);
    }

    void unbind() {
        GL20.glUseProgram(0);
    }

    void delete() {
        GL20.glDeleteProgram(this.id);
    }

    void setUniform1i(String name, int value) {
        GL20.glUniform1i(this.uniform(name), value);
    }

    void setUniform1f(String name, float value) {
        GL20.glUniform1f(this.uniform(name), value);
    }

    void setUniform4fv(String name, float[] values) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            GL20.glUniform4fv(this.uniform(name), stack.floats(values));
        }
    }

    void setUniform2f(String name, float x, float y) {
        GL20.glUniform2f(this.uniform(name), x, y);
    }

    void setUniform3f(String name, float x, float y, float z) {
        GL20.glUniform3f(this.uniform(name), x, y, z);
    }

    void setUniformMatrix4f(String name, Matrix4f mat) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            GL20.glUniformMatrix4fv(this.uniform(name), false, mat.get(stack.mallocFloat(16)));
        }
    }

    private int uniform(String name) {
        return this.uniformCache.computeIfAbsent(name, n -> GL20.glGetUniformLocation(this.id, n));
    }
}
