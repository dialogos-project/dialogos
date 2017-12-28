/*
 * @(#)StructValue.java
 * Created on Wed Oct 16 2002
 *
 * Copyright (c) 2002 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.script.exp.values;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.clt.script.exp.EvaluationException;
import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.types.StructType;

/**
 * A feature structure, consisting of a mapping from labels to values.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class StructValue extends Value implements Reference
{

	private Map<String, Value> slots = new HashMap<String, Value>();

	/**
	 * Construct a new empty structure.
	 */
	public StructValue()
	{

		this(new String[0], new Value[0]);
	}

	/**
	 * Construct a structure from arrays of labels and values.
	 */
	public StructValue(String[] names, Value[] values)
	{

		if (names == null)
		{
			throw new IllegalArgumentException("<null> labels");
		}
		if (values == null)
		{
			throw new IllegalArgumentException("<null> values");
		}

		this.init(names, values);
	}

	public StructValue(Map<String, ? extends Value> map)
	{

		if (map == null)
		{
			throw new IllegalArgumentException("<null> map");
		}

		String labels[] = map.keySet().toArray(new String[map.size()]);
		Value values[] = new Value[labels.length];
		for (int i = 0; i < labels.length; i++)
		{
			values[i] = map.get(labels[i]);
		}

		this.init(labels, values);
	}

	protected void init(String[] names, Value[] values)
	{

		this.slots.clear();

		if (names.length != values.length)
		{
			throw new IllegalArgumentException("Number of labels does not match number of elements");
		}

		for (int i = 0; i < names.length; i++)
		{
			this.add(names[i], values[i]);
		}
	}

	protected void add(String label, Value value)
	{

		this.slots.put(label, value);
	}

	@Override
	protected Value copyValue()
	{

		String labels[] = new String[this.slots.size()];
		Value values[] = new Value[this.slots.size()];
		int i = 0;
		for (Iterator<String> it = this.labels(); it.hasNext(); i++)
		{
			labels[i] = it.next();
			values[i] = this.getValue(labels[i]).copy();
		}
		return new StructValue(labels, values);
	}

	@Override
	public Type getType()
	{

		String labels[] = new String[this.slots.size()];
		Type types[] = new Type[this.slots.size()];

		int i = 0;
		for (Iterator<String> it = this.slots.keySet().iterator(); it.hasNext(); i++)
		{
			labels[i] = it.next();
			types[i] = this.getValue(labels[i]).getType();
		}

		return new StructType(labels, types, false);
	}

	/**
	 * Get an iterator over all labels of this structure
	 */
	public Iterator<String> labels()
	{

		return this.slots.keySet().iterator();
	}

	public Set<String> getLabels()
	{

		return Collections.unmodifiableSet(this.slots.keySet());
	}

	/**
	 * Return the number of keys in theis structure
	 */
	public int size()
	{

		return this.slots.size();
	}

	/**
	 * Check whether this structure contains a certain label.
	 */
	public boolean containsLabel(String label)
	{

		return this.slots.containsKey(label);
	}

	/**
	 * Get the value for label <code>name</code>
	 */
	public Value getValue(String name)
	{

		Value v = this.slots.get(name);
		if (v == null)
		{
			throw new EvaluationException("Struct does not contain element " + name);
		} else
		{
			return v;
		}
	}

	/**
	 * Set the value for label <code>name</code>
	 */
	public void setValue(String name, Value v)
	{

		if (!this.slots.containsKey(name))
		{
			throw new EvaluationException("Struct does not contain element " + name);
		} else
		{
			this.slots.put(name, v);
		}
	}

	/**
	 * Structures are equal, if they contain the same labels and those labels
	 * are each mapped to equivalent values.
	 */
	@Override
	public boolean equals(Object v)
	{

		if (v == this)
		{
			return true;
		} else if (v instanceof StructValue)
		{
			StructValue s = (StructValue) v;
			if (this.slots.size() != s.slots.size())
			{
				return false;
			}
			for (Iterator<String> it = this.slots.keySet().iterator(); it.hasNext();)
			{
				String label = it.next();
				if (!s.slots.containsKey(label))
				{
					return false;
				} else if (!this.getValue(label).equals(s.getValue(label)))
				{
					return false;
				}
			}
			return true;
		} else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{

		int hash = 0;
		for (Iterator<String> it = this.labels(); it.hasNext();)
		{
			String label = it.next();
			hash = hash ^ label.hashCode();
			hash = hash ^ this.getValue(label).hashCode();
		}
		return hash;
	}

	@Override
	public String toString()
	{

		Set<String> labels = new TreeSet<String>(this.slots.keySet());

		StringBuilder b = new StringBuilder();
		b.append("{ ");
		for (Iterator<String> it = labels.iterator(); it.hasNext();)
		{
			String label = it.next();
			Value value = this.getValue(label);
			b.append(label);
			b.append(" = ");
			b.append(value.toString());
			if (it.hasNext())
			{
				b.append(", ");
			}
		}
		b.append(" }");
		return b.toString();
	}

	/**
	 * Merge structures <code>s1</code> and <code>s2</code>.
	 * <p>
	 * The source structures will not be modified. Instead a new StructValue
	 * will be created and returned. If <code>s1</code> and <code>s2</code> both
	 * contain the same label, the result depends on the type of its subvalues.
	 * If the label is mapped to a substructure in both <code>s1</code> and
	 * <code>s2</code> then these substructures are merged recursively.
	 * Otherwise the value from <code>s2</code> is taken.
	 * </p>
	 * Examples:<br>
	 * <code>merge({ x=3, y=4 }, { x=4, z=5 })</code> =
	 * <code>{ x=4, y=4, z=5 }</code><br>
	 * <code>merge({ x={x=3}, y={a=4} }, { x={y=4}, y=5 })</code> =
	 * <code>{ x={x=3, y=4}, y=5 }</code>
	 */
	public static StructValue merge(StructValue s1, StructValue s2)
	{

		return StructValue.merge(s1, s2, false);
	}

	public static StructValue mergeDefined(StructValue s1, StructValue s2)
	{

		return StructValue.merge(s1, s2, true);
	}

	private static StructValue merge(StructValue s1, StructValue s2, boolean forceDefined)
	{

		Map<String, Value> m = new HashMap<String, Value>();
		for (Iterator<String> it = s1.labels(); it.hasNext();)
		{
			String label = it.next();
			m.put(label, s1.getValue(label).copy());
		}
		for (Iterator<String> it = s2.labels(); it.hasNext();)
		{
			String label = it.next();
			Value v = s2.getValue(label);
			if (m.containsKey(label))
			{
				if ((m.get(label) instanceof StructValue) && (v instanceof StructValue))
				{
					m.put(label, StructValue.merge((StructValue) m.get(label), (StructValue) v, forceDefined));
				} else
				{
					if ((v instanceof Undefined) && forceDefined)
					{
						m.put(label, m.get(label));
					} else
					{
						m.put(label, v.copy());
					}
				}
			} else
			{
				m.put(label, v.copy());
			}
		}

		String[] labels = new String[m.size()];
		Value[] values = new Value[m.size()];
		int i = 0;
		for (Iterator<String> it = m.keySet().iterator(); it.hasNext(); i++)
		{
			labels[i] = it.next();
			values[i] = m.get(labels[i]);
		}
		return new StructValue(labels, values);
	}

	@Override
	public void prettyPrint(PrintWriter w, int inset)
	{

		if (this.size() == 0)
		{
			w.print("{ }");
		} else
		{
			w.println("{");
			for (Iterator<String> it = this.labels(); it.hasNext();)
			{
				String label = it.next();
				for (int i = 0; i < inset + 2; i++)
				{
					w.print(' ');
				}
				w.print(label);
				w.print(" = ");
				this.getValue(label).prettyPrint(w, inset + 2);
				if (it.hasNext())
				{
					w.println(",");
				} else
				{
					w.println();
				}
			}
			for (int i = 0; i < inset; i++)
			{
				w.print(' ');
			}
			w.print("}");
		}
		this.printAttributes(w);
	}

	@Override
	public Object getReadableValue()
	{
		Map<String, Object> newMap = new HashMap<String, Object>();
		for (Map.Entry<String, Value> entry : slots.entrySet())
		{
			Object value = entry.getValue().getReadableValue();
			newMap.put(entry.getKey(), value);			
		}
		return newMap;
	}
}
