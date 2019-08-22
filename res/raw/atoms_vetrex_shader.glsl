attribute highp vec4 vertex;
attribute mediump vec3 normal;
uniform mediump mat4 matrix;
uniform mediump mat3 invmatrix;
uniform bool lighting;
varying mediump vec3 nor;
varying mediump vec3 N;
varying mediump vec2 uv;
void main(void){
  nor=normal;
  uv.x = abs(vertex.x); 
  uv.y = 1.0-vertex.z;
  
  if (lighting){    
    N = invmatrix * normal;
    N=normalize(N);
  }
  else{
      nor=vec3(0.5,0.5,0.5);
      //vec4 position = matrix * vertex;
  }
  gl_Position = matrix* vertex;
}