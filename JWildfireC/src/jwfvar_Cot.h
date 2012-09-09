/*
 JWildfireC - an external C-based fractal-flame-renderer for JWildfire
 Copyright (C) 2012 Andreas Maschke

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

#ifndef JWFVAR_COT_H_
#define JWFVAR_COT_H_

#include "jwf_Variation.h"

class CotFunc: public Variation {
public:
	CotFunc() {
	}

	const char* getName() const {
		return "cot";
	}

	void transform(FlameTransformationContext *pContext, XYZPoint *pAffineTP, XYZPoint *pVarTP, float pAmount) {
    float cotsin = sinf(2.0f * pAffineTP->x);
    float cotcos = cosf(2.0f * pAffineTP->x);
    float cotsinh = sinhf(2.0f * pAffineTP->y);
    float cotcosh = coshf(2.0f * pAffineTP->y);
    float cotden = 1.0f / (cotcosh - cotcos);
    pVarTP->x += pAmount * cotden * cotsin;
    pVarTP->y += pAmount * cotden * -1 * cotsinh;
    if (pContext->isPreserveZCoordinate) {
      pVarTP->z += pAmount * pAffineTP->z;
    }
	}

	CotFunc* makeCopy() {
		return new CotFunc(*this);
	}

};

#endif // JWFVAR_COT_H_