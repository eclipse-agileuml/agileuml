import java.util.*;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Collections;
import java.util.function.Function;
import java.io.Serializable;

class MatrixLib { static ArrayList<MatrixLib> MatrixLib_allInstances = new ArrayList<MatrixLib>();

  MatrixLib() { MatrixLib_allInstances.add(this); }

  static MatrixLib createMatrixLib() { MatrixLib result = new MatrixLib();
    return result; }


  public static ArrayList<Double> rowMult(ArrayList<Double> s, ArrayList<ArrayList<Double>> m)
  {
    ArrayList<Double> result = null;
    result = Ocl.collectSequence(Ocl.integerSubrange(1,s.size()),(i)->{return (double) Ocl.sum(Ocl.collectSequence(Ocl.integerSubrange(1,m.size()),(k)->{ return ((double) (s).get(k - 1)) * (((double) ((ArrayList<Double>) (m).get(k - 1)).get(i - 1))); }));});
    return result;
  }


  public static ArrayList<ArrayList<Double>> matrixMultiplication(ArrayList<ArrayList<Double>> m1, ArrayList<ArrayList<Double>> m2)
  {
    ArrayList<ArrayList<Double>> result = null;
    result = Ocl.collectSequence(m1,(row)->{return MatrixLib.rowMult(row, m2);});
    return result;
  }


  public static ArrayList subRows(ArrayList m, ArrayList<Integer> s)
  {
    ArrayList result = null;
    result = Ocl.collectSequence(Ocl.selectSequence(s,(i)->{return 1 <= i && i <= m.size();}),(j)->{return ((Object) m.get(j - 1));});
    return result;
  }


  public static ArrayList subMatrix(ArrayList<ArrayList> m, ArrayList<Integer> rows, ArrayList<Integer> cols)
  {
    ArrayList result = null;
    result = Ocl.collectSequence(Ocl.selectSequence(rows,(i)->{return 1 <= i && i <= m.size();}),(j)->{return MatrixLib.subRows(((ArrayList) m.get(j - 1)), cols);});
    return result;
  }


  public static ArrayList matrixExcludingRowColumn(ArrayList m, int row, int col)
  {
    ArrayList result = null;
    ArrayList<ArrayList> res = (new ArrayList());
    for (int i : Ocl.integerSubrange(1,m.size()))
    {
      if (i != row)
    {
      ArrayList r = ((ArrayList) ((Object) m.get(i - 1)));
    ArrayList subrow = Ocl.collectSequence(Ocl.selectSequence(Ocl.integerSubrange(1,r.size()),(j)->{return j != col;}),(j)->{return ((Object) r.get(j - 1));});
    res = Ocl.append(res,subrow);
    }
    }
    return res;
  }


  public static ArrayList column(ArrayList m, int i)
  {
    ArrayList result = null;
    if ((((Object) m.get(1 - 1)).getType() == ArrayList.class))
    {
      result = Ocl.collectSequence(m,(r)->{return ((Object) ((ArrayList) r).get(i - 1));});
    }
    else {
      result = ((Object) m.get(i - 1));
    }
    return result;
  }


  public static ArrayList<Integer> shape(Object x)
  {
    ArrayList<Integer> result = null;
    ArrayList<Integer> res = Ocl.collectSequence((new ArrayList()),(var2)->{return 0;});
    if ((x.getType() == ArrayList.class))
    {
      ArrayList sq = ((ArrayList) x);
    res = Ocl.initialiseSequence(sq.size());
    if (sq.size() > 0)
    {
      res = Ocl.union(res,MatrixLib.shape(((Object) sq.get(1 - 1))));
    }
    else {
      return res;
    }
    }
    else {
      return res;
    }
    return res;
  }


  public static ArrayList singleValueMatrix(ArrayList<Integer> sh, Object x)
  {
    ArrayList result = null;
    if (sh.size() == 0)
    {
      return (new ArrayList());
    }
    if (sh.size() == 1)
    {
      return Ocl.collectSequence(Ocl.integerSubrange(1,((int) sh.get(1 - 1))),(var3)->{return x;});
    }
    ArrayList res = (new ArrayList());
    res = Ocl.collectSequence(Ocl.integerSubrange(1,((int) sh.get(1 - 1))),(var4)->{return MatrixLib.singleValueMatrix(Ocl.tail(sh), x);});
    return res;
  }


