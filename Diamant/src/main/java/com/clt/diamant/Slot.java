package com.clt.diamant;

import java.util.Stack;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.clt.script.Environment;
import com.clt.script.debug.Debugger;
import com.clt.script.exp.EvaluationException;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;
import com.clt.script.exp.Value;
import com.clt.script.exp.types.ListType;
import com.clt.script.exp.types.StructType;
import com.clt.script.exp.values.Undefined;
import com.clt.util.StringTools;
import com.clt.xml.XMLWriter;

/**
 * A class to save variables. 
 * 
 * @author tillk
 */
public class Slot extends AbstractVariable
{

	public static final Type[] supportedTypes =
	{ Type.Bool, Type.Int, Type.Real, Type.String, new ListType(), new StructType() };

	private Type _type;
	private String initValue;

	private Stack<Value> instances;

	public Slot()
	{
		this(Resources.getString("UntitledIdentifier"), Type.String, "undefined", false);
	}

	public Slot(String name, Type type, String initValue, boolean export)
	{
		super(name, export);
		this._type = type;
		this.initValue = initValue;

		this.instances = new Stack<Value>();

		this.instantiate(Value.of(initValue));
	}

	@Override
	public Type getType()
	{
		return _type;
	}
	
	/**
	 * Sets the type of the variable.
	 * 
	 * @param type Type of the variable
	 */
	public void setType(Type type)
	{
		_type = type;
	}

	public Slot clone(Mapping map)
	{
		Slot v = new Slot(this._name, this._type, this.getValue().toString(), this._export);
		map.addVariable(this, v);
		return v;
	}

	@Override
	public Slot clone()
	{
		Slot v = new Slot(this._name, this._type, this.getValue().toString(), this._export);
		return v;
	}

	public static Type legacyType(String t)
	{

		if (t.equals("java.lang.Boolean"))
		{
			return Type.Bool;
		} else if (t.equals("java.lang.Long"))
		{
			return Type.Int;
		} else if (t.equals("java.lang.Double"))
		{
			return Type.Real;
		} else if (t.equals("java.lang.String"))
		{
			return Type.String;
		} else if (t.equals("list"))
		{
			return new ListType();
		} else if (t.equals("struct"))
		{
			return new StructType();
		} else
		{
			throw new TypeException("Unexpected type: " + t);
		}
	}

	public void instantiate(Environment env, Debugger dbg)
	{
		try
		{
			if (StringTools.isEmpty(this.initValue))
			{
				this.instantiate(new Undefined());
			} else
			{
				this.instantiate(Expression.parseExpression(this.initValue, env).evaluate(dbg));
			}
		} catch (Exception exn)
		{
			throw new EvaluationException("Illegal expression: " + this.initValue);
		}
	}

	public void instantiate(Value v)
	{
		this.instances.push(v);
	}

	public void uninstantiate()
	{
		this.instances.pop();
	}

	@Override
	public void setValue(Object value)
	{
		if (value instanceof Value)
		{
			setValue((Value) value);
		}
	}
	
	private void setValue(Value value)
	{
		if (this.instances.empty())
		{
			throw new EvaluationException("Slot not instantiated");
		} else
		{
			if (!(value instanceof Undefined) && !this.getType().getObjectClass().isAssignableFrom(value.getClass()))
			{
				throw new EvaluationException("Attempt to assign a value of type " + value.getClass().getName()
						+ " to variable " + this.getName() + " of type " + this.getType() + ".");
			}
			this.instances.pop();
			this.instances.push(value);
			for (ChangeListener l : _listeners)
			{
				l.stateChanged(new ChangeEvent(this));
			}
		}
	}

	@Override
	public Value getValue()
	{
		if (this.instances.empty())
		{
			return new Undefined();
		} else
		{
			return this.instances.peek();
		}
	}

	public void setInitValue(String initValue)
	{
		this.initValue = initValue;
		this.setType(_type);
	}

	public String getInitValue()
	{
		return this.initValue;
	}

	@Override
	public void write(XMLWriter out, IdMap uid_map, String tag)
	{
		out.openElement(tag, new String[]
		{ "uid" }, new Object[]
		{ uid_map.variables.put(this) });

		out.printElement("name", this.getName());

		String typename;
		if (this.getType() instanceof ListType)
		{
			typename = "list";
		} else if (this.getType() instanceof StructType)
		{
			typename = "struct";
		} else
		{
			typename = this.getType().toString();
		}

		out.printElement("type", typename);
		out.printElement("value", this.initValue);
		if (this._export)
		{
			out.printElement("export", null);
		}

		out.closeElement(tag);
	}
}