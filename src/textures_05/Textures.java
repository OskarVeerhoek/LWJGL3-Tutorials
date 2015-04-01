/*
 * Copyright (c) 2015, Oskar Veerhoek
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */

package textures_05;

import de.matthiasmann.twl.utils.PNGDecoder;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.system.MemoryUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Put description here.
 *
 * @author Oskar Veerhoek
 */
public class Textures {

    private static GLFWErrorCallback errorCallback;
    private static long windowID;

    private static int textureID;
    private static PNGDecoder textureDecoder;
    private static ByteBuffer textureData;

    private static void render() {
        glClear(GL_COLOR_BUFFER_BIT);

        glBindTexture(GL_TEXTURE_2D, textureID);

        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex2i(-1, 1); // Upper-left
        glTexCoord2f(1, 0);
        glVertex2i(1, 1); // Upper-right
        glTexCoord2f(1, 1);
        glVertex2i(1, -1); // Bottom-right
        glTexCoord2f(0, 1);
        glVertex2i(-1, -1); // Bottom-left
        glEnd();

        glBindTexture(GL_TEXTURE_2D, 0);

        glfwSwapBuffers(windowID);
    }

    private static void cleanUp() {
        glfwDestroyWindow(windowID);
        glfwTerminate();
    }

    private static void setUpTextures() {

        // Novel Java 7 way of handling exception with try-with-resources

        try (InputStream inputStream = new FileInputStream("res/texture.png")) {
            textureDecoder = new PNGDecoder(inputStream);
            textureData = BufferUtils.createByteBuffer(4 * textureDecoder.getWidth() * textureDecoder.getHeight());
            textureDecoder.decode(textureData, textureDecoder.getWidth() * 4, PNGDecoder.Format.RGBA);
            textureData.flip();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void setUpOpenGL() {
        GLContext.createFromCurrent();
        // Set up textures
        glEnable(GL_TEXTURE_2D);

        textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, textureDecoder.getWidth(), textureDecoder.getHeight(), 0, GL_RGBA,
                GL_UNSIGNED_BYTE, textureData);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private static void update() {
        glfwPollEvents();
    }

    private static void enterGameLoop() {
        while (glfwWindowShouldClose(windowID) == GL_FALSE) {
            render();
            update();
        }
    }

    private static void setUpGLFW() {
        boolean glfwInitializationResult = glfwInit() == GL11.GL_TRUE;

        if (glfwInitializationResult == false)
            throw new IllegalStateException("GLFW initialization failed");

        windowID = glfwCreateWindow(500, 500, "Test", MemoryUtil.NULL, MemoryUtil.NULL);

        if (windowID == MemoryUtil.NULL)
            throw new IllegalStateException("GLFW window creation failed");

        glfwMakeContextCurrent(windowID); // Links the OpenGL context of the window to the currrent thread
        glfwSwapInterval(1); // Enable VSync
        glfwShowWindow(windowID);
    }

    public static void main(String[] args) {
        errorCallback = Callbacks.errorCallbackPrint(System.err);
        glfwSetErrorCallback(errorCallback);

        setUpGLFW();
        setUpTextures();
        setUpOpenGL();
        enterGameLoop();
        cleanUp();
    }

}