  public static ArrayList fillMatrixFrom(ArrayList<Double> sq, ArrayList<Integer> sh)
  {
    ArrayList result = null;
    if (sh.size() == 0)
    {
      return (new ArrayList());
    }
    if (sh.size() == 1)
    {
      return Ocl.subrange(sq,1,((int) sh.get(1 - 1)));
    }
    ArrayList<ArrayList> res = (new ArrayList());
    int prod = Ocl.prdint(Ocl.tail(sh));
    for (int i : Ocl.integerSubrange(1,((int) sh.get(1 - 1))))
    {
      ArrayList rowi = MatrixLib.fillMatrixFrom(Ocl.subrange(sq,1 + prod * (i - 1),prod * i), Ocl.tail(sh));
    res = Ocl.append(res,rowi);
    }
    return res;
  }


  public static ArrayList<ArrayList<Double>> identityMatrix(int n)
  {
    ArrayList<ArrayList<Double>> result = null;
    result = Ocl.collectSequence(Ocl.integerSubrange(1,n),(i)->{return Ocl.collectSequence(Ocl.integerSubrange(1,n),(j)->{return ((i == j) ? 1.0 : 0.0);});});
    return result;
  }


  public static ArrayList flattenMatrix(ArrayList m)
  {
    ArrayList result = null;
    if (m.size() == 0)
    {
      return (new ArrayList());
    }
    if ((((Object) m.get(1 - 1)).getType() == ArrayList.class))
    {
      ArrayList sq = ((ArrayList) ((Object) m.get(1 - 1)));
    return Ocl.union(MatrixLib.flattenMatrix(sq),MatrixLib.flattenMatrix(Ocl.tail(m)));
    }
    return m;
  }


  public static double sumMatrix(ArrayList m)
  {
    double result = 0.0;
    if (m.size() == 0)
    {
      return 0.0;
    }
    if ((((Object) m.get(1 - 1)).getType() == ArrayList.class))
    {
      ArrayList sq = ((ArrayList) ((Object) m.get(1 - 1)));
    return MatrixLib.sumMatrix(sq) + MatrixLib.sumMatrix(Ocl.tail(m));
    }
    ArrayList<Double> dmat = Ocl.union(Ocl.initialiseSequence(0.0),m);
    return Ocl.sumdouble(dmat);
  }


  public static double prdMatrix(ArrayList m)
  {
    double result = 0.0;
    if (m.size() == 0)
    {
      return 1.0;
    }
    if ((((Object) m.get(1 - 1)).getType() == ArrayList.class))
    {
      ArrayList sq = ((ArrayList) ((Object) m.get(1 - 1)));
    return MatrixLib.prdMatrix(sq) * (MatrixLib.prdMatrix(Ocl.tail(m)));
    }
    ArrayList<Double> dmat = Ocl.union(Ocl.initialiseSequence(1.0),m);
    return Ocl.prddouble(dmat);
  }


  public static ArrayList elementwiseApply(ArrayList m, Function<Double,Double> f)
  {
    ArrayList result = null;
    if (m.size() == 0)
    {
      return (new ArrayList());
    }
    if ((((Object) m.get(1 - 1)).getType() == ArrayList.class))
    {
      return Ocl.collectSequence(m,(_r)->{return MatrixLib.elementwiseApply(((ArrayList) _r), f);});
    }
    ArrayList<Double> dmat = Ocl.collectSequence((new ArrayList()),(var5)->{return 0.0;});
    for (Object x : m)
    {
      double y = ((double) x);
    dmat = Ocl.append(dmat,(f).apply(y));
    }
    return dmat;
  }


  public static ArrayList selectElements(ArrayList m, Function<Double,Boolean> f)
  {
    ArrayList result = null;
    if (m.size() == 0)
    {
      return (new ArrayList());
    }
    if ((((Object) m.get(1 - 1)).getType() == ArrayList.class))
    {
      return Ocl.collectSequence(m,(_r)->{return MatrixLib.selectElements(((ArrayList) _r), f);});
    }
    ArrayList<Double> dmat = Ocl.collectSequence((new ArrayList()),(var6)->{return 0.0;});
    for (Object x : m)
    {
      double y = ((double) x);
    if (((f).apply(y)))
    {
      dmat = Ocl.append(dmat,y);
    }
    }
    return dmat;
  }


