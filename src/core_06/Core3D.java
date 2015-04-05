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

import com.joml.matrix.Matrix4f;
import com.joml.utils.CamMath;
import com.joml.vector.Vector3f;
import com.joml.vector.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.system.MemoryUtil;
import utility.ShaderLoader;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Created by TheCodingUniverse on 05/04/2015.
 */
public class Core3D {
    // The indices of the vertex position and colour attributes, we use these attributes in the vertex shader
    private static final int VERTEX_POSITION = 1, VERTEX_COLOUR = 0;
    // The error callback function for GLFW
    private static GLFWErrorCallback errorCallback;
    private static GLFWCursorPosCallback cursorCallback;
    // The window handle
    private static long windowID;
    // The Vertex Array Object (VAO):  stores the of bindings between Vertex Attributes and vertex data
    private static int vertexArrayObject;
    // The Vertex Buffer Object (VBO): stores vertex position and colour data
    private static int vertexBufferObject;
    // The Index Buffer Object (IBO): stores the indices of the data in the VBO, used by glDrawElements
    private static int indexBufferObject;
    // The OpenGL shader program handle
    private static int shaderProgram;
    private static int uniformModelviewProjection;
    // In LWJGL we store vertex and index data using Buffers, because they most resemble C/C++ data arrays
    private static DoubleBuffer vertexData = BufferUtils.createDoubleBuffer(20);
    private static ShortBuffer indexData = BufferUtils.createShortBuffer(6);
    private static Matrix4f modelviewMatrix = new Matrix4f();
    private static Matrix4f projectionMatrix = new Matrix4f();
    private static FloatBuffer mvpMatrix = BufferUtils.createFloatBuffer(16);
    private static Vector4f translate = new Vector4f(0, 0, -5, 1);
    private static int mouseX = -9999, mouseY = -9999;

    static {
        vertexData.put(new double[]{
                // Vertex Positions: each vertex position has two components, x, and y
                -1.0, -1.0, // 0, visible vertices in this projection range from (-1, -1) to (+1, +1)
                +1.0, -1.0, // 1
                +1.0, +1.0, // 2
                -1.0, +1.0, // 3
                // Colours: each colour has three components, red, green and blue
                1.0, 0.0, 0.0, // 0, colours range from 0.0 to 1.0
                0.0, 1.0, 0.0, // 1
                0.0, 0.0, 1.0, // 2
                1.0, 1.0, 1.0  // 3
        });
        vertexData.flip();
        indexData.put(new short[]{
                // Indices for the triangles: each triangle has three indices
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

        glClearColor(0, 0, 0, 1);

        // >> Vertex Array Objects (VAO) are OpenGL Objects that store the
        // >> set of bindings between Vertex Attributes and the user's source
        // >> vertex data. (http://www.opengl.org/wiki/Vertex_Array_Object)
        // >> glGenVertexArrays returns n vertex array object names in arrays.
        // Create a VAO and store the handle in vertexArrayObject
        vertexArrayObject = glGenVertexArrays();
        // >> glGenBuffers returns n buffer object names in buffers.
        // >> No buffer objects are associated with the returned buffer object names
        // >> until they are first bound by calling glBindBuffer.
        vertexBufferObject = glGenBuffers();
        indexBufferObject = glGenBuffers();

        // >> glBindVertexArray binds the vertex array object with name array.
        // Bind the VAO to OpenGL
        glBindVertexArray(vertexArrayObject);
        // >> glBindBuffer binds a buffer object to the specified buffer binding point.
        // >> Vertex Buffer Objects (VBOs) are Buffer Objects that are used for
        // >> vertex data. (VBO = GL_ARRAY_BUFFER)
        // Bind our buffer object to GL_ARRAY_BUFFER, thus making it a VBO.
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);

        // >> glBufferData creates a new data store for the buffer object currently bound
        // >> to target. Any pre-existing data store is deleted. The new data store is created
        // >> with the specified size in bytes and usage. If data is not NULL, the data
        // >> store is initialized with data from this pointer. In its initial state, the
        // >> new data store is not mapped, it has a NULL mapped pointer, and its mapped
        // >> access is GL_READ_WRITE.
        // Store the vertex data (position and colour) in the VBO.
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);
        // Store the vertex index data in the IBO.
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexData, GL_STATIC_DRAW);

