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

public class CschFunc extends SimpleVariationFunc {

  @Override
  public void transform(XFormTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    /* complex vars by cothe */
    /* exp log sin cos tan sec csc cot sinh cosh tanh sech csch coth */
    //Hyperbolic Cosecant CSCH
    double cschsin = pContext.sin(pAffineTP.y);
    double cschcos = pContext.cos(pAffineTP.y);
    double cschsinh = Math.sinh(pAffineTP.x);
    double cschcosh = Math.cosh(pAffineTP.x);
    double cschden = 2.0 / (Math.cosh(2.0 * pAffineTP.x) - pContext.cos(2.0 * pAffineTP.y));
    pVarTP.x += pAmount * cschden * cschsinh * cschcos;
    pVarTP.y -= pAmount * cschden * cschcosh * cschsin;
  }

  @Override
  public String getName() {
    return "csch";
  }

}