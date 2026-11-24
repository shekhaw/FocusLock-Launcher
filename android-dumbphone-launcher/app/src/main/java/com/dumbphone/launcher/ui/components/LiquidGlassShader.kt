package com.dumbphone.launcher.ui.components

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import org.intellij.lang.annotations.Language

object LiquidGlassShader {

    @Language("AGSL")
    const val AGSL_SRC = """
        uniform shader content;
        uniform float2 size;
        uniform float4 cornerRadii;
        uniform float refractionHeight;
        uniform float refractionAmount;
        uniform float depthEffect;
        uniform float chromaticAberration;
        uniform float contrast;
        uniform float whitePoint;
        uniform float chromaMultiplier;
        uniform float3 tintColor;
        uniform float tintAlpha;

        const half3 rgbToY = half3(0.2126, 0.7152, 0.0722);

        float radiusAt(float2 coord, float4 radii) {
            if (coord.x >= 0.0) {
                if (coord.y <= 0.0) return radii.y;
                else return radii.z;
            } else {
                if (coord.y <= 0.0) return radii.x;
                else return radii.w;
            }
        }

        float sdRoundedRect(float2 coord, float2 halfSize, float radius) {
            float2 cornerCoord = abs(coord) - (halfSize - float2(radius));
            float outside = length(max(cornerCoord, 0.0)) - radius;
            float inside = min(max(cornerCoord.x, cornerCoord.y), 0.0);
            return outside + inside;
        }

        float safeSign(float value) {
            return value < 0.0 ? -1.0 : 1.0;
        }

        float2 safeNormalize(float2 value, float2 fallback) {
            float len = length(value);
            if (len > 0.001) {
                return value / len;
            }
            return fallback;
        }

        float2 gradSdRoundedRect(float2 coord, float2 halfSize, float radius) {
            float2 cornerCoord = abs(coord) - (halfSize - float2(radius));
            if (cornerCoord.x >= 0.0 || cornerCoord.y >= 0.0) {
                float2 outside = max(cornerCoord, 0.0);
                float outsideLength = length(outside);
                if (outsideLength > 0.001) {
                    return sign(coord) * (outside / outsideLength);
                }
                float useX = step(cornerCoord.y, cornerCoord.x);
                return float2(
                    useX * safeSign(coord.x),
                    (1.0 - useX) * safeSign(coord.y)
                );
            } else {
                float gradX = step(cornerCoord.y, cornerCoord.x);
                return sign(coord) * float2(gradX, 1.0 - gradX);
            }
        }

        float circleMap(float x) {
            return 1.0 - sqrt(max(0.0, 1.0 - x * x));
        }

        half4 saturateColor(half4 color, float amount) {
            half3 lin = toLinearSrgb(color.rgb);
            float y = dot(lin, rgbToY);
            half3 gray = half3(y);
            half3 sat = fromLinearSrgb(mix(gray, lin, amount));
            return half4(sat, color.a);
        }

        half4 main(float2 coord) {
            float2 halfSize = size * 0.5;
            float2 centeredCoord = coord - halfSize;
            float radius = radiusAt(centeredCoord, cornerRadii);

            // Caustic light banding: directional light rays sweeping across glass slab
            float bandCoord = (centeredCoord.x * 0.707 + centeredCoord.y * 0.707) / 70.0;
            float causticBand = sin(bandCoord * 3.14159) * 0.5 + 0.5;
            causticBand = pow(causticBand, 3.5) * 0.22;

            // Top specular Fresnel glare horizon
            float topGlare = smoothstep(halfSize.y - 14.0, halfSize.y, -centeredCoord.y) * 0.35;

            float sd = sdRoundedRect(centeredCoord, halfSize, radius);

            if (-sd >= refractionHeight) {
                half4 baseColor = content.eval(coord);
                baseColor = saturateColor(baseColor, chromaMultiplier);
                float3 target = (whitePoint > 0.0) ? float3(1.0) : float3(0.0);
                baseColor.rgb = mix(baseColor.rgb, target, abs(whitePoint));
                baseColor.rgb = (baseColor.rgb - 0.5) * (1.0 + contrast) + 0.5;
                baseColor.rgb += float3(causticBand + topGlare);

                half3 tintedRGB = mix(baseColor.rgb, tintColor, tintAlpha);
                return half4(tintedRGB, baseColor.a);
            }

            sd = min(sd, 0.0);
            float d = circleMap(1.0 - -sd / max(1.0, refractionHeight)) * refractionAmount;
            float smoothRadius = max(radius * 1.5, 30.0);
            float gradRadius = min(smoothRadius, min(halfSize.x, halfSize.y));

            float2 shapeGrad = gradSdRoundedRect(centeredCoord, halfSize, gradRadius);
            float2 depthGrad = safeNormalize(centeredCoord, shapeGrad);
            float2 grad = safeNormalize(shapeGrad + depthEffect * depthGrad, shapeGrad);

            float2 refractedCoord = coord + d * grad;
            float dispersionIntensity = chromaticAberration * ((centeredCoord.x * centeredCoord.y) / (halfSize.x * halfSize.y));
            float2 dispersedCoord = d * grad * dispersionIntensity;

            half4 color = half4(0.0);

            half4 red = content.eval(refractedCoord + dispersedCoord);
            color.r += red.r / 3.5;
            color.a += red.a / 7.0;

            half4 orange = content.eval(refractedCoord + dispersedCoord * (2.0 / 3.0));
            color.r += orange.r / 3.5;
            color.g += orange.g / 7.0;
            color.a += orange.a / 7.0;

            half4 yellow = content.eval(refractedCoord + dispersedCoord * (1.0 / 3.0));
            color.r += yellow.r / 3.5;
            color.g += yellow.g / 3.5;
            color.a += yellow.a / 7.0;

            half4 green = content.eval(refractedCoord);
            color.g += green.g / 3.5;
            color.a += green.a / 7.0;

            half4 cyan = content.eval(refractedCoord - dispersedCoord * (1.0 / 3.0));
            color.g += cyan.g / 3.5;
            color.b += cyan.b / 3.0;
            color.a += cyan.a / 7.0;

            half4 blue = content.eval(refractedCoord - dispersedCoord * (2.0 / 3.0));
            color.b += blue.b / 3.0;
            color.a += blue.a / 7.0;

            half4 purple = content.eval(refractedCoord - dispersedCoord);
            color.r += purple.r / 7.0;
            color.b += purple.b / 3.0;
            color.a += purple.a / 7.0;

            color = saturateColor(color, chromaMultiplier);
            float3 target = (whitePoint > 0.0) ? float3(1.0) : float3(0.0);
            color.rgb = mix(color.rgb, target, abs(whitePoint));
            color.rgb = (color.rgb - 0.5) * (1.0 + contrast) + 0.5;
            color.rgb += float3(causticBand + topGlare);

            half3 tintedRGB = mix(color.rgb, tintColor, tintAlpha);
            return half4(tintedRGB, color.a);
        }
    """
}

