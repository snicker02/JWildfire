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

// Variation Plugin DLL for Apophysis
//  Jed Kelsey, 20 June 2007
package org.jwildfire.create.tina.variation;

import org.jwildfire.base.Tools;
import org.jwildfire.create.tina.base.Layer;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;
import org.jwildfire.create.tina.random.AbstractRandomGenerator;
import org.jwildfire.create.tina.random.MarsagliaRandomGenerator;

import java.util.ArrayList;
import java.util.List;

public class MandelbrotFunc extends VariationFunc implements SupportsGPU {
  private static final long serialVersionUID = 1L;

  private static final String PARAM_ITER = "iter";
  private static final String PARAM_XMIN = "xmin";
  private static final String PARAM_XMAX = "xmax";
  private static final String PARAM_YMIN = "ymin";
  private static final String PARAM_YMAX = "ymax";
  private static final String PARAM_INVERT = "invert";
  private static final String PARAM_SKIN = "skin";
  private static final String PARAM_CX = "cx";
  private static final String PARAM_CY = "cy";
  private static final String PARAM_MAX_POINTS = "max_points";
  private static final String PARAM_SEED = "seed";
  private static final String PARAM_RND_Z_RANGE = "rnd_z_range";

  private static final String[] paramNames = {PARAM_ITER, PARAM_XMIN, PARAM_XMAX, PARAM_YMIN, PARAM_YMAX, PARAM_INVERT, PARAM_SKIN, PARAM_CX, PARAM_CY, PARAM_MAX_POINTS, PARAM_SEED, PARAM_RND_Z_RANGE};

  private int iter = 100;
  private double xmin = -1.6;
  private double xmax = 1.6;
  private double ymin = -1.2;
  private double ymax = 1.2;
  private int invert = 0;
  private double skin = 0.012;
  private double cx = 0.0;
  private double cy = 0.0;
  private int max_points = -1;
  private int seed = 1234;
  private double rnd_z_range = 0.0;

  private AbstractRandomGenerator randGen;

  private int _pIdx = 0;
  private List<Double> xP = new ArrayList<Double>();
  private List<Double> yP = new ArrayList<Double>();
  private List<Double> zP = new ArrayList<Double>();

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    double x1 = _x0;
    double x = _x0;
    double y1 = _y0;
    double y = _y0;
    int currIter;

    boolean inverted = randGen.random() < this.invert;
    if (inverted) {
      currIter = 0;
    } else {
      currIter = iter;
    }
    int k=0;
    while ((k++<10) && ((inverted && (currIter < iter)) ||
            ((!inverted) && ((currIter >= iter) ||
                    ((skin < 1) && (currIter < 0.1 * iter * (1 - skin))))))) {
      if ((_x0 == 0) && (_y0 == 0)) {
        // Choose a point at random
        if (max_points > 0) {
          if (xP.size() >= max_points) {
            _x0 = xP.get(_pIdx);
            _y0 = yP.get(_pIdx);
            _z0 = zP.get(_pIdx++);
            if (_pIdx >= max_points) {
              _pIdx = 0;
            }
          } else {
            _x0 = (xmax - xmin) * randGen.random() + xmin;
            _y0 = (ymax - ymin) * randGen.random() + ymin;
            _z0 = randGen.random() * rnd_z_range;
            xP.add(_x0);
            yP.add(_y0);
            zP.add(_z0);
          }
        } else {
          _x0 = (xmax - xmin) * randGen.random() + xmin;
          _y0 = (ymax - ymin) * randGen.random() + ymin;
          _z0 = randGen.random() * rnd_z_range;
        }
      } else {
        // Choose a point close to previous point
        _x0 = (skin + 0.001) * (randGen.random() - 0.5) + _x0;
        _y0 = (skin + 0.001) * (randGen.random() - 0.5) + _y0;
        //        _z0 = (skin + 0.001) * (randGen.random() - 0.5) + _z0;
      }
      x1 = _x0;
      y1 = _y0;
      x = _x0;
      y = _y0;
      currIter = 0;
      while (((x * x + y * y < 2 * 2) && (currIter < iter))) {
        double xtemp = x * x - y * y + _x0;
        y = 2.0 * x * y + _y0;
        x = xtemp;
        currIter++;
      }
      if ((currIter >= iter) || (skin == 1) || (currIter < 0.1 * (iter * (1 - skin)))) {
        // Random point next time
        _x0 = 0;
        _y0 = 0;
      }
    }
    if(k>=10) {x1=y1=50000.0-pContext.random()*100000;};

