package com.upokecenter.test;
/*
Written by Peter O. in 2013.
Any copyright is dedicated to the Public Domain.
http://creativecommons.org/publicdomain/zero/1.0/
If you like this, you should donate to Peter O.
at: http://peteroupc.github.io/
 */

import java.io.*;
import org.junit.Assert;
import org.junit.Test;
import com.upokecenter.util.*;
import com.upokecenter.cbor.*;
import com.upokecenter.numbers.*;

  public class CBORTest {
    public static void TestCBORMapAdd() {
      CBORObject cbor = CBORObject.NewMap();
      cbor.Add(1, 2);
      if (!(cbor.ContainsKey(CBORObject.FromObject(1))))Assert.fail();
      Assert.assertEquals((int)2, cbor.get(CBORObject.FromObject(1)));
      {
        String stringTemp = cbor.ToJSONString();
        Assert.assertEquals(
        "{\"1\":2}",
        stringTemp);
      }
      cbor.Add("hello", 2);
      if (!(cbor.ContainsKey("hello"))) {
 Assert.fail();
 }
      if (!(cbor.ContainsKey(CBORObject.FromObject("hello"))))Assert.fail();
      Assert.assertEquals((int)2, cbor.get("hello"));
      cbor.Set(1, 3);
      if (!(cbor.ContainsKey(CBORObject.FromObject(1))))Assert.fail();
      Assert.assertEquals((int)3, cbor.get(CBORObject.FromObject(1)));
    }

    @Test
    public void TestArray() {
      CBORObject cbor = CBORObject.FromJSONString("[]");
      cbor.Add(CBORObject.FromObject(3));
      cbor.Add(CBORObject.FromObject(4));
      byte[] bytes = cbor.EncodeToBytes();
      TestCommon.AssertByteArraysEqual(
        new byte[] { (byte)(0x80 | 2), 3, 4 },
        bytes);
      cbor = CBORObject.FromObject(new String[] { "a", "b", "c", "d", "e" });
      Assert.assertEquals("[\"a\",\"b\",\"c\",\"d\",\"e\"]", cbor.ToJSONString());
      CBORTestCommon.AssertRoundTrip(cbor);
      cbor = CBORObject.DecodeFromBytes(new byte[] { (byte)0x9f, 0, 1, 2, 3, 4, 5,
                    6, 7, (byte)0xff });
      {
        String stringTemp = cbor.ToJSONString();
        Assert.assertEquals(
        "[0,1,2,3,4,5,6,7]",
        stringTemp);
      }
    }

    @Test
    public void TestEInteger() {
      RandomGenerator r = new RandomGenerator();
      for (int i = 0; i < 500; ++i) {
        EInteger bi = RandomObjects.RandomEInteger(r);
        CBORTestCommon.AssertSer(
          CBORObject.FromObject(bi),
          bi.toString());
        if (!(CBORObject.FromObject(bi).isIntegral())) {
 Assert.fail();
 }
        CBORTestCommon.AssertRoundTrip(CBORObject.FromObject(bi));
        CBORTestCommon.AssertRoundTrip(CBORObject.FromObject(
          EDecimal.FromString(bi.toString() + "e1")));
      }
      EInteger[] ranges = {
       EInteger.FromString("-9223372036854776320"),
  EInteger.FromString("-9223372036854775296"),
  EInteger.FromString("-512"),
  EInteger.FromString("512"),
  EInteger.FromString("9223372036854775295"),
  EInteger.FromString("9223372036854776319"),
  EInteger.FromString("18446744073709551103"),
  EInteger.FromString("18446744073709552127")
      };
      for (int i = 0; i < ranges.length; i += 2) {
        EInteger bigintTemp = ranges[i];
        while (true) {
          CBORTestCommon.AssertSer(
            CBORObject.FromObject(bigintTemp),
            bigintTemp.toString());
          if (bigintTemp.equals(ranges[i + 1])) {
            break;
          }
          bigintTemp = bigintTemp.Add(EInteger.FromInt32(1));
        }
      }
    }

    @Test
    public void TestBigNumBytes() {
      CBORObject o = null;
      o = CBORTestCommon.FromBytesTestAB(new byte[] { (byte)0xc2, 0x41, (byte)0x88 });
      Assert.assertEquals(EInteger.FromRadixString("88", 16), o.AsEInteger());
      o = CBORTestCommon.FromBytesTestAB(new byte[] { (byte)0xc2, 0x42, (byte)0x88, 0x77 });
      Assert.assertEquals(EInteger.FromRadixString("8877", 16), o.AsEInteger());
      o = CBORTestCommon.FromBytesTestAB(new byte[] { (byte)0xc2, 0x44, (byte)0x88, 0x77,
        0x66,
        0x55 });
      Assert.assertEquals(
  EInteger.FromRadixString("88776655", 16),
  o.AsEInteger());
      o = CBORTestCommon.FromBytesTestAB(new byte[] { (byte)0xc2, 0x47, (byte)0x88, 0x77,
        0x66,
        0x55, 0x44, 0x33, 0x22 });
      Assert.assertEquals(
  EInteger.FromRadixString("88776655443322", 16),
  o.AsEInteger());
    }

    @Test
    public void TestByte() {
      for (int i = 0; i <= 255; ++i) {
        CBORTestCommon.AssertSer(
          CBORObject.FromObject((byte)i),
          TestCommon.IntToString(i));
      }
    }

    @Test
    public void TestByteArray() {
      CBORTestCommon.AssertSer(
        CBORObject.FromObject(new byte[] { 0x20, 0x78 }),
        "h'2078'");
    }

    @Test
    public void TestByteStringStream() {
      CBORTestCommon.FromBytesTestAB(
        new byte[] { 0x5f, 0x41, 0x20, 0x41, 0x20, (byte)0xff });
    }
    @Test(expected = CBORException.class)
    public void TestByteStringStreamNoIndefiniteWithinDefinite() {
      CBORTestCommon.FromBytesTestAB(new byte[] { 0x5f, 0x41, 0x20, 0x5f, 0x41,
        0x20, (byte)0xff, (byte)0xff });
    }
    @Test(expected = CBORException.class)
    public void TestByteStringStreamNoTagsBeforeDefinite() {
      CBORTestCommon.FromBytesTestAB(new byte[] { 0x5f, 0x41, 0x20, (byte)0xc2, 0x41,
        0x20, (byte)0xff });
    }

    private static String ObjectMessage(CBORObject obj) {
      return new StringBuilder()
        .append("CBORObject.DecodeFromBytes(")
           .append(TestCommon.ToByteArrayString(obj.EncodeToBytes()))
           .append("); /").append("/ ").append(obj.ToJSONString()).toString();
    }

    @Test
    public void TestCanFitIn() {
      RandomGenerator r = new RandomGenerator();
      for (int i = 0; i < 5000; ++i) {
        CBORObject ed = CBORTestCommon.RandomNumber(r);
        EDecimal ed2;

        ed2 = EDecimal.FromDouble(AsED(ed).ToDouble());
        if ((AsED(ed).compareTo(ed2) == 0) != ed.CanFitInDouble()) {
          Assert.fail(ObjectMessage(ed));
        }
        ed2 = EDecimal.FromSingle(AsED(ed).ToSingle());
        if ((AsED(ed).compareTo(ed2) == 0) != ed.CanFitInSingle()) {
          Assert.fail(ObjectMessage(ed));
        }
        if (!ed.IsInfinity() && !ed.IsNaN()) {
          ed2 = EDecimal.FromEInteger(AsED(ed)
                    .ToEInteger());
          if ((AsED(ed).compareTo(ed2) == 0) != ed.isIntegral()) {
            Assert.fail(ObjectMessage(ed));
          }
        }
        if (!ed.IsInfinity() && !ed.IsNaN()) {
          EInteger bi = ed.AsEInteger();
          if (ed.isIntegral()) {
            if ((bi.GetSignedBitLength() <= 31) != ed.CanFitInInt32()) {
              Assert.fail(ObjectMessage(ed));
            }
          }
       if ((bi.GetSignedBitLength() <= 31) !=
            ed.CanTruncatedIntFitInInt32()) {
            Assert.fail(ObjectMessage(ed));
          }
          if (ed.isIntegral()) {
            if ((bi.GetSignedBitLength() <= 63) != ed.CanFitInInt64()) {
              Assert.fail(ObjectMessage(ed));
            }
          }
       if ((bi.GetSignedBitLength() <= 63) !=
            ed.CanTruncatedIntFitInInt64()) {
            Assert.fail(ObjectMessage(ed));
          }
        }
      }
    }

    @Test
    public void TestCanFitInSpecificCases() {
      CBORObject cbor = CBORObject.DecodeFromBytes(new byte[] { (byte)0xfb,
        0x41, (byte)0xe0, (byte)0x85, 0x48, 0x2d, 0x14, 0x47, 0x7a });  // 2217361768.63373
      Assert.assertEquals(
  EInteger.FromString("2217361768"),
  cbor.AsEInteger());
      if (cbor.AsEInteger().GetSignedBitLength() <= 31) {
 Assert.fail();
 }
      if (cbor.CanTruncatedIntFitInInt32()) {
 Assert.fail();
 }
      cbor = CBORObject.DecodeFromBytes(new byte[] { (byte)0xc5, (byte)0x82,
        0x18, 0x2f, 0x32 });  // -2674012278751232
      Assert.assertEquals(52, cbor.AsEInteger().GetSignedBitLength());
      if (!(cbor.CanFitInInt64())) {
 Assert.fail();
 }
      if (CBORObject.FromObject(2554895343L).CanFitInSingle()) {
 Assert.fail();
 }
      cbor = CBORObject.DecodeFromBytes(new byte[] { (byte)0xc5, (byte)0x82,
        0x10, 0x38, 0x64 });  // -6619136
      Assert.assertEquals(EInteger.FromString("-6619136"), cbor.AsEInteger());
      Assert.assertEquals(-6619136, cbor.AsInt32());
      if (!(cbor.CanTruncatedIntFitInInt32())) {
 Assert.fail();
 }
    }

    @Test
    public void TestCBOREInteger() {
      CBORObject o = CBORObject.DecodeFromBytes(new byte[] { 0x3b, (byte)0xce,
        (byte)0xe2, 0x5a, 0x57, (byte)0xd8, 0x21, (byte)0xb9, (byte)0xa7 });
      Assert.assertEquals(
        EInteger.FromString("-14907577049884506536"),
        o.AsEInteger());
    }

    @Test
    public void TestCBORExceptions() {
      try {
        CBORObject.NewArray().Remove(null);
        Assert.fail("Should have failed");
      } catch (NullPointerException ex) {
        new Object();
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.NewMap().Remove(null);
        Assert.fail("Should have failed");
      } catch (NullPointerException ex) {
        new Object();
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.NewArray().Add(CBORObject.Null);
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.NewMap().Add(CBORObject.True);
        Assert.fail("Should have failed");
      } catch (IllegalStateException ex) {
        new Object();
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.True.Remove(CBORObject.True);
        Assert.fail("Should have failed");
      } catch (IllegalStateException ex) {
        new Object();
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.FromObject(0).Remove(CBORObject.True);
        Assert.fail("Should have failed");
      } catch (IllegalStateException ex) {
        new Object();
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.FromObject("").Remove(CBORObject.True);
        Assert.fail("Should have failed");
      } catch (IllegalStateException ex) {
        new Object();
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.NewArray().AsEFloat();
        Assert.fail("Should have failed");
      } catch (IllegalStateException ex) {
        new Object();
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.NewMap().AsEFloat();
        Assert.fail("Should have failed");
      } catch (IllegalStateException ex) {
        new Object();
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.True.AsEFloat();
        Assert.fail("Should have failed");
      } catch (IllegalStateException ex) {
        new Object();
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.False.AsEFloat();
        Assert.fail("Should have failed");
      } catch (IllegalStateException ex) {
        new Object();
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.Undefined.AsEFloat();
        Assert.fail("Should have failed");
      } catch (IllegalStateException ex) {
        new Object();
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.FromObject("").AsEFloat();
        Assert.fail("Should have failed");
      } catch (IllegalStateException ex) {
        new Object();
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
    }

    @Test
    public void TestCBORFromArray() {
      CBORObject o = CBORObject.FromObject(new int[] { 1, 2, 3 });
      Assert.assertEquals(3, o.size());
      Assert.assertEquals(1, o.get(0).AsInt32());
      Assert.assertEquals(2, o.get(1).AsInt32());
      Assert.assertEquals(3, o.get(2).AsInt32());
      CBORTestCommon.AssertRoundTrip(o);
    }

    @Test
    public void TestCBORInfinity() {
      {
        String stringTemp =
          CBORObject.FromObject(CBORTestCommon.FloatNegInf).toString();
        Assert.assertEquals(
        "-Infinity",
        stringTemp);
      }
      {
        String stringTemp =
          CBORObject.FromObject(CBORTestCommon.RatPosInf).toString();
        Assert.assertEquals(
        "Infinity",
        stringTemp);
      }

  CBORTestCommon.AssertRoundTrip(CBORObject.FromObject(CBORTestCommon.FloatNegInf));

  CBORTestCommon.AssertRoundTrip(CBORObject.FromObject(CBORTestCommon.RatPosInf));
      if (!(CBORObject.FromObject(CBORTestCommon.FloatNegInf)
                    .IsInfinity())) {
 Assert.fail();
 }
      if (!(CBORObject.FromObject(CBORTestCommon.RatPosInf)
                    .IsInfinity())) {
 Assert.fail();
 }
      if (!(CBORObject.FromObject(CBORTestCommon.FloatNegInf)
                    .IsNegativeInfinity())) {
 Assert.fail();
 }
      if (!(CBORObject.FromObject(CBORTestCommon.RatPosInf)
                    .IsPositiveInfinity())) {
 Assert.fail();
 }
      if (!(CBORObject.PositiveInfinity.IsPositiveInfinity())) {
 Assert.fail();
 }
      if (!(CBORObject.NegativeInfinity.IsNegativeInfinity())) {
 Assert.fail();
 }
      if (!(CBORObject.NaN.IsNaN())) {
 Assert.fail();
 }

  CBORTestCommon.AssertRoundTrip(CBORObject.FromObject(CBORTestCommon.DecNegInf));

  CBORTestCommon.AssertRoundTrip(CBORObject.FromObject(CBORTestCommon.FloatNegInf));

  CBORTestCommon.AssertRoundTrip(CBORObject.FromObject(Double.NEGATIVE_INFINITY));

  CBORTestCommon.AssertRoundTrip(CBORObject.FromObject(Float.NEGATIVE_INFINITY));

  CBORTestCommon.AssertRoundTrip(CBORObject.FromObject(CBORTestCommon.DecPosInf));

  CBORTestCommon.AssertRoundTrip(CBORObject.FromObject(CBORTestCommon.FloatPosInf));

  CBORTestCommon.AssertRoundTrip(CBORObject.FromObject(Double.POSITIVE_INFINITY));

  CBORTestCommon.AssertRoundTrip(CBORObject.FromObject(Float.POSITIVE_INFINITY));
    }

    @Test
    public void TestCompareB() {
      {
  String stringTemp = CBORObject.DecodeFromBytes(new byte[] { (byte)0xfa, 0x7f,
        (byte)0x80, 0x00, 0x00 }).AsERational().toString();
        Assert.assertEquals(
        "Infinity",
        stringTemp);
      }
      {
    CBORObject objectTemp = CBORObject.DecodeFromBytes(new byte[] { (byte)0xc5,
  (byte)0x82, 0x38, (byte)0xc7, 0x3b, 0x00, 0x00, 0x08, (byte)0xbf,
  (byte)0xda, (byte)0xaf, 0x73, 0x46 });
   CBORObject objectTemp2 = CBORObject.DecodeFromBytes(new byte[] { 0x3b, 0x5a,
  (byte)0x9b, (byte)0x9a, (byte)0x9c, (byte)0xb4, (byte)0x95, (byte)0xbf,
  0x71 });
        AddSubCompare(objectTemp, objectTemp2);
      }
      {
    CBORObject objectTemp = CBORObject.DecodeFromBytes(new byte[] { (byte)0xfa,
  0x1f, (byte)0x80, (byte)0xdb, (byte)0x9b });
   CBORObject objectTemp2 = CBORObject.DecodeFromBytes(new byte[] { (byte)0xfb,
  0x31, (byte)0x90, (byte)0xea, 0x16, (byte)0xbe, (byte)0x80, 0x0b, 0x37 });
        AddSubCompare(objectTemp, objectTemp2);
      }
      CBORObject cbor = CBORObject.FromObjectAndTag(
        Double.NEGATIVE_INFINITY,
        1956611);
      CBORTestCommon.AssertRoundTrip(cbor);
      cbor =

        CBORObject.FromObjectAndTag(
          CBORObject.FromObject(Double.NEGATIVE_INFINITY),
          1956611);
      CBORTestCommon.AssertRoundTrip(cbor);
      cbor =

        CBORObject.FromObjectAndTag(
          CBORObject.FromObject(CBORTestCommon.FloatNegInf),
          1956611);
      CBORTestCommon.AssertRoundTrip(cbor);
      cbor =

        CBORObject.FromObjectAndTag(
          CBORObject.FromObject(CBORTestCommon.DecNegInf),
          1956611);
      CBORTestCommon.AssertRoundTrip(cbor);
      cbor =

        CBORObject.FromObjectAndTag(
          CBORObject.FromObject(CBORTestCommon.FloatNegInf),
          1956611);
      CBORTestCommon.AssertRoundTrip(cbor);
    }

    @Test
    public void TestDecFracCompareIntegerVsBigFraction() {
      CBORObject o1 = null;
      CBORObject o2 = null;
      o1 = CBORObject.DecodeFromBytes(new byte[] { (byte)0xfb, (byte)0x8b, 0x44,
        (byte)0xf2, (byte)0xa9, 0x0c, 0x27, 0x42, 0x28 });
      o2 = CBORObject.DecodeFromBytes(new byte[] { (byte)0xc5, (byte)0x82, 0x38,
        (byte)0xa4, (byte)0xc3, 0x50, 0x02, (byte)0x98, (byte)0xc5, (byte)0xa8,
        0x02, (byte)0xc1, (byte)0xf6, (byte)0xc0, 0x1a, (byte)0xbe, 0x08,
          0x04, (byte)0x86, (byte)0x99, 0x3e, (byte)0xf1 });
      AddSubCompare(o1, o2);
    }

    @Test
    public void TestDecimalFrac() {
      CBORTestCommon.FromBytesTestAB(
        new byte[] { (byte)0xc4, (byte)0x82, 0x3, 0x1a, 1, 2, 3, 4 });
    }
    @Test(expected = CBORException.class)
    public void TestDecimalFracExactlyTwoElements() {
      CBORTestCommon.FromBytesTestAB(new byte[] { (byte)0xc4, (byte)0x82, (byte)0xc2, 0x41, 1 });
    }
    @Test(expected = CBORException.class)
    public void TestDecimalFracExponentMustNotBeBignum() {
      CBORTestCommon.FromBytesTestAB(new byte[] { (byte)0xc4, (byte)0x82, (byte)0xc2, 0x41, 1,
        0x1a,
        1, 2, 3, 4 });
    }

    @Test
    public void TestDecimalFracMantissaMayBeBignum() {
      CBORObject o = CBORTestCommon.FromBytesTestAB(
        new byte[] { (byte)0xc4, (byte)0x82, 0x3, (byte)0xc2, 0x41, 1 });
      Assert.assertEquals(
        EDecimal.FromString("1e3"),
        o.AsEDecimal());
    }

    @Test
    public void TestDivide() {
      RandomGenerator r = new RandomGenerator();
      for (int i = 0; i < 3000; ++i) {
        CBORObject o1 =
          CBORObject.FromObject(RandomObjects.RandomEInteger(r));
      CBORObject o2 = CBORObject.FromObject(RandomObjects.RandomEInteger(r));
        if (o2.isZero()) {
          continue;
        }
        ERational er = new ERational(o1.AsEInteger(), o2.AsEInteger());
        {
          ERational objectTemp = er;
          ERational objectTemp2 = CBORObject.Divide(
  o1,
  o2).AsERational();
          TestCommon.CompareTestEqual(objectTemp, objectTemp2);
        }
      }
    }

    @Test
    public void TestDouble() {
      if
        (!CBORObject.FromObject(Double.POSITIVE_INFINITY).IsPositiveInfinity()) {
        Assert.fail("Not positive infinity");
      }
      CBORTestCommon.AssertSer(
        CBORObject.FromObject(Double.POSITIVE_INFINITY),
        "Infinity");
      CBORTestCommon.AssertSer(
        CBORObject.FromObject(Double.NEGATIVE_INFINITY),
        "-Infinity");
      CBORTestCommon.AssertSer(
        CBORObject.FromObject(Double.NaN),
        "NaN");
      CBORObject oldobj = null;
      for (int i = -65539; i <= 65539; ++i) {
        CBORObject o = CBORObject.FromObject((double)i);
        if (!(o.CanFitInDouble())) {
 Assert.fail();
 }
        if (!(o.CanFitInInt32())) {
 Assert.fail();
 }
        if (!(o.isIntegral())) {
 Assert.fail();
 }
        CBORTestCommon.AssertSer(
          o,
          TestCommon.IntToString(i));
        if (oldobj != null) {
          TestCommon.CompareTestLess(oldobj, o);
        }
        oldobj = o;
      }
    }

    @Test
    public void TestExample() {
      // The following creates a CBOR map and adds
      // several kinds of objects to it
      CBORObject cbor = CBORObject.NewMap().Add("item", "any String")
        .Add("number", 42).Add("map", CBORObject.NewMap().Add("number", 42))
        .Add("array", CBORObject.NewArray().Add(999f).Add("xyz"))
        .Add("bytes", new byte[] { 0, 1, 2 });
      // The following converts the map to CBOR
      cbor.EncodeToBytes();
      // The following converts the map to JSON
      cbor.ToJSONString();
    }

    @Test(timeout = 5000)
    public void TestExtendedExtremeExponent() {
      // Values with extremely high or extremely low exponents;
      // we just check whether this test method runs reasonably fast
      // for all these test cases
      CBORObject obj;
      obj = CBORObject.DecodeFromBytes(new byte[] { (byte)0xc4, (byte)0x82,
     0x3a, 0x00, 0x1c, 0x2d, 0x0d, 0x1a, 0x13, 0x6c, (byte)0xa1, (byte)0x97 });
      CBORTestCommon.AssertRoundTrip(obj);
      obj = CBORObject.DecodeFromBytes(new byte[] { (byte)0xda, 0x00, 0x14,
        0x57, (byte)0xce, (byte)0xc5, (byte)0x82, 0x1a, 0x46, 0x5a, 0x37,
        (byte)0x87, (byte)0xc3, 0x50, 0x5e, (byte)0xec, (byte)0xfd, 0x73,
          0x50, 0x64, (byte)0xa1, 0x1f, 0x10, (byte)0xc4, (byte)0xff,
          (byte)0xf2, (byte)0xc4, (byte)0xc9, 0x65, 0x12 });
      CBORTestCommon.AssertRoundTrip(obj);
      int actual = CBORObject.FromObject(
        EDecimal.FromString("333333e-2"))
        .compareTo(CBORObject.FromObject(EFloat.Create(
          EInteger.FromString("5234222"),
          EInteger.FromString("-24936668661488"))));
      Assert.assertEquals(1, actual);
    }

    @Test
    public void TestFloat() {
      CBORTestCommon.AssertSer(
        CBORObject.FromObject(Float.POSITIVE_INFINITY),
        "Infinity");
      CBORTestCommon.AssertSer(
        CBORObject.FromObject(Float.NEGATIVE_INFINITY),
        "-Infinity");
      CBORTestCommon.AssertSer(
        CBORObject.FromObject(Float.NaN),
        "NaN");
      for (int i = -65539; i <= 65539; ++i) {
        CBORTestCommon.AssertSer(
          CBORObject.FromObject((float)i),
          TestCommon.IntToString(i));
      }
    }
    @Test
    public void TestHalfPrecision() {
      CBORObject o = CBORObject.DecodeFromBytes(
        new byte[] { (byte)0xf9, 0x7c, 0x00 });
      Assert.assertEquals(Float.POSITIVE_INFINITY, o.AsSingle(), 0f);
      o = CBORObject.DecodeFromBytes(
        new byte[] { (byte)0xf9, 0x00, 0x00 });
      Assert.assertEquals((float)0, o.AsSingle(), 0f);
      o = CBORObject.DecodeFromBytes(
        new byte[] { (byte)0xf9, (byte)0xfc, 0x00 });
      Assert.assertEquals(Float.NEGATIVE_INFINITY, o.AsSingle(), 0f);
      o = CBORObject.DecodeFromBytes(
        new byte[] { (byte)0xf9, 0x7e, 0x00 });
      if (!(Float.isNaN(o.AsSingle())))Assert.fail();
    }

    @Test
    public void TestJSON() {
      CBORObject o;
      o = CBORObject.FromJSONString("[1,2,null,true,false,\"\"]");
      Assert.assertEquals(6, o.size());
      Assert.assertEquals(1, o.get(0).AsInt32());
      Assert.assertEquals(2, o.get(1).AsInt32());
      Assert.assertEquals(CBORObject.Null, o.get(2));
      Assert.assertEquals(CBORObject.True, o.get(3));
      Assert.assertEquals(CBORObject.False, o.get(4));
      Assert.assertEquals("", o.get(5).AsString());
      o = CBORObject.FromJSONString("[1.5,2.6,3.7,4.0,222.22]");
      double actual = o.get(0).AsDouble();
      Assert.assertEquals((double)1.5, actual, 0);
      {
java.io.ByteArrayInputStream ms2a = null;
try {
ms2a = new java.io.ByteArrayInputStream(new byte[] { });

        try {
          CBORObject.ReadJSON(ms2a);
          Assert.fail("Should have failed");
        } catch (CBORException ex) {
          new Object();
        } catch (Exception ex) {
          Assert.fail(ex.toString());
          throw new IllegalStateException("", ex);
        }
}
finally {
try { if (ms2a != null) {
 ms2a.close();
 } } catch (java.io.IOException ex) {}
}
}
      {
java.io.ByteArrayInputStream ms2b = null;
try {
ms2b = new java.io.ByteArrayInputStream(new byte[] { 0x20 });

        try {
          CBORObject.ReadJSON(ms2b);
          Assert.fail("Should have failed");
        } catch (CBORException ex) {
          new Object();
        } catch (Exception ex) {
          Assert.fail(ex.toString());
          throw new IllegalStateException("", ex);
        }
}
finally {
try { if (ms2b != null) {
 ms2b.close();
 } } catch (java.io.IOException ex) {}
}
}
      try {
        CBORObject.FromJSONString("");
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
        new Object();
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.FromJSONString("[.1]");
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
        new Object();
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.FromJSONString("[-.1]");
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
        new Object();
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.FromJSONString("\u0020");
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
        new Object();
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      {
        String stringTemp = CBORObject.FromJSONString("true").ToJSONString();
        Assert.assertEquals(
        "true",
        stringTemp);
      }
      {
        String stringTemp = CBORObject.FromJSONString(" true ").ToJSONString();
        Assert.assertEquals(
        "true",
        stringTemp);
      }
      {
        String stringTemp = CBORObject.FromJSONString("false").ToJSONString();
        Assert.assertEquals(
        "false",
        stringTemp);
      }
      {
        String stringTemp = CBORObject.FromJSONString("null").ToJSONString();
        Assert.assertEquals(
        "null",
        stringTemp);
      }
      {
        String stringTemp = CBORObject.FromJSONString("5").ToJSONString();
        Assert.assertEquals(
        "5",
        stringTemp);
      }
    }

    @Test
    public void TestJSONEscapedChars() {
      CBORObject o = CBORObject.FromJSONString(
        "[\"\\r\\n\\u0006\\u000E\\u001A\\\\\\\"\"]");
      Assert.assertEquals(1, o.size());
      {
        String stringTemp = o.get(0).AsString();
        Assert.assertEquals(
        "\r\n\u0006\u000E\u001A\\\"",
        stringTemp);
      }
      {
        String stringTemp = o.ToJSONString();
        Assert.assertEquals(
        "[\"\\r\\n\\u0006\\u000E\\u001A\\\\\\\"\"]",
        stringTemp);
      }
      CBORTestCommon.AssertRoundTrip(o);
    }

    @Test
    public void TestLong() {
      long[] ranges = {
        -65539, 65539, 0xfffff000L, 0x100000400L,
        Long.MAX_VALUE - 1000, Long.MAX_VALUE, Long.MIN_VALUE, Long.MIN_VALUE +
          1000 };
      for (int i = 0; i < ranges.length; i += 2) {
        long j = ranges[i];
        while (true) {
          if (!(CBORObject.FromObject(j).isIntegral())) {
 Assert.fail();
 }
          if (!(CBORObject.FromObject(j).CanFitInInt64())) {
 Assert.fail();
 }
          if (!(CBORObject.FromObject(j).CanTruncatedIntFitInInt64())) {
 Assert.fail();
 }
          CBORTestCommon.AssertSer(
            CBORObject.FromObject(j),
            TestCommon.LongToString(j));
          Assert.assertEquals(
            CBORObject.FromObject(j),
            CBORObject.FromObject(EInteger.FromInt64(j)));
          CBORObject obj = CBORObject.FromJSONString(
            "[" + TestCommon.LongToString(j) + "]");
          CBORTestCommon.AssertSer(
            obj,
            "[" + TestCommon.LongToString(j) + "]");
          if (j == ranges[i + 1]) {
            break;
          }
          ++j;
        }
      }
    }

    @Test
    public void TestMap() {
      CBORObject cbor = CBORObject.FromJSONString("{\"a\":2,\"b\":4}");
      Assert.assertEquals(2, cbor.size());
      TestCommon.AssertEqualsHashCode(
        CBORObject.FromObject(2),
        cbor.get(CBORObject.FromObject("a")));
      TestCommon.AssertEqualsHashCode(
        CBORObject.FromObject(4),
        cbor.get(CBORObject.FromObject("b")));
      Assert.assertEquals(2, cbor.get(CBORObject.FromObject("a")).AsInt32());
      Assert.assertEquals(4, cbor.get(CBORObject.FromObject("b")).AsInt32());
      Assert.assertEquals(0, CBORObject.True.size());
      cbor = CBORObject.DecodeFromBytes(new byte[] { (byte)0xbf, 0x61, 0x61, 2,
                    0x61, 0x62, 4, (byte)0xff });
      Assert.assertEquals(2, cbor.size());
      TestCommon.AssertEqualsHashCode(
        CBORObject.FromObject(2),
        cbor.get(CBORObject.FromObject("a")));
      TestCommon.AssertEqualsHashCode(
        CBORObject.FromObject(4),
        cbor.get(CBORObject.FromObject("b")));
      Assert.assertEquals(2, cbor.get(CBORObject.FromObject("a")).AsInt32());
      Assert.assertEquals(4, cbor.get(CBORObject.FromObject("b")).AsInt32());
    }

    @Test
    public void TestMapInMap() {
      CBORObject oo;
      oo = CBORObject.NewArray().Add(CBORObject.NewMap()
                    .Add(
              new ERational(EInteger.FromInt32(1), EInteger.FromString("2")),
              3).Add(4, false)).Add(true);
      CBORTestCommon.AssertRoundTrip(oo);
      oo = CBORObject.NewArray();
      oo.Add(CBORObject.FromObject(0));
      CBORObject oo2 = CBORObject.NewMap();
      oo2.Add(CBORObject.FromObject(1), CBORObject.FromObject(1368));
      CBORObject oo3 = CBORObject.NewMap();
      oo3.Add(CBORObject.FromObject(2), CBORObject.FromObject(1625));
      CBORObject oo4 = CBORObject.NewMap();
      oo4.Add(oo2, CBORObject.True);
      oo4.Add(oo3, CBORObject.True);
      oo.Add(oo4);
      CBORTestCommon.AssertRoundTrip(oo);
    }

    @Test
    public void TestParseDecimalStrings() {
      RandomGenerator rand = new RandomGenerator();
      for (int i = 0; i < 3000; ++i) {
        String r = RandomObjects.RandomDecimalString(rand);
        TestDecimalString(r);
      }
    }

    @Test(timeout = 50000)
    public void TestRandomData() {
      RandomGenerator rand = new RandomGenerator();
      CBORObject obj;
      for (int i = 0; i < 1000; ++i) {
        obj = CBORTestCommon.RandomCBORObject(rand);
        CBORTestCommon.AssertRoundTrip(obj);
        TestWriteToJSON(obj);
      }
    }

    @Test(timeout = 50000)
    public void TestRandomNonsense() {
      RandomGenerator rand = new RandomGenerator();
      for (int i = 0; i < 200; ++i) {
        byte[] array = new byte[rand.UniformInt(1000000) + 1];
        for (int j = 0; j < array.length; ++j) {
          if (j + 3 <= array.length) {
            int r = rand.UniformInt(0x1000000);
            array[j] = (byte)(r & 0xff);
            array[j + 1] = (byte)((r >> 8) & 0xff);
            array[j + 2] = (byte)((r >> 16) & 0xff);
            j += 2;
          } else {
            array[j] = (byte)rand.UniformInt(256);
          }
        }
        {
java.io.ByteArrayInputStream ms = null;
try {
ms = new java.io.ByteArrayInputStream(array);
int startingAvailable = ms.available();

          int iobj = 0;
          while (iobj < 25 && (startingAvailable-ms.available()) != startingAvailable) {
            ++iobj;
            try {
              CBORObject o = CBORObject.Read(ms);
              try {
                if (o == null) {
                  Assert.fail("Object read is null");
                } else {
                  CBORObject.DecodeFromBytes(o.EncodeToBytes());
                }
              } catch (Exception ex) {
                Assert.fail(ex.toString());
                throw new IllegalStateException("", ex);
              }
              String jsonString = "";
              try {
                if (o.getType() == CBORType.Array || o.getType() == CBORType.Map) {
                  jsonString = o.ToJSONString();
                  CBORObject.FromJSONString(jsonString);
                  TestWriteToJSON(o);
                }
              } catch (Exception ex) {
                Assert.fail(jsonString + "\n" + ex);
                throw new IllegalStateException("", ex);
              }
            } catch (CBORException ex) {
              new Object();  // Expected exception
            }
          }
}
finally {
try { if (ms != null) {
 ms.close();
 } } catch (java.io.IOException ex) {}
}
}
      }
    }

    @Test(timeout = 20000)
    public void TestRandomSlightlyModified() {
      RandomGenerator rand = new RandomGenerator();
      // Test slightly modified objects
      for (int i = 0; i < 200; ++i) {
        CBORObject originalObject = CBORTestCommon.RandomCBORObject(rand);
        byte[] array = originalObject.EncodeToBytes();
        // System.out.println(originalObject);
        int count2 = rand.UniformInt(10) + 1;
        for (int j = 0; j < count2; ++j) {
          int index = rand.UniformInt(array.length);
          array[index] = ((byte)rand.UniformInt(256));
        }
        {
java.io.ByteArrayInputStream inputStream = null;
try {
inputStream = new java.io.ByteArrayInputStream(array);
int startingAvailable = inputStream.available();

          while ((startingAvailable-inputStream.available()) != startingAvailable) {
            try {
              CBORObject o = CBORObject.Read(inputStream);
              byte[] encodedBytes = (o == null) ? null : o.EncodeToBytes();
              try {
                CBORObject.DecodeFromBytes(encodedBytes);
              } catch (Exception ex) {
                Assert.fail(ex.toString());
                throw new IllegalStateException("", ex);
              }
              String jsonString = "";
              try {
                if (o == null) {
                  Assert.fail("Object is null");
                }
                if (o != null && (o.getType() == CBORType.Array || o.getType() ==
                    CBORType.Map)) {
                  jsonString = o.ToJSONString();
                  // reread JSON String to test validity
                  CBORObject.FromJSONString(jsonString);
                  TestWriteToJSON(o);
                }
              } catch (Exception ex) {
                Assert.fail(jsonString + "\n" + ex);
                throw new IllegalStateException("", ex);
              }
            } catch (CBORException ex) {
              // Expected exception
              System.out.println(ex.getMessage());
            }
          }
}
finally {
try { if (inputStream != null) {
 inputStream.close();
 } } catch (java.io.IOException ex) {}
}
}
      }
    }

    @Test
    public void TestReadWriteInt() {
      RandomGenerator r = new RandomGenerator();
      try {
        for (int i = 0; i < 100000; ++i) {
          int val = ((int)RandomObjects.RandomInt64(r));
          {
            java.io.ByteArrayOutputStream ms = null;
try {
ms = new java.io.ByteArrayOutputStream();

              MiniCBOR.WriteInt32(val, ms);
              {
java.io.ByteArrayInputStream ms2 = null;
try {
ms2 = new java.io.ByteArrayInputStream(ms.toByteArray());

                Assert.assertEquals(val, MiniCBOR.ReadInt32(ms2));
}
finally {
try { if (ms2 != null) {
 ms2.close();
 } } catch (java.io.IOException ex) {}
}
}
}
finally {
try { if (ms != null) {
 ms.close();
 } } catch (java.io.IOException ex) {}
}
          }
          {
            java.io.ByteArrayOutputStream ms = null;
try {
ms = new java.io.ByteArrayOutputStream();

              CBORObject.Write(val, ms);
              {
java.io.ByteArrayInputStream ms2 = null;
try {
ms2 = new java.io.ByteArrayInputStream(ms.toByteArray());

                Assert.assertEquals(val, MiniCBOR.ReadInt32(ms2));
}
finally {
try { if (ms2 != null) {
 ms2.close();
 } } catch (java.io.IOException ex) {}
}
}
}
finally {
try { if (ms != null) {
 ms.close();
 } } catch (java.io.IOException ex) {}
}
          }
        }
      } catch (IOException ioex) {
        Assert.fail(ioex.getMessage());
      }
    }

    @Test
    public void TestShort() {
      for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; ++i) {
        CBORTestCommon.AssertSer(
          CBORObject.FromObject((short)i),
          TestCommon.IntToString(i));
      }
    }

    @Test
    public void TestSimpleValues() {
      CBORTestCommon.AssertSer(
        CBORObject.FromObject(true),
        "true");
      CBORTestCommon.AssertSer(
        CBORObject.FromObject(false),
        "false");
      CBORTestCommon.AssertSer(
        CBORObject.FromObject((Object)null),
        "null");
    }

    @Test
    public void TestSubtract() {
      RandomGenerator r = new RandomGenerator();
      for (int i = 0; i < 3000; ++i) {
        CBORObject o1 = CBORTestCommon.RandomNumber(r);
        CBORObject o2 = CBORTestCommon.RandomNumber(r);
        EDecimal cmpDecFrac = AsED(o1).Subtract(AsED(o2));
        EDecimal cmpCobj = AsED(CBORObject.Subtract(o1, o2));
        TestCommon.CompareTestEqual(cmpDecFrac, cmpCobj);
        CBORTestCommon.AssertRoundTrip(o1);
        CBORTestCommon.AssertRoundTrip(o2);
      }
    }

    @Test
    public void TestTaggedUntagged() {
      for (int i = 200; i < 1000; ++i) {
        if (i == 264 || i == 265 || i + 1 == 264 || i + 1 == 265) {
          // Skip since they're being used as
          // arbitrary-precision numbers
          continue;
        }
        CBORObject o, o2;
        o = CBORObject.FromObject(0);
        o2 = CBORObject.FromObjectAndTag(o, i);
        TestCommon.AssertEqualsHashCode(o, o2);
        o = CBORObject.FromObjectAndTag(o, i + 1);
        TestCommon.AssertEqualsHashCode(o, o2);
        o =
  CBORObject.FromObject(EInteger.FromString(
  "999999999999999999999999999999999"));
        o2 = CBORObject.FromObjectAndTag(o, i);
        TestCommon.AssertEqualsHashCode(o, o2);
        o = CBORObject.FromObjectAndTag(o, i + 1);
        TestCommon.AssertEqualsHashCode(o, o2);
        o = CBORObject.FromObject(new byte[] { 1, 2, 3 });
        o2 = CBORObject.FromObjectAndTag(o, i);
        TestCommon.AssertEqualsHashCode(o, o2);
        o = CBORObject.FromObjectAndTag(o, i + 1);
        TestCommon.AssertEqualsHashCode(o, o2);
        o = CBORObject.NewArray();
        o.Add(CBORObject.FromObject(0));
        o.Add(CBORObject.FromObject(1));
        o.Add(CBORObject.FromObject(2));
        o2 = CBORObject.FromObjectAndTag(o, i);
        TestCommon.AssertEqualsHashCode(o, o2);
        o = CBORObject.FromObjectAndTag(o, i + 1);
        TestCommon.AssertEqualsHashCode(o, o2);
        o = CBORObject.NewMap();
        o.Add("a", CBORObject.FromObject(0));
        o.Add("b", CBORObject.FromObject(1));
        o.Add("c", CBORObject.FromObject(2));
        o2 = CBORObject.FromObjectAndTag(o, i);
        TestCommon.AssertEqualsHashCode(o, o2);
        o = CBORObject.FromObjectAndTag(o, i + 1);
        TestCommon.AssertEqualsHashCode(o, o2);
        o = CBORObject.FromObject("a");
        o2 = CBORObject.FromObjectAndTag(o, i);
        TestCommon.AssertEqualsHashCode(o, o2);
        o = CBORObject.FromObjectAndTag(o, i + 1);
        TestCommon.AssertEqualsHashCode(o, o2);
        o = CBORObject.False;
        o2 = CBORObject.FromObjectAndTag(o, i);
        TestCommon.AssertEqualsHashCode(o, o2);
        o = CBORObject.FromObjectAndTag(o, i + 1);
        TestCommon.AssertEqualsHashCode(o, o2);
        o = CBORObject.True;
        o2 = CBORObject.FromObjectAndTag(o, i);
        TestCommon.AssertEqualsHashCode(o, o2);
        o = CBORObject.FromObjectAndTag(o, i + 1);
        TestCommon.AssertEqualsHashCode(o, o2);
        o = CBORObject.Null;
        o2 = CBORObject.FromObjectAndTag(o, i);
        TestCommon.AssertEqualsHashCode(o, o2);
        o = CBORObject.FromObjectAndTag(o, i + 1);
        TestCommon.AssertEqualsHashCode(o, o2);
        o = CBORObject.Undefined;
        o2 = CBORObject.FromObjectAndTag(o, i);
        TestCommon.AssertEqualsHashCode(o, o2);
        o = CBORObject.FromObjectAndTag(o, i + 1);
        TestCommon.AssertEqualsHashCode(o, o2);
      }
    }

    @Test
    public void TestTags() {
      EInteger maxuint = EInteger.FromString("18446744073709551615");
      EInteger[] ranges = {
        EInteger.FromString("37"),
        EInteger.FromString("65539"),
       EInteger.FromString("2147483147"),
  EInteger.FromString("2147484147"),
  EInteger.FromString("9223372036854775307"),
  EInteger.FromString("9223372036854776307"),
  EInteger.FromString("18446744073709551115"),
  EInteger.FromString("18446744073709551615") };
      if (CBORObject.True.isTagged()) {
 Assert.fail();
 }
      CBORObject trueObj = CBORObject.True;
      Assert.assertEquals(
        EInteger.FromString("-1"),
        trueObj.getMostInnerTag());
      EInteger[] tagstmp = CBORObject.True.GetAllTags();
      Assert.assertEquals(0, tagstmp.length);
      for (int i = 0; i < ranges.length; i += 2) {
        EInteger bigintTemp = ranges[i];
        while (true) {
          EInteger ei = bigintTemp;
          EInteger bigintNext = ei.Add(EInteger.FromInt32(1));
          if (bigintTemp.GetSignedBitLength() <= 31) {
            int bc = ei.ToInt32Checked();
            if (bc >= -1 && bc <= 37) {
              bigintTemp = bigintNext;
              continue;
            }
            if (bc == 264 || bc == 265) {
              bigintTemp = bigintNext;
              continue;
            }
          }
          CBORObject obj = CBORObject.FromObjectAndTag(0, bigintTemp);
          if (!(obj.isTagged())) {
 Assert.fail("obj not tagged");
 }
          EInteger[] tags = obj.GetAllTags();
          Assert.assertEquals(1, tags.length);
          Assert.assertEquals(bigintTemp, tags[0]);
          if (!obj.getMostInnerTag().equals(bigintTemp)) {
            String errmsg = "obj tag doesn't match: " + obj;
            Assert.assertEquals(errmsg, bigintTemp, obj.getMostInnerTag());
          }
          CBORTestCommon.AssertSer(
            obj,
            bigintTemp.toString() + "(0)");
          if (!bigintTemp.equals(maxuint)) {
            EInteger bigintNew = bigintNext;
            if (bigintNew.equals(EInteger.FromString("264")) ||
                bigintNew.equals(EInteger.FromString("265"))) {
              bigintTemp = bigintNext;
              continue;
            }
            // Test multiple tags
            CBORObject obj2 = CBORObject.FromObjectAndTag(obj, bigintNew);
            EInteger[] bi = obj2.GetAllTags();
            if (bi.length != 2) {
              {
                String stringTemp = "Expected 2 tags: " + obj2;
                Assert.assertEquals(stringTemp, 2, bi.length);
              }
            }
            bigintNew = bigintNext;
            TestCommon.CompareTestEqualAndConsistent(
  bi[0],
  bigintNew,
  "Outer tag doesn't match");
            TestCommon.CompareTestEqualAndConsistent(
  bi[1],
  bigintTemp,
  "Inner tag doesn't match");
            if (!obj2.getMostInnerTag().equals((Object)bigintTemp)) {
              {
                String stringTemp = "Innermost tag doesn't match: " + obj2;
                Assert.assertEquals(stringTemp, bigintTemp, obj2.getMostInnerTag());
              }
            }
            String str = bigintNext.toString() + "(" +
              bigintTemp.toString() + "(0))";
            CBORTestCommon.AssertSer(
              obj2,
              str);
          }
          if (bigintTemp.equals(ranges[i + 1])) {
            break;
          }
          bigintTemp = bigintNext;
        }
      }
    }

    @Test
    public void TestTags264And265() {
      CBORObject cbor;
      // Tag 264
      cbor = CBORObject.DecodeFromBytes(new byte[] { (byte)0xd9, 0x01, 0x08, (byte)0x82,
        (byte)0xc2, 0x42, 2, 2, (byte)0xc2, 0x42, 2, 2 });
      CBORTestCommon.AssertRoundTrip(cbor);
      // Tag 265
      cbor = CBORObject.DecodeFromBytes(new byte[] { (byte)0xd9, 0x01, 0x09, (byte)0x82,
        (byte)0xc2, 0x42, 2, 2, (byte)0xc2, 0x42, 2, 2 });
      CBORTestCommon.AssertRoundTrip(cbor);
    }
    @Test(expected = CBORException.class)
    public void TestTagThenBreak() {
      CBORTestCommon.FromBytesTestAB(new byte[] { (byte)0xd1, (byte)0xff });
    }

    @Test
    public void TestTextStringStream() {
      CBORObject cbor = CBORTestCommon.FromBytesTestAB(
        new byte[] { 0x7f, 0x61, 0x2e, 0x61, 0x2e, (byte)0xff });
      {
        String stringTemp = cbor.AsString();
        Assert.assertEquals(
        "..",
        stringTemp);
      }
      TestTextStringStreamOne(TestCommon.Repeat('x', 200000));
      TestTextStringStreamOne(TestCommon.Repeat('\u00e0', 200000));
      TestTextStringStreamOne(TestCommon.Repeat('\u3000', 200000));
      TestTextStringStreamOne(TestCommon.Repeat("\ud800\udc00", 200000));
    }
    @Test(expected = CBORException.class)
    public void TestTextStringStreamNoIndefiniteWithinDefinite() {
      CBORTestCommon.FromBytesTestAB(new byte[] { 0x7f, 0x61, 0x20, 0x7f, 0x61,
        0x20, (byte)0xff, (byte)0xff });
    }
    @Test(expected = CBORException.class)
    public void TestTextStringStreamNoTagsBeforeDefinite() {
      CBORTestCommon.FromBytesTestAB(new byte[] { 0x7f, 0x61, 0x20, (byte)0xc0, 0x61,
        0x20, (byte)0xff });
    }

    private static EDecimal AsED(CBORObject obj) {
      return EDecimal.FromString(
        obj.AsEDecimal().toString());
    }

    private static void AddSubCompare(CBORObject o1, CBORObject o2) {
      EDecimal cmpDecFrac = AsED(o1).Add(AsED(o2));
      EDecimal cmpCobj = AsED(CBORObject.Addition(o1, o2));
      TestCommon.CompareTestEqual(cmpDecFrac, cmpCobj);
      cmpDecFrac = AsED(o1).Subtract(AsED(o2));
      cmpCobj = AsED(CBORObject.Subtract(o1, o2));
      TestCommon.CompareTestEqual(cmpDecFrac, cmpCobj);
      CBORObjectTest.CompareDecimals(o1, o2);
    }

    private static void TestDecimalString(String r) {
      CBORObject o = CBORObject.FromObject(EDecimal.FromString(r));
      CBORObject o2 = CBORDataUtilities.ParseJSONNumber(r);
      TestCommon.CompareTestEqual(o, o2);
    }

    private static void TestTextStringStreamOne(String longString) {
      CBORObject cbor, cbor2;
      cbor = CBORObject.FromObject(longString);
      cbor2 = CBORTestCommon.FromBytesTestAB(cbor.EncodeToBytes());
      Assert.assertEquals(
        longString,
        CBORObject.DecodeFromBytes(cbor.EncodeToBytes()).AsString());
      {
        Object objectTemp = longString;
        Object objectTemp2 = CBORObject.DecodeFromBytes(cbor.EncodeToBytes(
                  CBOREncodeOptions.NoIndefLengthStrings)).AsString();
        Assert.assertEquals(objectTemp, objectTemp2);
      }
      TestCommon.AssertEqualsHashCode(cbor, cbor2);
      Assert.assertEquals(longString, cbor2.AsString());
    }

    private static void TestWriteToJSON(CBORObject obj) {
      CBORObject objA = null;
      String jsonString = "";
      java.io.ByteArrayOutputStream ms = null;
try {
ms = new java.io.ByteArrayOutputStream();

        try {
          obj.WriteJSONTo(ms);
          jsonString = DataUtilities.GetUtf8String(
            ms.toByteArray(),
            true);
          objA = CBORObject.FromJSONString(jsonString);
        } catch (CBORException ex) {
          throw new IllegalStateException(jsonString, ex);
        } catch (IOException ex) {
          throw new IllegalStateException("", ex);
        }
}
finally {
try { if (ms != null) {
 ms.close();
 } } catch (java.io.IOException ex) {}
}
      CBORObject objB = CBORObject.FromJSONString(obj.ToJSONString());
      if (!objA.equals(objB)) {
        System.out.println(objA);
        System.out.println(objB);
        Assert.fail("WriteJSONTo gives different results from ToJSONString");
      }
    }
  }
