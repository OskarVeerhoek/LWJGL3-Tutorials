#version 330 core

layout(location = 0) in vec4 vertex_position;
layout(location = 1) in vec4 vertex_colour;

smooth out vec4 fragment_colour;

void main()
{
    fragment_colour = vertex_colour;
    gl_Position = vertex_position;
}