    pVarTP.x += pAmount * (x1 + cx * x); // + FTx^;
    pVarTP.y += pAmount * (y1 + cy * y); // + FTy^;
    pVarTP.z += _z0;
  }

  @Override
  public String[] getParameterNames() {
    return paramNames;
  }

  @Override
  public Object[] getParameterValues() {
    return new Object[]{iter, xmin, xmax, ymin, ymax, invert, skin, cx, cy, max_points, seed, rnd_z_range};
  }

  @Override
  public void setParameter(String pName, double pValue) {
    if (PARAM_ITER.equalsIgnoreCase(pName))
      iter = Tools.FTOI(pValue);
    else if (PARAM_XMIN.equalsIgnoreCase(pName))
      xmin = pValue;
    else if (PARAM_XMAX.equalsIgnoreCase(pName))
      xmax = pValue;
    else if (PARAM_YMIN.equalsIgnoreCase(pName))
      ymin = pValue;
    else if (PARAM_YMAX.equalsIgnoreCase(pName))
      ymax = pValue;
    else if (PARAM_INVERT.equalsIgnoreCase(pName))
      invert = Tools.FTOI(pValue);
    else if (PARAM_SKIN.equalsIgnoreCase(pName))
      skin = pValue;
    else if (PARAM_CX.equalsIgnoreCase(pName))
      cx = pValue;
    else if (PARAM_CY.equalsIgnoreCase(pName))
      cy = pValue;
    else if (PARAM_MAX_POINTS.equalsIgnoreCase(pName)) {
      max_points = Tools.FTOI(pValue);
      if (max_points > 0 && max_points < 100) {
        max_points = 100;
      }
    } else if (PARAM_SEED.equalsIgnoreCase(pName))
      seed = Tools.FTOI(pValue);
    else if (PARAM_RND_Z_RANGE.equalsIgnoreCase(pName))
      rnd_z_range = pValue;
    else
      throw new IllegalArgumentException(pName);
  }
  
  @Override
  public void randomize() {
  	iter = (int) (Math.random() * 950 + 50);
  	xmin = Math.random() * 2.0 - 2.0;
  	xmax = Math.random() * (1.0 - xmin) + xmin;
  	ymin = Math.random() * 1.5 - 1.5;
  	ymax = Math.random() * (1.5 - ymin) + ymin;
  	invert = (int) (Math.random() * 2);
  	skin = Math.random() * 0.99 + 0.01;
    cx = (Math.random() < 0.75) ? 0.0 : Math.random() * 3.0 - 1.5;
    cy = (Math.random() < 0.75) ? 0.0 : Math.random() * 3.0 - 1.5;
    max_points = (Math.random() < 0.85) ? -1 : (int) (Math.random() * iter * (iter - 50) + 50*iter);
    seed = (int) (Math.random() * 1000000);
    rnd_z_range = Math.random() * 2.0 - 1.0;
  }

  @Override
  public String getName() {
    return "mandelbrot";
  }

  private double _x0, _y0, _z0;

  @Override
  public void init(FlameTransformationContext pContext, Layer pLayer, XForm pXForm, double pAmount) {
    _x0 = _y0 = _z0 = 0.0;
    xP.clear();
    yP.clear();
    zP.clear();
    _pIdx = 0;
    randGen = new MarsagliaRandomGenerator();
    randGen.randomize(seed);
  }

  @Override
  public VariationFuncType[] getVariationTypes() {
    return new VariationFuncType[]{VariationFuncType.VARTYPE_3D, VariationFuncType.VARTYPE_SIMULATION, VariationFuncType.VARTYPE_BASE_SHAPE, VariationFuncType.VARTYPE_ESCAPE_TIME_FRACTAL, VariationFuncType.VARTYPE_SUPPORTS_GPU};
  }

  @Override
  public String getGPUCode(FlameTransformationContext context) {
    return
         "float _x0 = varpar->mandelbrot_x0;\n"
         +"float _y0 = varpar->mandelbrot_y0;\n"
         +"float _z0 = varpar->mandelbrot_z0;\n"
        + "   float x1 = _x0;\n"
        + "    float x = _x0;\n"
        + "    float y1 = _y0;\n"
        + "    float y = _y0;\n"
        + "    int currIter;\n"
        + "\n"
        + "    short inverted = RANDFLOAT() < __mandelbrot_invert;\n"
        + "    if (inverted) {\n"
        + "      currIter = 0;\n"
        + "    } else {\n"
        + "      currIter = __mandelbrot_iter;\n"
        + "    }\n"
        + "    int k=0;\n"
        + "    while ((k++<10) && ((inverted && (currIter < __mandelbrot_iter)) ||\n"
        + "            ((!inverted) && ((currIter >= __mandelbrot_iter) ||\n"
        + "                    ((__mandelbrot_skin < 1) && (currIter < 0.1 * __mandelbrot_iter * (1 - __mandelbrot_skin))))))) {\n"
        + "      if ((_x0 == 0) && (_y0 == 0)) {\n"
        + "          _x0 = (__mandelbrot_xmax - __mandelbrot_xmin) * RANDFLOAT() + __mandelbrot_xmin;\n"
        + "          _y0 = (__mandelbrot_ymax - __mandelbrot_ymin) * RANDFLOAT() + __mandelbrot_ymin;\n"
        + "          _z0 = RANDFLOAT() * __mandelbrot_rnd_z_range;\n"
        + "      } else {\n"
        + "        _x0 = (__mandelbrot_skin + 0.001) * (RANDFLOAT() - 0.5) + _x0;\n"
        + "        _y0 = (__mandelbrot_skin + 0.001) * (RANDFLOAT() - 0.5) + _y0;\n"
        + "      }\n"
        + "      x1 = _x0;\n"
        + "      y1 = _y0;\n"
        + "      x = _x0;\n"
        + "      y = _y0;\n"
        + "      currIter = 0;\n"
        + "      while (((x * x + y * y < 2 * 2) && (currIter < __mandelbrot_iter))) {\n"
        + "        float xtemp = x * x - y * y + _x0;\n"
        + "        y = 2.0 * x * y + _y0;\n"
        + "        x = xtemp;\n"
        + "        currIter++;\n"
        + "      }\n"
        + "      if ((currIter >= __mandelbrot_iter) || (__mandelbrot_skin == 1) || (currIter < 0.1 * (__mandelbrot_iter * (1 - __mandelbrot_skin)))) {\n"
        + "        _x0 = 0;\n"
        + "        _y0 = 0;\n"
        + "      }\n"
        + "    }\n"
        + "    if(k>=10) {x1=y1=50000.0-RANDFLOAT()*100000;};\n"
        + "    __px += __mandelbrot * (x1 + __mandelbrot_cx * x);\n"
        + "    __py += __mandelbrot * (y1 + __mandelbrot_cy * y);\n"
        + "    __pz += _z0;\n"
        + "\n"
        + "*(&(varpar->mandelbrot_x0))=_x0;\n"
        + "*(&(varpar->mandelbrot_y0))=_y0;\n"
        + "*(&(varpar->mandelbrot_z0))=_z0;\n";

  }

  @Override
  public String[] getGPUExtraParameterNames() {
    return new String[]{"x0", "y0", "z0"};
  }

}
