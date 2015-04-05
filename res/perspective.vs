#version 330 core
// If this version is not supported, try #version 150 core

uniform mat4 modelview_projection;

layout(location = 0) in vec4 vertex_colour;
layout(location = 1) in vec4 vertex_position;
// remove layout(..) if the version is lower than #version 330 core

smooth out vec4 fragment_colour;

void main()
{
    fragment_colour = vertex_colour;
    gl_Position = modelview_projection * vertex_position;
}