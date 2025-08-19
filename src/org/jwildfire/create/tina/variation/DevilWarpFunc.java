/*
  JWildfire - an image and animation processor written in Java 
  Copyright (C) 1995-2011 Andreas Maschke

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

import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

import static org.jwildfire.base.mathlib.MathLib.pow;

public class DevilWarpFunc extends VariationFunc implements SupportsGPU {
  private static final long serialVersionUID = 1L;

  private static final String PARAM_A = "a";
  private static final String PARAM_B = "b";
  private static final String PARAM_EFFECT = "effect";
  private static final String PARAM_WARP = "warp";
  private static final String PARAM_RMIN = "rmin";
  private static final String PARAM_RMAX = "rmax";
  private static final String[] paramNames = {PARAM_A, PARAM_B, PARAM_EFFECT, PARAM_WARP, PARAM_RMIN, PARAM_RMAX};
  private double a = 2.0;
  private double b = 1.0;
  private double effect = 1.0;
  private double warp = 0.5;
  private double rmin = -0.24;
  private double rmax = 100;

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    /* devil_warp by dark-beam */
    double xx = pAffineTP.x;
    double yy = pAffineTP.y;
    double r2 = 1.0 / (xx * xx + yy * yy);
    double r = pow(xx * xx + r2 * b * yy * yy, warp) - pow(yy * yy + r2 * a * xx * xx, warp);
    if (r > rmax)
      r = rmax;
    else if (r < rmin)
      r = rmin;
    r = effect * (r);
    pVarTP.x += xx * (1 + r);
    pVarTP.y += yy * (1 + r);
    if (pContext.isPreserveZCoordinate()) {
      pVarTP.z += pAmount * pAffineTP.z;
    }
  }

  @Override
  public String[] getParameterNames() {
    return paramNames;
  }

  @Override
  public Object[] getParameterValues() {
    return new Object[]{a, b, effect, warp, rmin, rmax};
  }

  @Override
  public void setParameter(String pName, double pValue) {
    if (PARAM_A.equalsIgnoreCase(pName))
      a = pValue;
    else if (PARAM_B.equalsIgnoreCase(pName))
      b = pValue;
    else if (PARAM_EFFECT.equalsIgnoreCase(pName))
      effect = pValue;
    else if (PARAM_WARP.equalsIgnoreCase(pName))
      warp = pValue;
    else if (PARAM_RMIN.equalsIgnoreCase(pName))
      rmin = pValue;
    else if (PARAM_RMAX.equalsIgnoreCase(pName))
      rmax = pValue;
    else
      throw new IllegalArgumentException(pName);
  }
  
  @Override
  public void randomize() {
  	a = Math.random() * 3.0;
  	b = Math.random() * 3.0;
  	effect = Math.random() * 6.0 - 3.0;
  	warp = Math.random() * 6.0 - 3.0;
  	rmin = Math.random() * 7.0 - 5.0;
  	rmax = rmin + ((Math.random() < 0.8) ? Math.random() * 5.0 : Math.random() * 100.0);
  }

  @Override
  public String getName() {
    return "devil_warp";
  }

  @Override
  public VariationFuncType[] getVariationTypes() {
    return new VariationFuncType[]{VariationFuncType.VARTYPE_2D,VariationFuncType.VARTYPE_SUPPORTS_GPU, VariationFuncType.VARTYPE_SUPPORTED_BY_SWAN};
  }
  @Override
  public String getGPUCode(FlameTransformationContext context) {
    return   "    float xx = __x;"
    		+"    float yy = __y;"
    		+"    float r2 = 1.0 / (xx * xx + yy * yy);"
    		+"    float r = powf(xx * xx + r2 *  __devil_warp_b  * yy * yy,  __devil_warp_warp ) - powf(yy * yy + r2 *  __devil_warp_a  * xx * xx,  __devil_warp_warp );"
    		+"    if (r >  __devil_warp_rmax )"
    		+"      r =  __devil_warp_rmax ;"
    		+"    else if (r <  __devil_warp_rmin )"
    		+"      r =  __devil_warp_rmin ;"
    		+"    r =  __devil_warp_effect  * (r);"
    		+"    __px += xx * (1 + r);"
    		+"    __py += yy * (1 + r);"
            + (context.isPreserveZCoordinate() ? "__pz += __devil_warp * __z;": "");
  }
}
