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
 * Put description here.
 *
 * @author Oskar Veerhoek
 */
public class Input {

    private static GLFWErrorCallback errorCallback;
    private static long windowID;
    private static int windowWidth = 640, windowHeight = 480;

    private static boolean inputEnabled = true;
    private static int mouseX = 0, mouseY = 0;
    private static GLFWCursorPosCallback cursorCallback;

    private static void render() {
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

    private static void setUpOpenGL() {
        GLContext.createFromCurrent();
        glClearColor(0.2f, 0.2f, 0.2f, 1);
    }

    private static void update() {
        glfwPollEvents();
    }

    private static void enterGameLoop() {
        while (glfwWindowShouldClose(windowID) == GL_FALSE) {
            render();
            input();
            update();
        }
    }

    private static void setUpGLFW() {
        boolean glfwInitializationResult = glfwInit() == GL11.GL_TRUE;

        if (glfwInitializationResult == false)
            throw new IllegalStateException("GLFW initialization failed");

        windowID = glfwCreateWindow(windowWidth, windowHeight, "Test", MemoryUtil.NULL, MemoryUtil.NULL);

        if (windowID == MemoryUtil.NULL)
            throw new IllegalStateException("GLFW window creation failed");

        glfwMakeContextCurrent(windowID); // Links the OpenGL context of the window to the currrent thread
        glfwSwapInterval(1); // Enable VSync
        glfwShowWindow(windowID);

        glfwSetCursorPosCallback(windowID, cursorCallback = new GLFWCursorPosCallback() {

            @Override
            public void invoke(long window, double xpos, double ypos) {
                mouseX = (int) xpos;
                mouseY = (int) ypos;
            }
        });
    }

    public static void main(String[] args) {
        errorCallback = Callbacks.errorCallbackPrint(System.err);
        glfwSetErrorCallback(errorCallback);

        setUpGLFW();
        setUpOpenGL();
        enterGameLoop();
        cleanUp();
    }

}