/**
 * Applies physical liquid glass refraction, chromatic dispersion, and caustic light banding.
 * On Android 13+ (API 33+, Android 17), runs the full GPU AGSL shader.
 */
fun Modifier.liquidGlassRefraction(
    cornerRadiusPx: Float = 84f,
    refractionHeight: Float = 48f,
    refractionAmount: Float = 32f,
    chromaticAberration: Float = 0.75f,
    depthEffect: Float = 0.30f,
    blurRadius: Float = 14f
): Modifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    this.graphicsLayer {
        try {
            if (size.width > 0f && size.height > 0f) {
                val shader = RuntimeShader(LiquidGlassShader.AGSL_SRC)
                shader.setFloatUniform("size", size.width, size.height)
                shader.setFloatUniform("cornerRadii", cornerRadiusPx, cornerRadiusPx, cornerRadiusPx, cornerRadiusPx)
                shader.setFloatUniform("refractionHeight", refractionHeight)
                shader.setFloatUniform("refractionAmount", refractionAmount)
                shader.setFloatUniform("depthEffect", depthEffect)
                shader.setFloatUniform("chromaticAberration", chromaticAberration)
                shader.setFloatUniform("contrast", 0.08f)
                shader.setFloatUniform("whitePoint", 0.08f)
                shader.setFloatUniform("chromaMultiplier", 1.30f)
                shader.setFloatUniform("tintColor", 1.0f, 1.0f, 1.0f)
                shader.setFloatUniform("tintAlpha", 0.14f)

                val shaderEffect = RenderEffect.createRuntimeShaderEffect(shader, "content")
                val finalEffect = if (blurRadius > 0.5f) {
                    val blurEffect = RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP)
                    RenderEffect.createChainEffect(shaderEffect, blurEffect)
                } else {
                    shaderEffect
                }
                renderEffect = finalEffect.asComposeRenderEffect()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
} else {
    this
}
