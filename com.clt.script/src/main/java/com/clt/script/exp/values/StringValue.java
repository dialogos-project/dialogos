/*
 * @(#)StringValue.java
 * Created on Thu Oct 02 2003
 *
 * Copyright (c) 2003 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.script.exp.values;

import com.clt.script.exp.Type;
import com.clt.script.exp.Value;

/**
 * A value representing a unicode character string.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public final class StringValue extends PrimitiveValue implements Comparable<StringValue>
{

	String value;

	public StringValue(String value)
	{

		if (value == null)
		{
			throw new IllegalArgumentException();
		}
		this.value = value;
	}

	/**
	 * Return the string represented by this value.
	 */
	public String getString()
	{

		return this.value;
	}

	@Override
	protected Value copyValue()
	{

		return new StringValue(this.value);
	}

	@Override
	public Type getType()
	{

		return Type.String;
	}

	@Override
	public boolean equals(Object v)
	{

		if (v == this)
		{
			return true;
		} else if (v instanceof StringValue)
		{
			return ((StringValue) v).getString().equals(this.getString());
		} else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{

		return this.value.hashCode();
	}

	public int compareTo(StringValue o)
	{

		return this.value.compareTo(o.value);
	}

	@Override
	public String toString()
	{

		return StringValue.toSourceString(this.value, false);
	}

	public static StringValue valueOf(String s)
	{

		return new StringValue(s);
	}

	public static String toSourceString(String s, boolean ascii)
	{

		StringBuilder b = new StringBuilder(s.length());
		b.append('\"');
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			switch (c)
			{
			case '\n':
				b.append("\\n");
				break;
			case '\r':
				b.append("\\r");
				break;
			case '\t':
				b.append("\\t");
				break;
			case '\f':
				b.append("\\f");
				break;
			case '\b':
				b.append("\\b");
				break;
			case '\"':
				b.append("\\\"");
				break;
			case '\\':
				b.append("\\\\");
				break;
			default:
				if (!ascii || ((c >= '\u0020') && (c < '\u0080')))
				{
					b.append(c);
				} else
				{
					String esc = Integer.toHexString(c);
					b.append("\\u");
					for (int j = esc.length(); j < 4; j++)
					{
						b.append('0');
					}
					b.append(esc);
				}
				break;
			}
		}
		b.append('\"');
		return b.toString();
	}

	@Override
	public Object getReadableValue()
	{
		return getString();
	}

}
