/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.clt.diamant

import com.clt.script.exp.Type;
import com.clt.script.exp.values.*;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;


/**
 *
 * @author koller
 */
class AbstractVariableTest {
    @Test
    public void testStringSlot() throws AbstractVariable.VariableParsingException {
        Slot x = new Slot("name", Type.String, "undefined", true);
        x.setValue(new StringValue("hallo"));      
        testEncodeDecode(x);
    }
    
    @Test
    public void testIntSlot() throws AbstractVariable.VariableParsingException {
        Slot x = new Slot("name", Type.Int, "undefined", true);
        x.setValue(new IntValue(27));
        testEncodeDecode(x);
    }
    
    @Test
    public void testListSlot() throws AbstractVariable.VariableParsingException {
        Slot x = new Slot("name", Type.getTypeForName("list"), "undefined", true);
        ListValue v = new ListValue(new StringValue("hallo"), new IntValue(27));
        x.setValue(v);
        testEncodeDecode(x);
    }
    
    @Test
    public void testStructSlot() throws AbstractVariable.VariableParsingException {
        Slot x = new Slot("name", Type.getTypeForName("struct"), "undefined", true);
        Map vv = [number: new IntValue(27), string: new StringValue("hallo")];
        StructValue v = new StructValue(vv);
        x.setValue(v);
        testEncodeDecode(x);
    }
    
    @Test
    public void testDeepStructSlot() throws AbstractVariable.VariableParsingException {
        Slot x = new Slot("name", Type.getTypeForName("struct"), "undefined", true);
        Map vvv = [foo: new ListValue(new StringValue("hallo"), new IntValue(27))]
        Map vv = [number: new IntValue(27), string: new StringValue("hallo"), stru: new StructValue(vvv)];
        StructValue v = new StructValue(vv);
        x.setValue(v);
        testEncodeDecode(x);
    }
    
    @Test
    public void testStringGv() throws AbstractVariable.VariableParsingException {
        GroovyVariable x = new GroovyVariable("name", "hallo", true);
        testEncodeDecode(x);
    }
    
    @Test
    public void testIntGv() throws AbstractVariable.VariableParsingException {
        GroovyVariable x = new GroovyVariable("name", new Integer(27), true);
        testEncodeDecode(x);
    }
    
    @Test
    public void testStringListGv() throws AbstractVariable.VariableParsingException {
        GroovyVariable x = new GroovyVariable("name", ["a", "b", "c"], true);
        testEncodeDecode(x);
    }
    
    // NB int list ([1,2,3]) would _not_ work, because Gson decodes it into [1.0, 2.0, 3.0]
    
    public static class CustomClass {
        private int number;
        private String string;
        
        public CustomClass(int number, String string) {
            this.number = number;
            this.string = string;
        }
        
        public boolean equals(Object other) {
            if (this.is(other)) {
                return true;
            }
            
            if (other == null) {
                return false;
            }
            
            if (getClass() != other.getClass()) {
                return false;
            }
            
            final CustomClass o = (CustomClass) other;
            if( this.number != other.number ) {
                return false;
            }
            if( this.string != other.string ) {
                return false;
            }
            
            return true;
        }
    }
    
    @Test
    public void testCustomClassGv() throws AbstractVariable.VariableParsingException {
        CustomClass value = new CustomClass(27, "hallo")
        GroovyVariable x = new GroovyVariable("name", value, true);
        testEncodeDecode(x);
    }
    
    private void testEncodeDecode(AbstractVariable x) throws AbstractVariable.VariableParsingException {
        String encoded = x.toJson();
        AbstractVariable decoded = AbstractVariable.fromJson(encoded);
        
        assert x == decoded : "expected " + x.toDetailedString() + ", but got " + decoded.toDetailedString()
    }
}

