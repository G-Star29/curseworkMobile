precision mediump float;

uniform vec3 uLightPos;
uniform sampler2D uShadowTexture;
uniform float uxPixelOffset;
uniform float uyPixelOffset;
varying vec3 vPosition;
varying vec4 vColor;
varying vec3 vNormal;
varying vec4 vShadowCoord;

float shadowSimple(){
	vec4 shadowMapPosition = vShadowCoord / vShadowCoord.w;
	float distanceFromLight = texture2D(uShadowTexture, shadowMapPosition.st).z;
	float bias = 0.001;
	return float(distanceFromLight > shadowMapPosition.z - bias);
}
  
void main() {
	vec3 lightVec = uLightPos - vPosition;
	lightVec = normalize(lightVec);
	float specular = pow(max(dot(vNormal, lightVec), 0.0), 5.0);
	float diffuse = max(dot(vNormal, lightVec), 0.1);
	float ambient = 0.3;
   	float shadow = 1.0;
		if (vShadowCoord.w > 0.0) {
			shadow = shadowSimple();
			shadow = (shadow * 0.9) + 0.9;
		}
    gl_FragColor = (vColor * (diffuse + ambient + specular) * shadow);
}                                                                     	
