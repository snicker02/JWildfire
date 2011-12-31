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

import org.jwildfire.create.tina.base.Constants;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

public class WedgeSphFunc extends VariationFunc {

  private static final String PARAM_ANGLE = "angle";
  private static final String PARAM_HOLE = "hole";
  private static final String PARAM_COUNT = "count";
  private static final String PARAM_SWIRL = "swirl";

  private static final String[] paramNames = { PARAM_ANGLE, PARAM_HOLE, PARAM_COUNT, PARAM_SWIRL };

  private double angle = 0.20;
  private double hole = 0.20;
  private double count = 2.0;
  private double swirl = 0.30;

  @Override
  public void transform(XFormTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    /* Wedge_sph from apo plugins pack */

    double r = 1.0 / (pAffineTP.getPrecalcSqrt(pContext) + Constants.EPSILON);
    double a = pAffineTP.getPrecalcAtanYX(pContext) + swirl * r;
    double c = Math.floor((count * a + Math.PI) * Constants.M_1_PI * 0.5);

    double comp_fac = 1 - angle * count * Constants.M_1_PI * 0.5;

    a = a * comp_fac + c * angle;

    double sa = pContext.sin(a);
    double ca = pContext.cos(a);
    r = pAmount * (r + hole);

    pVarTP.x += r * ca;
    pVarTP.y += r * sa;
  }

  @Override
  public String[] getParameterNames() {
    return paramNames;
  }

  @Override
  public Object[] getParameterValues() {
    return new Object[] { angle, hole, count, swirl };
  }

  @Override
  public void setParameter(String pName, double pValue) {
    if (PARAM_ANGLE.equalsIgnoreCase(pName))
      angle = pValue;
    else if (PARAM_HOLE.equalsIgnoreCase(pName))
      hole = pValue;
    else if (PARAM_COUNT.equalsIgnoreCase(pName))
      count = pValue;
    else if (PARAM_SWIRL.equalsIgnoreCase(pName))
      swirl = pValue;
    else
      throw new IllegalArgumentException(pName);
  }

  @Override
  public String getName() {
    return "wedge_sph";
  }

}