package com.upokecenter.cbor;
/*
Written by Peter O. in 2014.
Any copyright is dedicated to the Public Domain.
http://creativecommons.org/publicdomain/zero/1.0/
If you like this, you should donate to Peter O.
at: http://peteroupc.github.io/
 */

import com.upokecenter.util.*;
import com.upokecenter.numbers.*;

  class CBORExtendedDecimal implements ICBORNumber
  {
    public boolean IsPositiveInfinity(Object obj) {
      EDecimal ed = (EDecimal)obj;
      return ed.IsPositiveInfinity();
    }

    public boolean IsInfinity(Object obj) {
      EDecimal ed = (EDecimal)obj;
      return ed.IsInfinity();
    }

    public boolean IsNegativeInfinity(Object obj) {
      EDecimal ed = (EDecimal)obj;
      return ed.IsNegativeInfinity();
    }

    public boolean IsNaN(Object obj) {
      EDecimal ed = (EDecimal)obj;
      return ed.IsNaN();
    }

    public double AsDouble(Object obj) {
      EDecimal ed = (EDecimal)obj;
      return ed.ToDouble();
    }

    public EDecimal AsExtendedDecimal(Object obj) {
      EDecimal ed = (EDecimal)obj;
      return ed;
    }

    public EFloat AsExtendedFloat(Object obj) {
      EDecimal ed = (EDecimal)obj;
      return ed.ToEFloat();
    }

    public float AsSingle(Object obj) {
      EDecimal ed = (EDecimal)obj;
      return ed.ToSingle();
    }

    public EInteger AsEInteger(Object obj) {
      EDecimal ed = (EDecimal)obj;
      return ed.ToEInteger();
    }

    public long AsInt64(Object obj) {
      EDecimal ef = (EDecimal)obj;
      if (this.CanTruncatedIntFitInInt64(obj)) {
        EInteger bi = ef.ToEInteger();
        return bi.ToInt64Checked();
      }
      throw new ArithmeticException("This Object's value is out of range");
    }

    public boolean CanFitInSingle(Object obj) {
      EDecimal ef = (EDecimal)obj;
      return (!ef.isFinite()) ||
      (ef.compareTo(EDecimal.FromSingle(ef.ToSingle())) == 0);
    }

    public boolean CanFitInDouble(Object obj) {
      EDecimal ef = (EDecimal)obj;
      return (!ef.isFinite()) ||
      (ef.compareTo(EDecimal.FromDouble(ef.ToDouble())) == 0);
    }

    public boolean CanFitInInt32(Object obj) {
      return this.IsIntegral(obj) && this.CanTruncatedIntFitInInt32(obj);
    }

    public boolean CanFitInInt64(Object obj) {
      return this.IsIntegral(obj) && this.CanTruncatedIntFitInInt64(obj);
    }

    public boolean CanTruncatedIntFitInInt64(Object obj) {
      EDecimal ef = (EDecimal)obj;
      if (!ef.isFinite()) {
        return false;
      }
      if (ef.isZero()) {
        return true;
      }
      if (ef.getExponent().compareTo(EInteger.FromInt64(21)) >= 0) {
        return false;
      }
      EInteger bi = ef.ToEInteger();
      return bi.GetSignedBitLength() <= 63;
    }

    public boolean CanTruncatedIntFitInInt32(Object obj) {
      EDecimal ef = (EDecimal)obj;
      if (!ef.isFinite()) {
        return false;
      }
      if (ef.isZero()) {
        return true;
      }
      if (ef.getExponent().compareTo(EInteger.FromInt64(11)) >= 0) {
        return false;
      }
      EInteger bi = ef.ToEInteger();
      return bi.CanFitInInt32();
    }

    public boolean IsZero(Object obj) {
      EDecimal ed = (EDecimal)obj;
      return ed.isZero();
    }

    public int Sign(Object obj) {
      EDecimal ed = (EDecimal)obj;
      return ed.IsNaN() ? 2 : ed.signum();
    }

    public boolean IsIntegral(Object obj) {
      EDecimal ed = (EDecimal)obj;
      return ed.isFinite() && ((ed.getExponent().signum() >= 0) ||
      (ed.compareTo(EDecimal.FromEInteger(ed.ToEInteger())) ==
      0));
    }

    public int AsInt32(Object obj, int minValue, int maxValue) {
      EDecimal ef = (EDecimal)obj;
      if (this.CanTruncatedIntFitInInt32(obj)) {
        EInteger bi = ef.ToEInteger();
        int ret = bi.ToInt32Checked();
        if (ret >= minValue && ret <= maxValue) {
          return ret;
        }
      }
      throw new ArithmeticException("This Object's value is out of range");
    }

    public Object Negate(Object obj) {
      EDecimal ed = (EDecimal)obj;
      return ed.Negate();
    }

    public Object Abs(Object obj) {
      EDecimal ed = (EDecimal)obj;
      return ed.Abs();
    }

    public ERational AsExtendedRational(Object obj) {
      return ERational.FromEDecimal((EDecimal)obj);
    }

    public boolean IsNegative(Object obj) {
      return ((EDecimal)obj).isNegative();
    }
  }