  public static ArrayList elementwiseNot(ArrayList m)
  {
    ArrayList result = null;
    if (m.size() == 0)
    {
      return (new ArrayList());
    }
    if ((((Object) m.get(1 - 1)).getType() == ArrayList.class))
    {
      return Ocl.collectSequence(m,(_r)->{return MatrixLib.elementwiseNot(((ArrayList) _r));});
    }
    ArrayList<Boolean> dmat = Ocl.collectSequence((new ArrayList()),(var7)->{return false;});
    for (Object z : m)
    {
      boolean y = ((boolean) z);
    dmat = Ocl.append(dmat,((y) ? false : true));
    }
    return dmat;
  }


  public static ArrayList elementwiseMult(ArrayList m, double x)
  {
    ArrayList result = null;
    if (m.size() == 0)
    {
      return (new ArrayList());
    }
    if ((((Object) m.get(1 - 1)).getType() == ArrayList.class))
    {
      return Ocl.collectSequence(m,(_r)->{return MatrixLib.elementwiseMult(((ArrayList) _r), x);});
    }
    ArrayList<Double> dmat = Ocl.collectSequence((new ArrayList()),(var8)->{return 0.0;});
    for (Object z : m)
    {
      double y = ((double) z);
    dmat = Ocl.append(dmat,y * x);
    }
    return dmat;
  }


  public static ArrayList elementwisePower(ArrayList m, double x)
  {
    ArrayList result = null;
    if (m.size() == 0)
    {
      return (new ArrayList());
    }
    if ((((Object) m.get(1 - 1)).getType() == ArrayList.class))
    {
      return Ocl.collectSequence(m,(_r)->{return MatrixLib.elementwisePower(((ArrayList) _r), x);});
    }
    ArrayList<Double> dmat = Ocl.collectSequence((new ArrayList()),(var9)->{return 0.0;});
    for (Object z : m)
    {
      double y = ((double) z);
    dmat = Ocl.append(dmat,Math.pow(y,x));
    }
    return dmat;
  }


  public static ArrayList elementwiseAdd(ArrayList m, double x)
  {
    ArrayList result = null;
    if (m.size() == 0)
    {
      return (new ArrayList());
    }
    if ((((Object) m.get(1 - 1)).getType() == ArrayList.class))
    {
      return Ocl.collectSequence(m,(_r)->{return MatrixLib.elementwiseAdd(((ArrayList) _r), x);});
    }
    ArrayList<Double> dmat = Ocl.collectSequence((new ArrayList()),(var10)->{return 0.0;});
    for (Object z : m)
    {
      double y = ((double) z);
    dmat = Ocl.append(dmat,y + x);
    }
    return dmat;
  }


  public static ArrayList elementwiseSubtract(ArrayList m, double x)
  {
    ArrayList result = null;
    if (m.size() == 0)
    {
      return (new ArrayList());
    }
    if ((((Object) m.get(1 - 1)).getType() == ArrayList.class))
    {
      return Ocl.collectSequence(m,(_r)->{return MatrixLib.elementwiseSubtract(((ArrayList) _r), x);});
    }
    ArrayList<Double> dmat = Ocl.collectSequence((new ArrayList()),(var11)->{return 0.0;});
    for (Object z : m)
    {
      double y = ((double) z);
    dmat = Ocl.append(dmat,y - x);
    }
    return dmat;
  }


  public static ArrayList elementwiseDivide(ArrayList m, double x)
  {
    ArrayList result = null;
    if (m.size() == 0)
    {
      return (new ArrayList());
    }
    if ((((Object) m.get(1 - 1)).getType() == ArrayList.class))
    {
      return Ocl.collectSequence(m,(_r)->{return MatrixLib.elementwiseDivide(((ArrayList) _r), x);});
    }
    ArrayList<Double> dmat = Ocl.collectSequence((new ArrayList()),(var12)->{return 0.0;});
    for (Object z : m)
    {
      double y = ((double) z);
    dmat = Ocl.append(dmat,y / x);
    }
    return dmat;
  }


