#pragma OPENCL EXTENSION cl_khr_3d_image_writes : enable
/**
    Sampler
*/
__constant sampler_t normalizedSampler = CLK_NORMALIZED_COORDS_TRUE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
__constant sampler_t regularSampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

/**
    Solids
*/
typedef struct {
    float3 position;
    float radius;
} Sphere;

/**
    Materials
*/
typedef struct {
    float3 diffuse;
    float metallic;
    float roughness;
} Material;

/**
    Hit
*/
typedef struct {
    float t;
    float3 position;
    float3 normal;
    bool collides;
} HitInfo;

/**
    RAY
*/

typedef struct {
    float3 origin;
    float3 direction;
} Ray;

float3 pointAtRayT(const Ray* ray, float t) {
    return ray->origin + ray->direction * t;
}

/**
    Random
*/

int rand(unsigned int* seed) {
    unsigned int x = *seed;
    x ^= x << 13;
    x ^= x >> 17;
    x ^= x << 15;
    *seed = x;
    return x;
}

float randFloat(unsigned int* seed) {
    return (float) rand(seed) / UINT_MAX;
}

float3 randInUnitSphere(unsigned int* seed) {
    float3 p;
    do {
        p = (float3) (randFloat(seed), randFloat(seed), randFloat(seed));
    } while(dot(p, p) >= 1.0);
    return p;
}

/**
    Color util
*/

int pixelPack(float4 rgb) {
    int ai = floor(rgb.w * 255);
    int ri = floor(rgb.x * 255);
    int gi = floor(rgb.y * 255);
    int bi = floor(rgb.z * 255);
    return ((ai & 0xFF) << 24) | ((ri & 0xFF) << 16) | ((gi & 0xFF) << 8) | (bi & 0xFF);
}

float4 pixelUnpack(unsigned int pixel) {
    float a = ((pixel >> 24) & 0xFF) / (float) 0xFF;
    float r = ((pixel >> 16) & 0xFF) / (float) 0xFF;
    float g = ((pixel >> 8) & 0xFF) / (float) 0xFF;
    float b = ((pixel) & 0xFF) / (float) 0xFF;
    return (float4) (r, g, b, a);
}

/**
    Ray tracing
*/

bool hitSphere(const Sphere* sphere, const Ray* ray, HitInfo* hit) {

    float3 oc = ray->origin - sphere->position;
    float a = dot(ray->direction, ray->direction);
    float b = 2.0 * dot(oc, ray->direction);
    float c = dot(oc, oc) - sphere->radius * sphere->radius;

    float disc = b*b - 4*a*c;

    if(disc > 0) {
        float t1 = (-b -sqrt(disc)) / (2.0*a);
        float t2 = (-b +sqrt(disc)) / (2.0*a);
        float t = min(t1, t2);
        if(t > 0 && t < FLT_MAX) {
            hit->position = pointAtRayT(ray, t);
            hit->normal = (hit->position - sphere->position) / sphere->radius;
            return true;
        }
    }
    
    return false;

}

bool getCollision(const Ray* ray, HitInfo* hit) {
    Sphere sphere;
    sphere.position = (float3) (0.0, 0.0, -1.0);
    sphere.radius = 0.5;
    return hitSphere(&sphere, ray, hit);
}

float3 reflect(float3 vec, float3 normal) {
    return vec - 2 * dot(vec, normal) * normal;
}

float4 getEnvironmentColor(float3 direction, image2d_t environment) {
    float u = atan2(direction.z, direction.x) / (2 * M_PI_F) * 0.5f + 0.5f;
    float v = 1.0f - (direction.y * 0.5f + 0.5f);
    
    return read_imagef(environment, normalizedSampler, (float2) (u, v));

//    return (float3) (u, v, 1.0f);
}

float4 shootRay(float3 origin, float3 direction, __read_only image2d_t environment, bool useEnvironment, unsigned int* seed) {
    HitInfo hit;
    Ray ray;
    ray.origin = origin;
    ray.direction = direction;
    
    float4 color = (float4) (1.0f);
    for(int bounce = 0; bounce < 50 && getCollision(&ray, &hit); bounce++) {
        //hard coded lambertian
        ray.origin = hit.position;
        //ray.direction = fast_normalize(hit.position + hit.normal + randInUnitSphere(seed));
        ray.direction = fast_normalize(reflect(ray.direction, hit.normal) + randInUnitSphere(seed) * 0.3f);
        color *= (float4) (1.0);
    }
    
    //multiply by environment color
    if(useEnvironment) {
        color *= getEnvironmentColor(ray.direction, environment);
    } else {
        float t = ray.direction.y * (float) 0.5 + 0.5;
        float4 bottom = (float4) (1.0);
        float4 top = (float4) (0.5, 0.7, 1.0, 1.0);
        color *= bottom * (1 - t) + top * t;
    }
    
    return color;
}

__kernel void render(image2d_t environment, unsigned int useEnv, image3d_t result, __global const unsigned int* seeds) {

    size_t width = get_global_size(0);
    size_t height = get_global_size(1);
    size_t samples = get_global_size(2);
    
    size_t i = get_global_id(0);
    size_t j = get_global_id(1);
    size_t k = get_global_id(2);
    
    unsigned int seed = seeds[j * width + i] * k;
    unsigned int index = k * width * height + j * width + i;
    bool useEnvironment = useEnv > 0;
    
    //Flip y-axis (origin will be in bottom left)
    j = height - j - 1;
    
    //Camera stuff
    const float aspect = (float) width / height;
    const float3 origin = (float3) (0.0, 0.0, 0.0);
    const float3 corner = (float3) (-1.0 * aspect, -1.0, -1.0);
    const float3 hor = (float3) (2.0 * aspect, 0.0, 0.0);
    const float3 ver = (float3) (0.0, 2.0, 0.0);

    //Get ray direction
    const float u = ((float) i + randFloat(&seed)) / width;
    const float v = ((float) j + randFloat(&seed)) / height;

    //const float3 raydir = corner + hor * u + ver * v;
    float4 color = shootRay(origin, corner + hor * u + ver * v, environment, useEnvironment, &seed);
    
    write_imagef(result, (int4) (i, j, k, 0), color);
    
}



/**
    Downsampling kernel
*/


__kernel void downsample(const int samples, image3d_t image, image2d_t result) {
    
    size_t width = get_global_size(0);
    size_t height = get_global_size(1);
    
    size_t i = get_global_id(0);
    size_t j = get_global_id(1);
    
    //const int samples = *samplesPtr;
    
    float4 color = (float4) (0.0);
    for(int k = 0; k < samples; k++) {
        color += read_imagef(image, regularSampler, (int4) (i, j, k, 0));
        //color += pixelUnpack(image[k * width * height + j * width + i]);
    }
    
    color /= samples;
    
    //result[j * width + i] = pixelPack(color);
    write_imagef(result, (int2) (i, j), color);
    
}