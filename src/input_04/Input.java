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

package input_04;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Press space to view fully coloured triangle
 * Move mouse to change triangle colour
 *
 * @author Oskar Veerhoek
 */
public class Input {

    private static GLFWErrorCallback errorCallback;
    private static long windowID;

    private static boolean inputEnabled = true;
    private static int mouseX = 0, mouseY = 0;
    private static GLFWCursorPosCallback cursorCallback;

    private static void setUp() {
        // Initialize GLFW:
        int glfwInitializationResult = glfwInit(); // initialize GLFW and store the result (pass or fail)
        if (glfwInitializationResult == GL_FALSE)
            throw new IllegalStateException("GLFW initialization failed");

        // Configure the GLFW window
        windowID = glfwCreateWindow(
                640, 480,   // Width and height of the drawing canvas in pixels
                "Simple Mouse/Keyboard Input",     // Title of the window
                MemoryUtil.NULL, // Monitor ID to use for fullscreen mode, or NULL to use windowed mode (LWJGL JavaDoc)
                MemoryUtil.NULL); // Window to share resources with, or NULL to not share resources (LWJGL JavaDoc)

        if (windowID == MemoryUtil.NULL)
            throw new IllegalStateException("GLFW window creation failed");

        glfwMakeContextCurrent(windowID); // Links the OpenGL context of the window to the current thread (GLFW_NO_CURRENT_CONTEXT error)
        glfwSwapInterval(1); // Enable VSync, which effective caps the frame-rate of the application to 60 frames-per-second
        glfwShowWindow(windowID);
        glfwSetCursorPosCallback(windowID, cursorCallback = new GLFWCursorPosCallback() {

            @Override
            public void invoke(long window, double xpos, double ypos) {
                mouseX = (int) xpos;
                mouseY = (int) ypos;
            }
        });


        // If you don't add this line, you'll get the following exception:
        //  java.lang.IllegalStateException: There is no OpenGL context current in the current thread.
        GLContext.createFromCurrent(); // Links LWJGL to the OpenGL context

        // Set the background colour of OpenGL. Everything will be reset to this color once you call glClear(GL_COLOR_BUFFER_BIT).
        glClearColor(0.2f, 0.2f, 0.2f, 1);
    }

    private static void enterUpdateLoop() {
        while (glfwWindowShouldClose(windowID) == GL_FALSE) {
            draw();
            input();
            // Polls the user input. This is very important, because it prevents your application from becoming unresponsive
            glfwPollEvents();
        }
    }

    private static void draw() {
        glClear(GL_COLOR_BUFFER_BIT);

        glBegin(GL_TRIANGLES);

        if (inputEnabled) {
            float red = 1 - (float) mouseY / 480;
            float green = 1 - (float) mouseX / 640 - red;
            float blue = (float) mouseX / 640 - red;

            glColor3f(red, 0, 0);
            glVertex2f(0, 0.75f);
            glColor3f(0, green, 0);
            glVertex2f(-0.75f, -0.75f);
            glColor3f(0, 0, blue);
            glVertex2f(0.75f, -0.75f);
        } else {
            glColor3f(1, 0, 0);
            glVertex2f(0, 0.75f);
            glColor3f(0, 1, 0);
            glVertex2f(-0.75f, -0.75f);
            glColor3f(0, 0, 1);
            glVertex2f(0.75f, -0.75f);
        }

        glEnd();

        // Swaps the front and back framebuffers, this is a very technical process which you don't necessarily
        // need to understand. You can simply see this method as updating the window contents.
        glfwSwapBuffers(windowID);
    }

    private static void input() {
        if (glfwGetKey(windowID, GLFW_KEY_SPACE) == GLFW_PRESS) {
            inputEnabled = false;
        } else {
            inputEnabled = true;
        }
        System.out.println(mouseX + ", " + mouseY);
    }

    private static void cleanUp() {
        glfwDestroyWindow(windowID);
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