  public static ArrayList elementwiseLess(ArrayList m, double x)
  {
    ArrayList result = null;
    if (m.size() == 0)
    {
      return (new ArrayList());
    }
    if ((((Object) m.get(1 - 1)).getType() == ArrayList.class))
    {
      return Ocl.collectSequence(m,(_r)->{return MatrixLib.elementwiseLess(((ArrayList) _r), x);});
    }
    ArrayList<Boolean> dmat = Ocl.collectSequence((new ArrayList()),(var13)->{return false;});
    for (Object z : m)
    {
      double y = ((double) z);
    dmat = Ocl.append(dmat,((y < x) ? true : false));
    }
    return dmat;
  }


  public static ArrayList elementwiseGreater(ArrayList m, double x)
  {
    ArrayList result = null;
    if (m.size() == 0)
    {
      return (new ArrayList());
    }
    if ((((Object) m.get(1 - 1)).getType() == ArrayList.class))
    {
      return Ocl.collectSequence(m,(_r)->{return MatrixLib.elementwiseGreater(((ArrayList) _r), x);});
    }
    ArrayList<Boolean> dmat = Ocl.collectSequence((new ArrayList()),(var14)->{return false;});
    for (Object z : m)
    {
      double y = ((double) z);
    dmat = Ocl.append(dmat,((y > x) ? true : false));
    }
    return dmat;
  }


  public static ArrayList elementwiseEqual(ArrayList m, double x)
  {
    ArrayList result = null;
    if (m.size() == 0)
    {
      return (new ArrayList());
    }
    if ((((Object) m.get(1 - 1)).getType() == ArrayList.class))
    {
      return Ocl.collectSequence(m,(_r)->{return MatrixLib.elementwiseEqual(((ArrayList) _r), x);});
    }
    ArrayList<Boolean> dmat = Ocl.collectSequence((new ArrayList()),(var15)->{return false;});
    for (Object z : m)
    {
      double y = ((double) z);
    dmat = Ocl.append(dmat,((x == y) ? true : false));
    }
    return dmat;
  }


  public static double detaux(double x1, double x2, double y1, double y2)
  {
    double result = 0.0;
    result = x1 * y2 - x2 * y1;
    return result;
  }


  public static double determinant2(ArrayList m)
  {
    double result = 0.0;
    ArrayList m1 = ((ArrayList) ((Object) (m).get(1 - 1)));
    ArrayList m2 = ((ArrayList) ((Object) (m).get(2 - 1)));
    double d11 = ((double) ((Object) m1.get(1 - 1)));
    double d12 = ((double) ((Object) m1.get(2 - 1)));
    double d21 = ((double) ((Object) m2.get(1 - 1)));
    double d22 = ((double) ((Object) m2.get(2 - 1)));
    return MatrixLib.detaux(d11, d12, d21, d22);
  }


  public static double determinant3(ArrayList<ArrayList> m)
  {
    double result = 0.0;
    ArrayList<ArrayList> subm1 = MatrixLib.subMatrix(m, Ocl.initialiseSequence(2,3), Ocl.initialiseSequence(2,3));
    ArrayList<ArrayList> subm2 = MatrixLib.subMatrix(m, Ocl.initialiseSequence(2,3), Ocl.initialiseSequence(1,3));
    ArrayList<ArrayList> subm3 = MatrixLib.subMatrix(m, Ocl.initialiseSequence(2,3), Ocl.initialiseSequence(1,2));
    ArrayList m1 = ((ArrayList) ((ArrayList) (m).get(1 - 1)));
    return (((double) ((Object) m1.get(1 - 1)))) * MatrixLib.determinant2(subm1) - (((double) ((Object) m1.get(2 - 1)))) * MatrixLib.determinant2(subm2) + (((double) ((Object) m1.get(3 - 1)))) * MatrixLib.determinant2(subm3);
  }


  public static double determinant(ArrayList m)
  {
    double result = 0.0;
    int n = m.size();
    if (n == 1)
    {
      return ((double) ((Object) m.get(1 - 1)));
    }
    if (n == 2)
    {
      return MatrixLib.determinant2(m);
    }
    if (n == 3)
    {
      return MatrixLib.determinant3(m);
    }
    double res = 0.0;
    ArrayList row = ((ArrayList) ((Object) m.get(1 - 1)));
    int factor = 1;
    for (int i : Ocl.integerSubrange(1,n))
    {
      ArrayList submat = MatrixLib.matrixExcludingRowColumn(m, 1, i);
    double det = MatrixLib.determinant(submat);
    double rowi = ((double) ((Object) row.get(i - 1)));
    res = res + factor * rowi * det;
    factor = -factor;
    }
    return res;
  }


