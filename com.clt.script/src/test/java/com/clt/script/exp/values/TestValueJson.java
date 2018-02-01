package com.clt.script.exp.values;

import com.clt.script.exp.Value;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by timo on 06.10.17.
 */
public class TestValueJson {
    @Test
    public void testSimpleJson() {
        StructValue sv = new StructValue();
        sv.add("intOne", new IntValue(1));
        sv.add("intNOne", new IntValue(-1));
        sv.add("intMax", new IntValue(Long.MAX_VALUE));
        sv.add("floatOne", new RealValue(1f));
        sv.add("floatMax", new RealValue(Double.MAX_VALUE));
        //sv.add("floatNInf", new RealValue(Double.NEGATIVE_INFINITY)); // this is a problem with JSON
        //sv.add("floatNAN", new RealValue(Double.NaN)); // this is a problem with JSON
        sv.add("string", new StringValue("abc"));
        sv.add("structEmpty", new StructValue());
        sv.add("boolTrue", new BoolValue(true));
        sv.add("boolFalse", new BoolValue(false));
        sv.add("undefined", new Undefined());
        sv.add("listEmpty", new ListValue(new Value[0]));
        sv.add("listFilled", new ListValue(new IntValue(0), new IntValue(1), new IntValue(2)));
        String s = sv.toJson();
        System.out.println(s);
        Value sv2 = Value.fromJson(s);
        System.out.println(sv2.toJson());
        Assert.assertEquals("ouch", sv, sv2);
    }
}