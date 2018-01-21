package com.clt.script.exp.expressions;

import java.io.PrintWriter;
import java.util.Map;

import com.clt.script.debug.Debugger;
import com.clt.script.exp.EvaluationException;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;
import com.clt.script.exp.Value;
import com.clt.script.exp.types.TypeVariable;
import com.clt.script.exp.values.IntValue;
import com.clt.script.exp.values.RealValue;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class Cast extends Expression {

  private Expression exp;
  private Type type;


  public Cast(Expression exp, Type type) {

    this.exp = exp;
    this.type = type;
  }


  @Override
  public Expression copy(Map<?, ?> mapping) {

    return new Cast(this.exp.copy(mapping), this.type.copy());
  }


  @Override
  public Type getType() {

    Type t = this.exp.getType();
    try {
      Type.unify(t, this.type);
    } catch (Exception exn) {
      if (!(((t == Type.Int) && (this.type == Type.Real)) || ((t == Type.Real) && (this.type == Type.Int)))) {
        throw new TypeException("Cannot cast " + this.exp + " into type \""
          + this.type + "\"");
      }
    }
    return this.type;
  }


  @Override
  protected Value eval(Debugger dbg) {

    Value v = this.exp.evaluate(dbg);
    Type t = v.getType();

    if (t instanceof TypeVariable) {
      return v;
    }
    else if (Type.equals(v.getType(), this.type)) {
      return v;
    }
    else {
      try {
        Type.unify(t, this.type);
        return v;
      } catch (Exception exn) {
        if ((v instanceof IntValue) && (this.type == Type.Real)) {
          return new RealValue(((IntValue)v).getInt());
        }
        else if ((v instanceof RealValue) && (this.type == Type.Int)) {
          return new IntValue((long)((RealValue)v).getReal());
        }
        else {
          throw new EvaluationException("Illegal attempt to cast " + this.exp
            + " (type "
                          + v.getType() + ") to type " + this.type);
        }
      }
    }
  }


  @Override
  public int getPriority() {

    return 9;
  }


  @Override
  public void write(PrintWriter w) {

    w.print(this.type + "(");
    this.exp.write(w, this.exp.getPriority() < this.getPriority());
    w.print(")");
  }
}