  public static ArrayList rowAddition(ArrayList m1, ArrayList m2)
  {
    ArrayList result = null;
    ArrayList<ArrayList> res = (new ArrayList());
    if ((((Object) m1.get(1 - 1)).getType() == ArrayList.class))
    {
      for (int i : Ocl.integerSubrange(1,m1.size()))
    {
      ArrayList sq1 = ((ArrayList) ((Object) m1.get(i - 1)));
    ArrayList sq2 = ((ArrayList) ((Object) m2.get(i - 1)));
    res = Ocl.append(res,MatrixLib.rowAddition(sq1, sq2));
    }
    return res;
    }
    for (int j : Ocl.integerSubrange(1,m1.size()))
    {
      double m1j = ((double) ((Object) m1.get(j - 1)));
    double m2j = ((double) ((Object) m2.get(j - 1)));
    res = Ocl.append(res,m1j + m2j);
    }
    return res;
  }


  public static ArrayList matrixAddition(ArrayList m1, ArrayList m2)
  {
    ArrayList result = null;
    ArrayList<ArrayList> res = (new ArrayList());
    for (int i : Ocl.integerSubrange(1,m1.size()))
    {
      ArrayList sq1 = ((ArrayList) ((Object) m1.get(i - 1)));
    ArrayList sq2 = ((ArrayList) ((Object) m2.get(i - 1)));
    res = Ocl.append(res,MatrixLib.rowAddition(sq1, sq2));
    }
    return res;
  }


  public static ArrayList rowSubtraction(ArrayList m1, ArrayList m2)
  {
    ArrayList result = null;
    ArrayList<ArrayList> res = (new ArrayList());
    if ((((Object) m1.get(1 - 1)).getType() == ArrayList.class))
    {
      for (int i : Ocl.integerSubrange(1,m1.size()))
    {
      ArrayList sq1 = ((ArrayList) ((Object) m1.get(i - 1)));
    ArrayList sq2 = ((ArrayList) ((Object) m2.get(i - 1)));
    res = Ocl.append(res,MatrixLib.rowSubtraction(sq1, sq2));
    }
    return res;
    }
    for (int j : Ocl.integerSubrange(1,m1.size()))
    {
      double m1j = ((double) ((Object) m1.get(j - 1)));
    double m2j = ((double) ((Object) m2.get(j - 1)));
    res = Ocl.append(res,m1j - m2j);
    }
    return res;
  }


  public static ArrayList matrixSubtraction(ArrayList m1, ArrayList m2)
  {
    ArrayList result = null;
    ArrayList<ArrayList> res = (new ArrayList());
    for (int i : Ocl.integerSubrange(1,m1.size()))
    {
      ArrayList sq1 = ((ArrayList) ((Object) m1.get(i - 1)));
    ArrayList sq2 = ((ArrayList) ((Object) m2.get(i - 1)));
    res = Ocl.append(res,MatrixLib.rowSubtraction(sq1, sq2));
    }
    return res;
  }


  public static ArrayList rowDotProduct(ArrayList m1, ArrayList m2)
  {
    ArrayList result = null;
    ArrayList<ArrayList> res = (new ArrayList());
    if ((((Object) m1.get(1 - 1)).getType() == ArrayList.class))
    {
      for (int i : Ocl.integerSubrange(1,m1.size()))
    {
      ArrayList sq1 = ((ArrayList) ((Object) m1.get(i - 1)));
    ArrayList sq2 = ((ArrayList) ((Object) m2.get(i - 1)));
    res = Ocl.append(res,MatrixLib.rowDotProduct(sq1, sq2));
    }
    return res;
    }
    for (int j : Ocl.integerSubrange(1,m1.size()))
    {
      double m1j = ((double) ((Object) m1.get(j - 1)));
    double m2j = ((double) ((Object) m2.get(j - 1)));
    res = Ocl.append(res,m1j * m2j);
    }
    return res;
  }


