#version 330 core

smooth in vec4 fragment_colour;
out vec4 fragColor;

void main()
{
    fragColor = fragment_colour;
}