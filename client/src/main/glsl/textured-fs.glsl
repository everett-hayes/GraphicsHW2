#version 300 es

precision highp float;

in vec2 textCoord;
out vec4 fragmentColor;

uniform struct {
    sampler2D colorTexture;
} material;

void main(void) {
  fragmentColor = texture(material.colorTexture, textCoord);
}