        // Create a new shader program from the two files containing a vertex shader and a fragment shader.
        shaderProgram = ShaderLoader.loadShaderPair("res/perspective.vs", "res/shader.fs");
        glUseProgram(shaderProgram);
        uniformModelviewProjection = glGetUniformLocation(shaderProgram, "modelview_projection");

        CamMath.lookAt(new Vector3f(0, 0, 5), new Vector3f(0, 0, 0), new Vector3f(0, 1, 0), modelviewMatrix);
        System.out.println("Model View: ");
        System.out.println(modelviewMatrix);
        CamMath.perspective(60, 1, 0.3f, 1000, projectionMatrix);
        System.out.println("Projection: ");
        System.out.println(projectionMatrix);

        Matrix4f.mul(projectionMatrix, modelviewMatrix, mvpMatrix);
        mvpMatrix.flip();
        glUniformMatrix4(uniformModelviewProjection, false, mvpMatrix);
        mvpMatrix.flip();

        // >> glEnableVertexAttribArray enables the generic vertex attribute array specified by index.
        // >> glDisableVertexAttribArray disables the generic vertex attribute array specified by
        // >> index. By default, all client-side capabilities are disabled, including all generic
        // >> vertex attribute arrays. If enabled, the values in the generic vertex attribute array
        // >> will be accessed and used for rendering when calls are made to vertex array commands
        // >> such as glDrawArrays, glDrawElements, glDrawRangeElements, glMultiDrawElements, or glMultiDrawArrays.
        // Enable the vertex position vertex attribute.
        glEnableVertexAttribArray(VERTEX_POSITION);
        // Enable the vertex colour vertex attribute.;
        glEnableVertexAttribArray(VERTEX_COLOUR);

        glEnable(GL_DEPTH_TEST);

        // >> glVertexAttribPointer and glVertexAttribIPointer specify the location and data format of the
        // >> array of generic vertex attributes at index index to use when rendering. size specifies
        // >> the number of components per attribute and must be 1, 2, 3, 4, or GL_BGRA. type specifies
        // >> the data type of each component, and stride specifies the byte stride from one attribute
        // >> to the next, allowing vertices and attributes to be packed into a single array or stored
        // >> in separate arrays.
        // Tell OpenGL where to find the vertex position data (inside the VBO).
        glVertexAttribPointer(VERTEX_POSITION, 2, GL_DOUBLE, false, 0, 0);
        // Tell OpenGL where to find the vertex colour data (inside the VBO).
        glVertexAttribPointer(VERTEX_COLOUR, 3, GL_DOUBLE, false, 0, 8 * 4 * 2);

    }

    private static void enterUpdateLoop() {
        while (glfwWindowShouldClose(windowID) == GL_FALSE) {
            updateMatrices();
            draw();
            input();
            glfwPollEvents();
        }
    }

    private static void updateMatrices() {

        modelviewMatrix.translate(translate);

        mvpMatrix.clear();
        Matrix4f.mul(projectionMatrix, modelviewMatrix, mvpMatrix);
        mvpMatrix.flip();
        glUniformMatrix4(uniformModelviewProjection, false, mvpMatrix);
    }

    private static void draw() {
        // Clear the screen contents
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        // Draw the triangles as given to us by the IBO
        glDrawElements(
                GL_TRIANGLES, // The shape type: triangles, each consisting of three separate vertices
                6, // The number of indices: 6, 3 per triangle (and there are two triangles)
                GL_UNSIGNED_SHORT, // Data type, for OpenGL we always use GL_UNSIGNED_SHORT for DoubleBuffer (don't ask me why..)
                0); // Index offset, we want all the data so we just set this to zero
        // Refresh the GLFW window
        glfwSwapBuffers(windowID);
    }

    private static void input() {
        if (glfwGetKey(windowID, GLFW_KEY_LEFT) == GLFW_PRESS) {
            translate.x += 0.01f;
        } else if (glfwGetKey(windowID, GLFW_KEY_RIGHT) == GLFW_PRESS) {
            translate.x -= 0.01f;
        }
        if (glfwGetKey(windowID, GLFW_KEY_UP) == GLFW_PRESS) {
            translate.z += 0.01f;
        } else if (glfwGetKey(windowID, GLFW_KEY_DOWN) == GLFW_PRESS) {
            translate.z -= 0.01f;
        }
        if (glfwGetMouseButton(windowID, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {

        }
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
