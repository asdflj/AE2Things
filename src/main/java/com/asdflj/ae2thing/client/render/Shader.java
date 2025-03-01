package com.asdflj.ae2thing.client.render;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

/*
 * copy from xiaoxing2005-Korosensei-s-Wonderful-Tools
 * https://github.com/xiaoxing2005/xiaoxing2005-Korosensei-s-Wonderful-Tools
 */

public class Shader {

    public boolean isLoader = false;
    private int program;
    public final int mode;
    public String name;
    public String domain;
    public String vertShaderFilename;
    public String fragShaderFilename;
    public static ArrayList<Shader> allShaderProgram = new ArrayList<>();

    public Shader(String name, String vertShaderFilename, String fragShaderFilename) {
        this.name = name;
        this.domain = name;
        int program;
        try {
            this.vertShaderFilename = vertShaderFilename;
            this.fragShaderFilename = fragShaderFilename;
            program = createProgram(this, domain, vertShaderFilename, fragShaderFilename);
        } catch (Exception e) {
            isLoader = false;
            program = 0;
        }
        allShaderProgram.add(this);
        this.mode = 0;
        this.program = program;
    }

    private static int createProgram(Shader shaderProgram, String domain, String vertShaderFilename,
        String fragShaderFilename) {
        if (!OpenGlHelper.shadersSupported) {
            return 0;
        }

        final int program = GL20.glCreateProgram();

        final int vertShader = loadAndCompileShader(
            shaderProgram,
            program,
            domain,
            vertShaderFilename,
            GL20.GL_VERTEX_SHADER);
        final int fragShader = loadAndCompileShader(
            shaderProgram,
            program,
            domain,
            fragShaderFilename,
            GL20.GL_FRAGMENT_SHADER);

        if (vertShader != 0) GL20.glAttachShader(program, vertShader);
        if (fragShader != 0) GL20.glAttachShader(program, fragShader);

        GL20.glLinkProgram(program);

        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            shaderProgram.isLoader = false;
            GL20.glDeleteProgram(program);
            return 0;
        }

        GL20.glValidateProgram(program);

        if (GL20.glGetProgrami(program, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
            shaderProgram.isLoader = false;
            GL20.glDeleteProgram(program);
            return 0;
        }
        shaderProgram.isLoader = vertShader != 0 && fragShader != 0;
        GL20.glDeleteShader(vertShader);
        GL20.glDeleteShader(fragShader);

        return program;
    }

    private static int loadAndCompileShader(Shader shaderProgram, int program, String domain, String filename,
        int shaderType) {
        if (filename == null) {
            return 0;
        }

        final int shader = GL20.glCreateShader(shaderType);

        if (shader == 0) {
            shaderProgram.isLoader = false;
            return 0;
        }

        final String code = loadFile(shaderProgram, new ResourceLocation(domain, filename));
        if (code == null) {
            GL20.glDeleteShader(shader);
            shaderProgram.isLoader = false;
            return 0;
        }

        GL20.glShaderSource(shader, code);
        GL20.glCompileShader(shader);

        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            shaderProgram.isLoader = false;
            GL20.glDeleteShader(shader);
            return 0;
        }

        return shader;
    }

    private static String loadFile(Shader shaderProgram, ResourceLocation resourceLocation) {
        try {
            final StringBuilder code = new StringBuilder();
            final InputStream inputStream = Minecraft.getMinecraft()
                .getResourceManager()
                .getResource(resourceLocation)
                .getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                code.append(line);
                code.append('\n');
            }
            reader.close();

            return code.toString();
        } catch (Exception e) {
            shaderProgram.isLoader = false;
        }

        return null;
    }

    public int getProgram() {
        return this.program;
    }

    public void use() {
        GL20.glUseProgram(this.program);
    }

    public void clear() {
        GL20.glUseProgram(0);
    }

}
