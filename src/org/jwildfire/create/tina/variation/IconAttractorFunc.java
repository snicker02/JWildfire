package org.jwildfire.create.tina.variation;


/**
 * Symetric Icon Attractors
 *
 * @author Jesus Sosa
 * @date January 11 , 2018
 * Attractors came from the book  Symmetry in Chaos by Michael Field and Martin Golubitsky
 * Reference:
 * https://softologyblog.wordpress.com/2017/03/04/2d-strange-attractors/
 */

import org.jwildfire.base.Tools;
import org.jwildfire.create.tina.base.Layer;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

import static org.jwildfire.base.mathlib.MathLib.*;

public class IconAttractorFunc extends VariationFunc implements SupportsGPU {

  private static final long serialVersionUID = 1L;
  private static final String PARAM_PRESETID = "presetId";
  private static final String PARAM_DEGREE = "degree";
  private static final String PARAM_A = "a";
  private static final String PARAM_B = "b";
  private static final String PARAM_G = "g";
  private static final String PARAM_O = "o";
  private static final String PARAM_L = "l";


  private static final String PARAM_CENTERX = "centerx";
  private static final String PARAM_CENTERY = "centery";
  private static final String PARAM_SCALE = "scale";

  private static final String RESSOURCE_ID_REFERENCE = "presetId_reference";
  
  private static final String[] paramNames = {PARAM_PRESETID, PARAM_DEGREE, PARAM_A, PARAM_B, PARAM_G, PARAM_O, PARAM_L, PARAM_CENTERX, PARAM_CENTERY, PARAM_SCALE};
  private static final String[] ressourceNames = {RESSOURCE_ID_REFERENCE};

  int[] pdeg = {5, 3, 5, 3, 3, 9, 6, 23, 7, 5, 5, 5, 4, 3, 3, 3, 16};
  double[] pa = {5.0, -1.0, 1.806, 10.0, -2.5, 3.0, 5.0, -2.5, 1.0, 2.32, -2.0, 2.0, 2.0, -1.0, -1.0, -1.0, -2.5};
  double[] pb = {-1.9, 0.1, 0.0, -12.0, 0.0, -16.79, 1.5, 0.0, -0.1, 0.0, 0.0, 0.2, 0.0, 0.1, 0.1, 0.03, -0.1};
  double[] pg = {1.0, -0.82, 1.0, 1.0, 0.9, 1.0, 1.0, 0.9, 0.167, 0.75, -0.5, 0.1, 1.0, -0.82, -0.805, -0.80, 0.90};
  double[] po = {0.188, 0.12, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1, 0.0, 0.0, 0.0, -0.15};
  double[] pl = {-2.5, 1.56, -1.806, -2.195, 2.5, -2.05, -2.7, 2.409, -2.08, -2.32, 2.6, -2.34, -1.86, 1.56, 1.5, 1.455, 2.39};

  int presetId = (int) (pdeg.length * Math.random());


  int degree = pdeg[presetId];
  double a = pa[presetId];
  double b = pb[presetId];
  double g = pg[presetId];
  double o = po[presetId];
  double l = pl[presetId];


  private double centerx = 0.0;
  private double centery = 0.0;
  private double scale = 5.0;
  private double bdcs;

  private String id_reference = "org.jwildfire.create.tina.variation.reference.ReferenceFile iconattractor-presets.pdf";
  
  public void init(FlameTransformationContext pContext, Layer pLayer, XForm pXForm, double pAmount) {
    bdcs = 1.0 / (scale == 0.0 ? 10E-6 : scale);
  }

  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
// Reference:
//	https://softologyblog.wordpress.com/2017/03/04/2d-strange-attractors/
    double x, y;


    double zzbar = sqr(pAffineTP.x) + sqr(pAffineTP.y);
    double p = a * zzbar + l;

    double zreal = pAffineTP.x;
    double zimag = pAffineTP.y;

    for (int i = 1; i <= degree - 2; i++) {
      double za = zreal * pAffineTP.x - zimag * pAffineTP.y;
      double zb = zimag * pAffineTP.x + zreal * pAffineTP.y;
      zreal = za;
      zimag = zb;
    }
    double zn = pAffineTP.x * zreal - pAffineTP.y * zimag;
    p = p + b * zn;

    x = p * pAffineTP.x + g * zreal - o * pAffineTP.y;
    y = p * pAffineTP.y - g * zimag + o * pAffineTP.x;


    pVarTP.x = x * pAmount;
    pVarTP.y = y * pAmount;

