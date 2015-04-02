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

package test_01;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Code is based on the following sample code: http://www.lwjgl.org/guide
 */
public class DisplayTest {

    private static GLFWErrorCallback errorCallback;
    private static long windowID;

    public static void main(String[] args) {
        // Set the error handling code: all GLFW errors will be printed to the system error stream (just like println)
        errorCallback = Callbacks.errorCallbackPrint(System.err);
        glfwSetErrorCallback(errorCallback);

        // Initialize GLFW:
        int glfwInitializationResult = glfwInit(); // initialize GLFW and store the result (pass or fail)
        if (glfwInitializationResult == GL_FALSE)
            throw new IllegalStateException("GLFW initialization failed");

        // Configure the GLFW window
        windowID = glfwCreateWindow(
                640, 480,   // Width and height of the drawing canvas in pixels
                "Test",     // Title of the window
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

        // Enter the update loop: keep refreshing the window as long as the window isn't closed
        while (glfwWindowShouldClose(windowID) == GL_FALSE) {
            // Clear the contents of the window (try disabling this and resizing the window – fun guaranteed)
            glClear(GL_COLOR_BUFFER_BIT);
            // Swaps the front and back framebuffers, this is a very technical process which you don't necessarily
            // need to understand. You can simply see this method as updating the window contents.
            glfwSwapBuffers(windowID);
            // Polls the user input. This is very important, because it prevents your application from becoming unresponsive
            glfwPollEvents();
        }

        // It's important to release the resources when the program has finished to prevent dreadful memory leaks
        glfwDestroyWindow(windowID);
        // Destroys all remaining windows and cursors (LWJGL JavaDoc)
        glfwTerminate();
    }
}
