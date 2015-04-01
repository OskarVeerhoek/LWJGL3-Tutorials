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

package rendering_03;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Put description here.
 *
 * @author Oskar Veerhoek
 */
public class Rendering {

    private static GLFWErrorCallback errorCallback;
    private static long windowID;

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
                640, 480,   // Width and height of the drawing canvas in pixels
                "Simple Rendering",     // Title of the window
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

        // Set the background colour of OpenGL. Everything will be reset to this color once you call glClear(GL_COLOR_BUFFER_BIT).

        glClearColor(
                0, // red component, from 0 to 1
                0, // green component, from 0 to 1
                0, // blue component, from 0 to 1
                1);// alpha/transparency component, from 0 to 1

        // Enter the state that is required for modify the projection. Note that, in contrary to Java2D, the vertex
        // coordinate system does not have to be equal to the window coordinate space. The invocation to glOrtho creates
        // a 2D vertex coordinate system like this:
        // Upper-Left:  (0,0)   Upper-Right:  (640,0)
        // Bottom-Left: (0,480) Bottom-Right: (640,480)
        // If you skip the glOrtho method invocation, the default 2D projection coordinate space will be like this:
        // Upper-Left:  (-1,+1) Upper-Right:  (+1,+1)
        // Bottom-Left: (-1,-1) Bottom-Right: (+1,-1)
        glMatrixMode(GL_PROJECTION);
        glOrtho(0, 640, 480, 0, 1, -1);
        glMatrixMode(GL_MODELVIEW);
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
        // ">>" denotes a possibly modified piece of OpenGL documentation (http://www.opengl.org/sdk/docs/man/)
        // >> glBegin and glEnd delimit the vertices that define a primitive or a group of like primitives.
        // >> glBegin accepts a single argument that specifies how the vertices are interpreted.
        // All upcoming vertex calls will be taken as points of a quadrilateral until glEnd is called. Since
        // this primitive requires four vertices, we will have to call glVertex four times.
        glBegin(GL_QUADS);
        // >> glVertex commands are used within glBegin/glEnd pairs to specify point, line, and polygon vertices.
        // >> glColor sets the current colour. (All subsequent calls to glVertex will be assigned this colour)
        // >> The number after 'glVertex'/'glColor' indicates the amount of components. (xyzw/rgba)
        // >> The character after the number indicates the type of arguments.
        // >>      (for 'glVertex' = d: Double, f: Float, i: Integer)
        // >>      (for 'glColor'  = d: Double, f: Float, b: Signed Byte, ub: Unsigned Byte)
        glColor3f(1.0f, 0.0f, 0.0f);                    // Pure Green
        glVertex2i(0, 0);                               // Upper-left
        glColor3b((byte) 0, (byte) 127, (byte) 0);      // Pure Red
        glVertex2d(640.0, 0.0);                         // Upper-right
        glColor3ub((byte) 255, (byte) 255, (byte) 255); // White
        glVertex2f(640.0f, 480.0f);                     // Bottom-right
        glColor3d(0.0d, 0.0d, 1.0d);                    // Pure Blue
        glVertex2i(0, 480);                             // Bottom-left
        // If we put another four calls to glVertex2i here, a second quadrilateral will be drawn.
        glEnd();
        // Swaps the front and back framebuffers, this is a very technical process which you don't necessarily
        // need to understand. You can simply see this method as updating the window contents.
        glfwSwapBuffers(windowID);
    }

    private static void cleanUp() {
        // It's important to release the resources when the program has finished to prevent dreadful memory leaks
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
