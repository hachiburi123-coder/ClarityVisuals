#version 330 core

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec2 aUv;

uniform mat4 u_Proj;
uniform mat4 u_View;

out vec2 vUv;

void main() {
    vUv = aUv;
    gl_Position = u_Proj * u_View * vec4(aPos, 1.0);
}