  public static ArrayList dotProduct(ArrayList m1, ArrayList m2)
  {
    ArrayList result = null;
    ArrayList<ArrayList> res = (new ArrayList());
    for (int i : Ocl.integerSubrange(1,m1.size()))
    {
      ArrayList sq1 = ((ArrayList) ((Object) m1.get(i - 1)));
    ArrayList sq2 = ((ArrayList) ((Object) m2.get(i - 1)));
    res = Ocl.append(res,MatrixLib.rowDotProduct(sq1, sq2));
    }
    return res;
  }


  public static ArrayList rowDotDivision(ArrayList m1, ArrayList m2)
  {
    ArrayList result = null;
    ArrayList<ArrayList> res = (new ArrayList());
    if ((((Object) m1.get(1 - 1)).getType() == ArrayList.class))
    {
      for (int i : Ocl.integerSubrange(1,m1.size()))
    {
      ArrayList m1i = ((ArrayList) ((Object) m1.get(i - 1)));
    ArrayList m2i = ((ArrayList) ((Object) m2.get(i - 1)));
    res = Ocl.append(res,MatrixLib.rowDotDivision(m1i, m2i));
    }
    return res;
    }
    for (int j : Ocl.integerSubrange(1,m1.size()))
    {
      double m1j = ((double) ((Object) m1.get(j - 1)));
    double m2j = ((double) ((Object) m2.get(j - 1)));
    res = Ocl.append(res,m1j / m2j);
    }
    return res;
  }


  public static ArrayList dotDivision(ArrayList m1, ArrayList m2)
  {
    ArrayList result = null;
    ArrayList<ArrayList> res = (new ArrayList());
    for (int i : Ocl.integerSubrange(1,m1.size()))
    {
      ArrayList sq1 = ((ArrayList) ((Object) m1.get(i - 1)));
    ArrayList sq2 = ((ArrayList) ((Object) m2.get(i - 1)));
    res = Ocl.append(res,MatrixLib.rowDotDivision(sq1, sq2));
    }
    return res;
  }


  public static ArrayList rowLess(ArrayList m1, ArrayList m2)
  {
    ArrayList result = null;
    ArrayList<ArrayList> res = (new ArrayList());
    if ((((Object) m1.get(1 - 1)).getType() == ArrayList.class))
    {
      for (int i : Ocl.integerSubrange(1,m1.size()))
    {
      ArrayList m1i = ((ArrayList) ((Object) m1.get(i - 1)));
    ArrayList m2i = ((ArrayList) ((Object) m2.get(i - 1)));
    res = Ocl.append(res,MatrixLib.rowLess(m1i, m2i));
    }
    return res;
    }
    for (int j : Ocl.integerSubrange(1,m1.size()))
    {
      double m1j = ((double) ((Object) m1.get(j - 1)));
    double m2j = ((double) ((Object) m2.get(j - 1)));
    res = Ocl.append(res,((m1j < m2j) ? true : false));
    }
    return res;
  }


  public static ArrayList matrixLess(ArrayList m1, ArrayList m2)
  {
    ArrayList result = null;
    ArrayList<ArrayList> res = (new ArrayList());
    for (int i : Ocl.integerSubrange(1,m1.size()))
    {
      ArrayList m1i = ((ArrayList) ((Object) m1.get(i - 1)));
    ArrayList m2i = ((ArrayList) ((Object) m2.get(i - 1)));
    res = Ocl.append(res,MatrixLib.rowLess(m1i, m2i));
    }
    return res;
  }


  public static ArrayList rowGreater(ArrayList m1, ArrayList m2)
  {
    ArrayList result = null;
    ArrayList<ArrayList> res = (new ArrayList());
    if ((((Object) m1.get(1 - 1)).getType() == ArrayList.class))
    {
      for (int i : Ocl.integerSubrange(1,m1.size()))
    {
      ArrayList m1i = ((ArrayList) ((Object) m1.get(i - 1)));
    ArrayList m2i = ((ArrayList) ((Object) m2.get(i - 1)));
    res = Ocl.append(res,MatrixLib.rowGreater(m1i, m2i));
    }
    return res;
    }
    for (int j : Ocl.integerSubrange(1,m1.size()))
    {
      double m1j = ((double) ((Object) m1.get(j - 1)));
    double m2j = ((double) ((Object) m2.get(j - 1)));
    res = Ocl.append(res,((m1j > m2j) ? true : false));
    }
    return res;
  }


