#version 330 core
precision highp float;
uniform vec2 iResolution;
uniform vec2 iMouse;
uniform float scaleFactor;
#define INNER_RADIUS 20.0
#define OUTER_RADIUS 50.0
#define SECTOR_COUNT 6
#define BORDER_AA 1.5

float annularMask(vec2 uv) {
    float dist = length(uv);
    float innerRadius = INNER_RADIUS * scaleFactor;
    float outerRadius = OUTER_RADIUS * scaleFactor;

    float inner = smoothstep(innerRadius - BORDER_AA, innerRadius + BORDER_AA, dist);
    float outer = smoothstep(outerRadius + BORDER_AA, outerRadius - BORDER_AA, dist);
    return inner * outer;
}

void main() {
    vec2 center = iResolution.xy * 0.5;
    vec2 fragUV = gl_FragCoord.xy - center;

    float mask = annularMask(fragUV);
    if (mask <= 0.0) {
        discard;
    }

    float angle = atan(fragUV.y, fragUV.x) + 3.1415926535;
    float sectorSize = 6.2831853071 / float(SECTOR_COUNT);
    int sectorID = int(angle / sectorSize);

    float sectorPos = mod(angle, sectorSize);
    float borderDist = min(sectorPos, sectorSize - sectorPos) * length(fragUV);
    float borderAlpha = smoothstep(BORDER_AA, -BORDER_AA, borderDist);

    vec2 mouseUV = iMouse.xy - center;
    mouseUV = vec2(mouseUV.x,mouseUV.y);
    int highlightSector = int((atan(mouseUV.y, mouseUV.x) + 3.1415926535) / sectorSize);

    vec3 color = vec3(0.5);
    if(sectorID == highlightSector){
        float leng = length(mouseUV);
        if(leng > INNER_RADIUS * scaleFactor){
            color = mix(vec3(0.8), vec3(1.0), smoothstep(0.0, 10.0, borderDist));
        }
    }

    color = mix(color, vec3(0.2), borderAlpha);

    gl_FragColor = vec4(color, mask);
}
