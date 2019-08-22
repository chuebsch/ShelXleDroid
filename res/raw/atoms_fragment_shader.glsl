uniform mediump vec3 col;
varying mediump vec3 nor;
varying mediump vec3 N;
uniform bool eli;
uniform bool lighting;
uniform bool text;
uniform sampler2D textureSampler;
varying mediump vec2 uv;
void main(void) {
    mediump vec3 color;
    if (text){
    gl_FragColor = texture2D(textureSampler, uv); 
    }else
    {
    if (lighting){
        highp vec4 lightDirection = normalize(vec4(-1.0, 1.0, 0.0, 0.0));
        highp float NdotL = dot(N, lightDirection.xyz);
        //highp float RdotL = dot(reflect(normalize(position), N), lightDirection.xyz);
        mediump vec3 ambient  = vec3(0.2, 0.2, 0.2);
        mediump vec3 diffuse  = vec3(0.8, 0.8, 0.8);
        color =(ambient + diffuse * max(NdotL, 0.0)) * col ;
    }else{
        color=col;
    }
    if (!eli) gl_FragColor = vec4(color,1.0);
    else {
        mediump vec3 XYZ=fract(abs(nor));
        mediump float dark=dot(col,col);
        
        if (min(XYZ.x,min(XYZ.y,XYZ.z))<0.07){ 
        if (dark<1.05) gl_FragColor = mix(vec4(color,1.0),vec4(1.0,1.0,1.0,1.0),0.8);
        else gl_FragColor = mix(vec4(color,1.0),vec4(0.0,0.0,0.0,1.0),0.8);
        }
 //       else if ((max(abs(XYZ.x-0.57),max(abs(XYZ.y-0.57),abs(XYZ.z-0.57)))<0.7)&&(dot(N,nor)>0.7)) discard;//gl_FragColor = mix(vec4(color,1.0),vec4(0.0,0.0,1.0,1.0),0.8);
        else gl_FragColor = vec4(color,1.0);
    }
    }
}
