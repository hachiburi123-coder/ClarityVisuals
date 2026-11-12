#version 150

uniform sampler2D InSampler;

uniform float Saturation;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 color = texture(InSampler, texCoord);
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    // Clamp is required: with strength > 1 vivid/HDR sky pixels get pushed
    // far outside [0,1], which turns into acid colors and white/black crush.
    color.rgb = clamp(mix(vec3(gray), color.rgb, Saturation), 0.0, 1.0);
    fragColor = color;
}
