/*
  JWildfire - an image and animation processor written in Java 
  Copyright (C) 1995-2021 Andreas Maschke

  This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser 
  General Public License as published by the Free Software Foundation; either version 2.1 of the 
  License, or (at your option) any later version.
 
  This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this software; 
  if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jwildfire.create.tina.variation;

import org.jwildfire.create.tina.base.Layer;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

import static org.jwildfire.base.mathlib.MathLib.*;

public class CurlSpFunc extends VariationFunc implements SupportsGPU {
  private static final long serialVersionUID = 1L;

  private static final String PARAM_POW = "pow";
  private static final String PARAM_C1 = "c1";
  private static final String PARAM_C2 = "c2";
  private static final String PARAM_SX = "sx";
  private static final String PARAM_SY = "sy";
  private static final String PARAM_DC = "dc";
  private static final String[] paramNames = {PARAM_POW, PARAM_C1, PARAM_C2, PARAM_SX, PARAM_SY, PARAM_DC};

  private double pow = 1.0;
  private double c1 = -0.01;
  private double c2 = 0.03;
  private double sx = 0.0;
  private double sy = 0.0;
  private double dc = 0.0;

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    // curl_sp by Xyrus02, http://apophysis-7x.org/extensions
    double x = powq4c(pAffineTP.x, power);
    double y = powq4c(pAffineTP.y, power);
    double z = powq4c(pAffineTP.z, power);

    double d = sqr(x) - sqr(y);

    double re = spread(c1 * x + c2 * d, sx) + 1.0;
    double im = spread(c1 * y + c2_x2 * x * y, sy);

    double c = powq4c(sqr(re) + sqr(im), power_inv);
    double r = pAmount / c;

    pVarTP.x += (x * re + y * im) * r;
    pVarTP.y += (y * re - x * im) * r;
    pVarTP.z += (z * pAmount) / c;

    pVarTP.color = range(0, pVarTP.color + dc_adjust * c, 1);
  }

  @Override
  public String[] getParameterNames() {
    return paramNames;
  }

  @Override
  public Object[] getParameterValues() {
    return new Object[]{pow, c1, c2, sx, sy, dc};
  }

  @Override
  public void setParameter(String pName, double pValue) {
    if (PARAM_POW.equalsIgnoreCase(pName))
      pow = pValue;
    else if (PARAM_C1.equalsIgnoreCase(pName))
      c1 = pValue;
    else if (PARAM_C2.equalsIgnoreCase(pName))
      c2 = pValue;
    else if (PARAM_SX.equalsIgnoreCase(pName))
      sx = pValue;
    else if (PARAM_SY.equalsIgnoreCase(pName))
      sy = pValue;
    else if (PARAM_DC.equalsIgnoreCase(pName))
      dc = pValue;
    else
      throw new IllegalArgumentException(pName);
  }

  @Override
  public String getName() {
    return "curl_sp";
  }

  private double c2_x2, dc_adjust, power_inv, power;

  @Override
  public void init(FlameTransformationContext pContext, Layer pLayer, XForm pXForm, double pAmount) {
    power = pow;
    c2_x2 = 2.0 * c2;
    dc_adjust = 0.1 * dc;
    power_inv = 1.0 / zeps(power);

    if (power == 0) {
      power = SMALL_EPSILON;
    }
  }

  private double spread(double a, double b) {
    return (sqrt(sqr(a) + sqr(b)) * ((a) > 0 ? 1 : -1));
  }

  private double range(double a, double x, double b) {
    return ((x) < (a) ? (a) : (x) > (b) ? (b) : (x));
  }

  private double powq4(double x, double y) {
    return (pow(fabs(x), y) * sign(x));
  }

  private double powq4c(double x, double y) {
    return ((y) == 1 ? (x) : powq4(x, y));
  }

  private double zeps(double x) {
    return ((x) == 0 ? SMALL_EPSILON : (x));
  }
  
  @Override
  public void randomize() {
    pow = Math.random() * 3.75 + 0.25;
    c1 = Math.random() * 5.0 - 2.5;
    c2 = Math.random() * 2.0 - 1.0;
    sx = Math.random();
    sy = Math.random();
    dc = Math.random() * 4.0 - 2.0;
  }

  @Override
  public VariationFuncType[] getVariationTypes() {
    return new VariationFuncType[]{VariationFuncType.VARTYPE_3D, VariationFuncType.VARTYPE_DC, VariationFuncType.VARTYPE_SUPPORTS_GPU};
  }

  @Override
  public String getGPUCode(FlameTransformationContext context) {
    return "float c2_x2, dc_adjust, power_inv, power;\n"
        + "   power = __curl_sp_pow;\n"
        + "    c2_x2 = 2.0 * __curl_sp_c2;\n"
        + "    dc_adjust = 0.1 * __curl_sp_dc;\n"
        + "    power_inv = 1.0 / (power==0 ? 1.e-6f : power);\n"
        + "    if (power == 0) {\n"
        + "      power = 1.e-6f;\n"
        + "    }\n"
        + "    float x = curl_sp_powq4c(__x, power);\n"
        + "    float y = curl_sp_powq4c(__y, power);\n"
        + "    float z = curl_sp_powq4c(__z, power);\n"
        + "\n"
        + "    float d = x*x - y*y;\n"
        + "\n"
        + "    float re = curl_sp_spread(__curl_sp_c1 * x + __curl_sp_c2 * d, __curl_sp_sx) + 1.0;\n"
        + "    float im = curl_sp_spread(__curl_sp_c1 * y + c2_x2 * x * y, __curl_sp_sy);\n"
        + "\n"
        + "    float c = curl_sp_powq4c(re*re + im*im, power_inv);\n"
        + "    float r = __curl_sp / c;\n"
        + "\n"
        + "    __px += (x * re + y * im) * r;\n"
        + "    __py += (y * re - x * im) * r;\n"
        + "    __pz += (z * __curl_sp) / c;\n"
        + "\n"
        + "    __pal = __pal + dc_adjust * c;\n"
        + "    if(__pal<0) __pal=0;\n"
        + "    else if(__pal > 1.f) __pal = 1.f;";
  }

  @Override
  public String getGPUFunctions(FlameTransformationContext context) {
    return "__device__ float curl_sp_spread(float a, float b) {\n"
        + "    return (sqrtf(a*a + b*b) * ((a) > 0 ? 1 : -1));\n"
        + "  }\n"
        + "__device__ float curl_sp_powq4(float x, float y) {\n"
        + "    return (powf(fabsf(x), y) * (x<0 ? -1 : x > 0 ? 1: 0));\n"
        + "  }\n"
        + "\n"
        + "__device__ float curl_sp_powq4c(float x, float y) {\n"
        + "    return ((y) == 1 ? (x) : curl_sp_powq4(x, y));\n"
        + "  }\n\n";
  }
}
