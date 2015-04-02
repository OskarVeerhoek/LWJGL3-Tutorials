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
 * @author Oskar Veerhoek
 */
public class Textures {

    private static GLFWErrorCallback errorCallback;
    private static long windowID;

    private static int textureID;
    private static PNGDecoder textureDecoder;
    private static ByteBuffer textureData;

    private static void setUp() {
        // Set the error handling code: all GLFW errors will be printed to the system error stream (just like println)
        errorCallback = Callbacks.errorCallbackPrint(System.err);
        glfwSetErrorCallback(errorCallback);

        // Initialize GLFW:
        int glfwInitializationResult = glfwInit(); // initialize GLFW and store the result (pass or fail)
        if (glfwInitializationResult == GL_FALSE)
            throw new IllegalStateException("GLFW initialization failed");

        // Configure the GLFW window
        windowID = glfwCreateWindow(
                500, 500,   // Width and height of the drawing canvas in pixels
                "Texturing",     // Title of the window
                MemoryUtil.NULL, // Monitor ID to use for fullscreen mode, or NULL to use windowed mode (LWJGL JavaDoc)
                MemoryUtil.NULL); // Window to share resources with, or NULL to not share resources (LWJGL JavaDoc)

        if (windowID == MemoryUtil.NULL)
            throw new IllegalStateException("GLFW window creation failed");

        glfwMakeContextCurrent(windowID); // Links the OpenGL context of the window to the current thread (GLFW_NO_CURRENT_CONTEXT error)
        glfwSwapInterval(1); // Enable VSync, which effective caps the frame-rate of the application to 60 frames-per-second
        glfwShowWindow(windowID);

        // If you don't add this line, you'll get the following exception:
        //  java.lang.IllegalStateException: There is no OpenGL context current in the current thread.
        GLContext.createFromCurrent(); // Links LWJGL to the OpenGL context

        // Load the texture data using PNGDecoder (you can also use other libraries such as slick_util)

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

        // Set up OpenGL states
        GLContext.createFromCurrent();
        // Enable texture drawing
        glEnable(GL_TEXTURE_2D);
        // Create a texture ID
        textureID = glGenTextures();
        // Bind the texture to the TEXTURE_2D slot (there can only be one bound texture at a time)
        glBindTexture(GL_TEXTURE_2D, textureID);
        // Magnification and minification filters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        // Hand the texture data from Java to OpenGL:
        glTexImage2D(GL_TEXTURE_2D, // Texture type (1D, 2D, 3D)
                0, // Level, always set this to zero
                GL_RGBA, // Internal format, RGBA works best
                textureDecoder.getWidth(), // Width of the texture in pixels
                textureDecoder.getHeight(), // Width of the texture in pixels
                0, // Border, always set this to zero
                GL_RGBA, // Texture format, in our case this is RGBA (you can dynamically find the texture type with PNGDecoder)
                GL_UNSIGNED_BYTE, // Type of the texture data, this is always unsigned byte (this should ring a bell with C/C++ programmers)
                textureData);
        // Unbind the texture, in our program this isn't strictly necessary because we have only one texture
        // But it's a good practice for when you have multiple textures
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private static void enterUpdateLoop() {
        while (glfwWindowShouldClose(windowID) == GL_FALSE) {
            draw();
            // Polls the user input. This is very important, because it prevents your application from becoming unresponsive
            glfwPollEvents();
        }
    }

    private static void draw() {
        // Clear the contents of the window (try disabling this and resizing the window – fun guaranteed)
        glClear(GL_COLOR_BUFFER_BIT);

        glBindTexture(GL_TEXTURE_2D, textureID);  // Bind the texture

        // We assign texture coordinates to vertex coordinates, which maps the texture to an OpenGL surface
        // (0, 0) is the upper-left corner of the texture
        // (1, 0) is the upper-right corner
        // (0, 1) is the bottom-left
        // (1, 1) is the bottom-right
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

        glBindTexture(GL_TEXTURE_2D, 0); // Unbind the texture

        // Swaps the front and back framebuffers, this is a very technical process which you don't necessarily
        // need to understand. You can simply see this method as updating the window contents.
        glfwSwapBuffers(windowID);
    }

    private static void cleanUp() {
        // It's important to release the resources when the program has finished to prevent dreadful memory leaks
        glDeleteTextures(textureID);
        glfwDestroyWindow(windowID);
        // Destroys all remaining windows and cursors (LWJGL JavaDoc)
        glfwTerminate();
    }

    public static void main(String[] args) {
        errorCallback = Callbacks.errorCallbackPrint(System.err);
        glfwSetErrorCallback(errorCallback);

        setUp();
        enterUpdateLoop();
        cleanUp();
    }
}