  public static ArrayList matrixGreater(ArrayList m1, ArrayList m2)
  {
    ArrayList result = null;
    ArrayList<ArrayList> res = (new ArrayList());
    for (int i : Ocl.integerSubrange(1,m1.size()))
    {
      ArrayList r1 = ((ArrayList) ((Object) m1.get(i - 1)));
    ArrayList r2 = ((ArrayList) ((Object) m2.get(i - 1)));
    res = Ocl.append(res,MatrixLib.rowGreater(r1, r2));
    }
    return res;
  }


  public static ArrayList rowMaximum(ArrayList m1, ArrayList m2)
  {
    ArrayList result = null;
    ArrayList<ArrayList> res = (new ArrayList());
    if ((((Object) m1.get(1 - 1)).getType() == ArrayList.class))
    {
      for (int i : Ocl.integerSubrange(1,m1.size()))
    {
      ArrayList m1i = ((ArrayList) ((Object) m1.get(i - 1)));
    ArrayList m2i = ((ArrayList) ((Object) m2.get(i - 1)));
    res = Ocl.append(res,MatrixLib.rowMaximum(m1i, m2i));
    }
    return res;
    }
    for (int j : Ocl.integerSubrange(1,m1.size()))
    {
      double m1j = ((double) ((Object) m1.get(j - 1)));
    double m2j = ((double) ((Object) m2.get(j - 1)));
    res = Ocl.append(res,((m1j > m2j) ? m1j : m2j));
    }
    return res;
  }


  public static ArrayList matrixMaximum(ArrayList m1, ArrayList m2)
  {
    ArrayList result = null;
    ArrayList<ArrayList> res = (new ArrayList());
    for (int i : Ocl.integerSubrange(1,m1.size()))
    {
      ArrayList r1 = ((ArrayList) ((Object) m1.get(i - 1)));
    ArrayList r2 = ((ArrayList) ((Object) m2.get(i - 1)));
    res = Ocl.append(res,MatrixLib.rowMaximum(r1, r2));
    }
    return res;
  }


  public static ArrayList rowMinimum(ArrayList m1, ArrayList m2)
  {
    ArrayList result = null;
    ArrayList<ArrayList> res = (new ArrayList());
    if ((((Object) m1.get(1 - 1)).getType() == ArrayList.class))
    {
      for (int i : Ocl.integerSubrange(1,m1.size()))
    {
      ArrayList m1i = ((ArrayList) ((Object) m1.get(i - 1)));
    ArrayList m2i = ((ArrayList) ((Object) m2.get(i - 1)));
    res = Ocl.append(res,MatrixLib.rowMinimum(m1i, m2i));
    }
    return res;
    }
    for (int j : Ocl.integerSubrange(1,m1.size()))
    {
      double m1j = ((double) ((Object) m1.get(j - 1)));
    double m2j = ((double) ((Object) m2.get(j - 1)));
    res = Ocl.append(res,((m1j < m2j) ? m1j : m2j));
    }
    return res;
  }


  public static ArrayList matrixMinimum(ArrayList m1, ArrayList m2)
  {
    ArrayList result = null;
    ArrayList<ArrayList> res = (new ArrayList());
    for (int i : Ocl.integerSubrange(1,m1.size()))
    {
      ArrayList r1 = ((ArrayList) ((Object) m1.get(i - 1)));
    ArrayList r2 = ((ArrayList) ((Object) m2.get(i - 1)));
    res = Ocl.append(res,MatrixLib.rowMinimum(r1, r2));
    }
    return res;
  }


  public static ArrayList transpose(ArrayList m)
  {
    ArrayList result = null;
    if ((((Object) m.get(1 - 1)).getType() == ArrayList.class))
    {
      {}
    }
    else {
      return m;
    }
    ArrayList<ArrayList> res = (new ArrayList());
    for (int i : Ocl.integerSubrange(1,m.size()))
    {
      res = Ocl.append(res,MatrixLib.column(m, i));
    }
    return res;
  }

}

class Sequence { static ArrayList<Sequence> Sequence_allInstances = new ArrayList<Sequence>();

  Sequence() { Sequence_allInstances.add(this); }

  static Sequence createSequence() { Sequence result = new Sequence();
    return result; }

}

