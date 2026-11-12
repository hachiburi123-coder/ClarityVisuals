#version 150

in vec3 Position;

out vec2 vNDC;

void main() {
    gl_Position = vec4(Position.xy, 1.0, 1.0);
    vNDC = Position.xy;
}
