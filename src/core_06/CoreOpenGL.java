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

package core_06;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.system.MemoryUtil;
import utility.ShaderLoader;

import java.nio.DoubleBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Put description here.
 *
 * @author Oskar Veerhoek
 */
public class CoreOpenGL {

    private static final int VERTEX_POSITION = 0, COLOUR_POSITION = 1;
    private static GLFWErrorCallback errorCallback;
    private static long windowID;
    private static int vertexArrayObject;
    private static int vertexBufferObject;
    private static int indexBufferObject;
    private static int shaderProgram;
    private static DoubleBuffer vertexData = BufferUtils.createDoubleBuffer(20);
    private static ShortBuffer indexData = BufferUtils.createShortBuffer(6);

    static {
        vertexData.put(new double[]{
                -1.0, -1.0, // 0
                +1.0, -1.0, // 1
                +1.0, +1.0, // 2
                -1.0, +1.0, // 3
                1.0, 0.0, 0.0, // 0
                0.0, 1.0, 0.0, // 1
                0.0, 0.0, 1.0, // 2
                1.0, 1.0, 1.0  // 3
        });
        vertexData.flip();
        indexData.put(new short[]{
                0, 1, 2,
                0, 2, 3
        });
        indexData.flip();
    }

    private static void setUp() {
        boolean glfwInitializationResult = glfwInit() == GL11.GL_TRUE;

        if (glfwInitializationResult == false)
            throw new IllegalStateException("GLFW initialization failed");

        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE); // Mac Modern OpenGL
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE); // Mac Modern OpenGL
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3); // Modern OpenGL
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2); // Mac Modern OpenGL

        windowID = glfwCreateWindow(640, 480, "Test", MemoryUtil.NULL, MemoryUtil.NULL);

        if (windowID == MemoryUtil.NULL)
            throw new IllegalStateException("GLFW window creation failed");

        glfwMakeContextCurrent(windowID); // Links the OpenGL context of the window to the currrent thread
        glfwSwapInterval(1); // Enable VSync
        glfwShowWindow(windowID);

        // If you don't add this line, you'll get the following exception:
        //  java.lang.IllegalStateException: There is no OpenGL context current in the current thread.
        GLContext.createFromCurrent(); // Links LWJGL to the OpenGL context

        glClearColor(0, 0, 0, 1);

        vertexArrayObject = glGenVertexArrays();
        vertexBufferObject = glGenBuffers();
        indexBufferObject = glGenBuffers();

        glBindVertexArray(vertexArrayObject);

        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);

        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexData, GL_STATIC_DRAW);

        glEnableVertexAttribArray(VERTEX_POSITION);
        glEnableVertexAttribArray(COLOUR_POSITION);

        glVertexAttribPointer(VERTEX_POSITION, 2, GL_DOUBLE, false, 0, 0);
        glVertexAttribPointer(COLOUR_POSITION, 3, GL_DOUBLE, false, 0, 8 * 4 * 2);

        shaderProgram = ShaderLoader.loadShaderPair("res/shader.vs", "res/shader.fs");
        glUseProgram(shaderProgram);
    }

    private static void enterUpdateLoop() {
        while (glfwWindowShouldClose(windowID) == GL_FALSE) {
            draw();
            // Polls the user input. This is very important, because it prevents your application from becoming unresponsive
            glfwPollEvents();
        }
    }

    private static void draw() {
        glClear(GL_COLOR_BUFFER_BIT);

        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);

        glfwSwapBuffers(windowID);
    }

    private static void cleanUp() {
        glDeleteVertexArrays(vertexArrayObject);
        glDeleteBuffers(vertexBufferObject);
        glDeleteBuffers(indexBufferObject);
        glDeleteProgram(shaderProgram);
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
