uniform mat4 uMVPMatrix;
uniform mat4 uMVMatrix;
uniform mat4 uNormalMatrix;
uniform mat4 uShadowProjMatrix;
attribute vec4 aPosition;
attribute vec4 aColor;
attribute vec3 aNormal;
varying vec3 vPosition;      		
varying vec4 vColor;          		
varying vec3 vNormal;
varying vec4 vShadowCoord;

void main() {
	vPosition = vec3(uMVMatrix * aPosition);
	vColor = aColor;
	vNormal = vec3(uNormalMatrix * vec4(aNormal, 0.0));
	vShadowCoord = uShadowProjMatrix * aPosition;
	gl_Position = uMVPMatrix * aPosition;                     
}