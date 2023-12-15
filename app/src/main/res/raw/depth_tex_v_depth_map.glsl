precision highp float;
uniform mat4 uMVPMatrix;
attribute vec4 aShadowPosition;

void main() {
	gl_Position = uMVPMatrix * aShadowPosition;
}