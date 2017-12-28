/*
 * @(#)IntValue.java
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
 * A 64 bit integer value in the range +/- 9,2233720368549e+18 (exactly
 * -0x8000000000000000 to 0x7fffffffffffffff).
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public final class IntValue extends PrimitiveValue implements Comparable<IntValue>
{

	long value;

	public IntValue(long value)
	{

		this.value = value;
	}

	/**
	 * Return the native value of this IntValue as a long.
	 */
	public long getInt()
	{

		return this.value;
	}

	@Override
	protected Value copyValue()
	{

		return new IntValue(this.value);
	}

	@Override
	public boolean equals(Object v)
	{

		if (v instanceof IntValue)
		{
			return ((IntValue) v).getInt() == this.getInt();
		} else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{

		return (int) (this.value ^ (this.value >> 32));
	}

	public int compareTo(IntValue o)
	{

		long v = o.value;
		if (v == this.value)
		{
			return 0;
		} else if (v < this.value)
		{
			return -1;
		} else
		{
			return 1;
		}
	}

	@Override
	public Type getType()
	{

		return Type.Int;
	}

	@Override
	public String toString()
	{

		return String.valueOf(this.value);
	}

	public static IntValue valueOf(String s)
	{

		return new IntValue(Long.parseLong(s));
	}

	@Override
	public Object getReadableValue()
	{
		return getInt();
	}
}