    pVarTP.color = fmod(fabs(bdcs * (sqr(pVarTP.x + centerx) + sqr(pVarTP.y + centery))), 1.0);

  }

  public String getName() {
    return "iconattractor_js";
  }

  public String[] getParameterNames() {
    return paramNames;
  }

  public Object[] getParameterValues() {
    return new Object[]{presetId, degree, a, b, g, o, l, centerx, centery, scale};
  }

  public void setParameter(String pName, double pValue) {
    if (pName.equalsIgnoreCase(PARAM_PRESETID)) {
      presetId = Tools.limitValue((int) pValue, 0, pdeg.length - 1);
      degree = pdeg[presetId];
      a = pa[presetId];
      b = pb[presetId];
      g = pg[presetId];
      o = po[presetId];
      l = pl[presetId];
    } else if (pName.equalsIgnoreCase(PARAM_DEGREE)) {
      degree = Tools.limitValue((int) pValue, 3, 50);
    } else if (pName.equalsIgnoreCase(PARAM_A)) {
//		      a = Tools.limitValue(pValue, -3.0, 3.0);
      a = pValue;
    } else if (pName.equalsIgnoreCase(PARAM_B)) {
//	      b = Tools.limitValue(pValue, -3.0, 3.0);
      b = pValue;
    } else if (pName.equalsIgnoreCase(PARAM_G)) {
//		      g = Tools.limitValue(pValue, -3.0, 3.0);
      g = pValue;
    } else if (pName.equalsIgnoreCase(PARAM_O)) {
//		      o = Tools.limitValue(pValue, -3.0, 3.0);
      o = pValue;
    } else if (pName.equalsIgnoreCase(PARAM_L)) {
//		      l = Tools.limitValue(pValue, -3.0, 3.0);
      l = pValue;
    } else if (PARAM_CENTERX.equalsIgnoreCase(pName))
      centerx = pValue;
    else if (PARAM_CENTERY.equalsIgnoreCase(pName))
      centery = pValue;
    else if (PARAM_SCALE.equalsIgnoreCase(pName))
      scale = pValue;
    else
      throw new IllegalArgumentException(pName);
  }

  @Override
  public String[] getRessourceNames() {
    return ressourceNames;
  }

  @Override
  public byte[][] getRessourceValues() {
    return new byte[][] {id_reference.getBytes()};
  }

  @Override
  public RessourceType getRessourceType(String pName) {
    if (RESSOURCE_ID_REFERENCE.equalsIgnoreCase(pName)) {
      return RessourceType.REFERENCE;
    }
    else throw new IllegalArgumentException(pName);
  }

  // Changing presetId will modify other params
	@Override
	public boolean dynamicParameterExpansion() {
		return true;
	}

	@Override
	public boolean dynamicParameterExpansion(String pName) {
		return true;
	}	
	
	@Override
	public void randomize() {
	  presetId = (int) (pdeg.length * Math.random());
	  degree = pdeg[presetId];
	  a = pa[presetId] + Math.random() * 0.2 - 0.1;
	  b = pb[presetId] + Math.random() * 0.2 - 0.1;
	  g = pg[presetId] + Math.random() * 0.2 - 0.1;
	  o = po[presetId] + Math.random() * 0.2 - 0.1;
	  l = pl[presetId] + Math.random() * 0.2 - 0.1;
		centerx = Math.random() * 2.0 - 1.0;
		centery = Math.random() * 2.0 - 1.0;
		scale = Math.random() * 10.0;
	}

	@Override
  public VariationFuncType[] getVariationTypes() {
    return new VariationFuncType[]{VariationFuncType.VARTYPE_2D, VariationFuncType.VARTYPE_SIMULATION, VariationFuncType.VARTYPE_DC, VariationFuncType.VARTYPE_BASE_SHAPE, VariationFuncType.VARTYPE_SUPPORTS_GPU, VariationFuncType.VARTYPE_SUPPORTS_BACKGROUND};
  }
	
  @Override
  public String getGPUCode(FlameTransformationContext context) {
    return   "    float bdcs = 1.0 / ( __iconattractor_js_scale  == 0.0 ? 10.0e-6 :  __iconattractor_js_scale );"
    		+"    float x, y;"
    		+"    float zzbar = __x*__x + __y*__y;"
    		+"    float p = __iconattractor_js_a * zzbar + __iconattractor_js_l;"
    		+"    float zreal = __x;"
    		+"    float zimag = __y;"
    		+"    for (int i = 1; i <=  __iconattractor_js_degree  - 2; i++) {"
    		+"      float za = zreal * __x - zimag * __y;"
    		+"      float zb = zimag * __x + zreal * __y;"
    		+"      zreal = za;"
    		+"      zimag = zb;"
    		+"    }"
    		+"    float zn = __x * zreal - __y * zimag;"
    		+"    p = p + __iconattractor_js_b * zn;"
    		+"    x = p * __x + __iconattractor_js_g * zreal - __iconattractor_js_o * __y;"
    		+"    y = p * __y - __iconattractor_js_g * zimag + __iconattractor_js_o * __x;"
    		+"    __px = x * __iconattractor_js;"
    		+"    __py = y * __iconattractor_js;"
    		+"    __pal = fmodf(fabsf(bdcs * (sqrf(__px +  __iconattractor_js_centerx ) + sqrf(__py +  __iconattractor_js_centery ))), 1.0);";
  }
}



