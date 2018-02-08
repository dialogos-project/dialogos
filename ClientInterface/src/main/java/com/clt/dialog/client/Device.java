package com.clt.dialog.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;

import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.values.BoolValue;
import com.clt.script.exp.values.IntValue;
import com.clt.script.exp.values.ListValue;
import com.clt.script.exp.values.PrimitiveValue;
import com.clt.script.exp.values.RealValue;
import com.clt.script.exp.values.StringValue;
import com.clt.script.exp.values.StructValue;
import com.clt.script.exp.values.Undefined;
import com.clt.xml.AbstractHandler;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 7.0
 */

abstract class Device {

  /**
   * Indicate the interface version.
   * 
   * Change this when the XML interface changes so that it is no longer
   * compatible with older versions.
   */
  protected static final String VERSION = "v7.0";

  static final boolean DEBUG_INTERFACE = false;

  private static Map<Class<?>, TypeID> type2id;
  private static Map<String, Class<?>> name2type;

  private final Object receiveLock = new Object();

  private enum TypeID {
        NULL, STRUCT, LIST, INT, REAL, BOOL, STRING, UNDEF
  }

  private static final String[] typenames =
    { "null", "struct", "list", "int", "real", "bool",
            "string", "undefined" };

  static {
    Device.type2id = new HashMap<Class<?>, TypeID>();
    Device.name2type = new HashMap<String, Class<?>>();

    Device.registerType(IntValue.class, TypeID.INT);
    Device.registerType(RealValue.class, TypeID.REAL);
    Device.registerType(BoolValue.class, TypeID.BOOL);
    Device.registerType(StringValue.class, TypeID.STRING);
    Device.registerType(StructValue.class, TypeID.STRUCT);
    Device.registerType(ListValue.class, TypeID.LIST);
    Device.registerType(Undefined.class, TypeID.UNDEF);

    Device.type2id.put(Boolean.class, TypeID.BOOL);
    Device.type2id.put(Byte.class, TypeID.INT);
    Device.type2id.put(Short.class, TypeID.INT);
    Device.type2id.put(Integer.class, TypeID.INT);
    Device.type2id.put(Long.class, TypeID.INT);
    Device.type2id.put(Float.class, TypeID.REAL);
    Device.type2id.put(Double.class, TypeID.REAL);
    Device.type2id.put(String.class, TypeID.STRING);
  }


  private static void registerType(Class<?> c, TypeID type) {

    Device.type2id.put(c, type);
    Device.name2type.put(Device.typenames[type.ordinal()], c);
  }


  private static String getTypeName(Class<?> c) {

    TypeID type = Device.type2id.get(c);
    if (type != null) {
      return Device.typenames[type.ordinal()];
    }
    else if (Map.class.isAssignableFrom(c)) {
      return Device.typenames[TypeID.STRUCT.ordinal()];
    }
    else if (Collection.class.isAssignableFrom(c)) {
      return Device.typenames[TypeID.LIST.ordinal()];
    }
    else if (Iterator.class.isAssignableFrom(c)) {
      return Device.typenames[TypeID.LIST.ordinal()];
    }
    else if (Enumeration.class.isAssignableFrom(c)) {
      return Device.typenames[TypeID.LIST.ordinal()];
    }
    else if (c.isArray()) {
      return Device.typenames[TypeID.LIST.ordinal()];
    }
    else {
      return Device.typenames[TypeID.STRING.ordinal()];
    }
  }


  private static TypeID getTypeID(Class<?> c) {

    TypeID type = Device.type2id.get(c);
    if (type != null) {
      return type;
    }
    else if (Map.class.isAssignableFrom(c)) {
      return TypeID.STRUCT;
    }
    else if (Collection.class.isAssignableFrom(c)) {
      return TypeID.LIST;
    }
    else if (Iterator.class.isAssignableFrom(c)) {
      return TypeID.LIST;
    }
    else if (Enumeration.class.isAssignableFrom(c)) {
      return TypeID.LIST;
    }
    else if (c.isArray()) {
      return TypeID.LIST;
    }
    else {
      return TypeID.STRING;
    }
  }

  private ConnectionState state = ConnectionState.DISCONNECTED;
  private Collection<DeviceListener> deviceListeners =
    new ArrayList<DeviceListener>();


  static void debug(String message) {

    if (Device.DEBUG_INTERFACE) {
      System.out.println("DEBUG: " + message);
    }
  }


  protected void setState(ConnectionState state) {

    if (this.state != state) {
      this.state = state;
      DeviceEvent evt = new DeviceEvent(this, state);
      synchronized (this.deviceListeners) {
        for (DeviceListener listener : this.deviceListeners) {
          listener.stateChanged(evt);
        }
      }
    }
  }


  public ConnectionState getState() {

    return this.state;
  }


  public void addDeviceListener(DeviceListener l) {

    synchronized (this.deviceListeners) {
      this.deviceListeners.add(l);
    }
  }


  public void removeDeviceListener(DeviceListener l) {

    synchronized (this.deviceListeners) {
      this.deviceListeners.remove(l);
    }
  }


  public void fireDataLogged(String message) {

    synchronized (this.deviceListeners) {
      DeviceEvent evt = new DeviceEvent(this, this.getState(), message);
      for (DeviceListener listener : this.deviceListeners) {
        listener.dataLogged(evt);
      }
    }
  }


  protected abstract void error(Throwable t);


  protected void receive(XMLReader r, String type, String elem,
      final ValueReceiver ref) {

    this.receiveValue(r, type, elem, new ValueReceiver() {

      public void valueReceived(String name, Value value) {

        DeviceEvent evt =
          new DeviceEvent(Device.this, Device.this.getState(), value);
        synchronized (Device.this.deviceListeners) {
          for (DeviceListener listener : Device.this.deviceListeners) {
            listener.dataReceived(evt);
          }
        }
        ref.valueReceived(name, value);
      }
    });
  }


  private void receiveValue(final XMLReader r, final String type,
      final String elem,
            final ValueReceiver ref) {

    final Map<String, Value> elements = new HashMap<String, Value>();
    final Map<String, Value> attributes = new HashMap<String, Value>();

    r.setHandler(new AbstractHandler(elem == null ? "value" : "elem") {

      String prim_value = null;


      @Override
      protected void start(String name, Attributes atts) {

        Device.debug("<" + name + " " + Device.toString(atts) + ">");

        if (name.equals("elem")) {
          Device.this.receiveValue(r, atts.getValue("type"), atts
            .getValue("name"),
                        new ValueReceiver() {

                          public void valueReceived(String name, Value v) {

                            elements.put(name, v);
                          }
                        });
        }
        else if (name.equals("att")) {
          final String attrname = atts.getValue("name");
          final String attrtype = atts.getValue("type");
          r.setHandler(new AbstractHandler("att") {

            @Override
            public void end(String name) {

              if (Device.DEBUG_INTERFACE) {
                System.out.println("  " + this.getValue());
                System.out.println("</" + name + ">");
              }

              try {
                Class<?> c = Device.name2type.get(attrtype);
                if (c == null) {
                  Device.this
                    .error(new IllegalArgumentException(
                                      "Unexpected attribute type: " + attrtype));
                }
                else {
                  Object o;
                  if (c.equals(Undefined.class)) {
                    o = new Undefined();
                  }
                  else {
                    try {
                      o =
                        c.getMethod("valueOf", new Class[] { String.class })
                          .invoke(
                                                null,
                            new Object[] { this.getValue() });
                    }
                                        catch (InvocationTargetException ivexn) {
                                          throw ivexn.getTargetException();
                                        }
                                      }
                                      Device.debug("Received value attribute "
                                        + attrname + "=" + o);
                                      attributes.put(attrname, (Value)o);
                                    }
                                  }
                            catch (ThreadDeath d) {
                              throw d;
                            }
                            catch (Throwable exn) {
                              Device.this.error(exn);
                            }
                          }
          });
        }
      }


      @Override
      protected void end(String name) {

        Device.debug("  " + this.getValue());
        Device.debug("</" + name + ">");

        if (name.equals("value") || name.equals("elem")) {
          Value value = null;
          if (type.equals(Device.typenames[TypeID.STRUCT.ordinal()])) {
            String[] labels = new String[elements.size()];
            Value[] values = new Value[elements.size()];
            int i = 0;
            for (Iterator<String> it = elements.keySet().iterator(); it
              .hasNext(); i++)
                        {
                          labels[i] = it.next();
                          values[i] = elements.get(labels[i]);
                        }
                        value = new StructValue(labels, values);
                      }
                      else if (type.equals(Device.typenames[TypeID.LIST
                        .ordinal()])) {
                        Value[] values = new Value[elements.size()];
                        for (Iterator<String> it = elements.keySet().iterator(); it
                          .hasNext();) {
                          String index = it.next();
                          values[Integer.parseInt(index)] = elements.get(index);
                        }
                        value = new ListValue(values);
                      }
                      /*
                       * else if (type.equals(PointerValue.class.getName())) {
                       * value = new PointerValue(new Variable() { Value base =
                       * (Value) elements.get("base"); public Value getValue() {
                       * return base; } public void setValue(Value v) { base =
                       * v; } }); }
                       */
                      else if (type.equals(Device.typenames[TypeID.BOOL
                        .ordinal()])) {
                        try {
                          value = BoolValue.valueOf(this.prim_value);
                        }
                        catch (Exception exn) {
                          Device.this.error(exn);
                        }
                      }
                      else if (type.equals(Device.typenames[TypeID.INT
                        .ordinal()])) {
                        try {
                          value = IntValue.valueOf(this.prim_value);
                        }
                        catch (Exception exn) {
                          Device.this.error(exn);
                        }
                      }
                      else if (type.equals(Device.typenames[TypeID.REAL
                        .ordinal()])) {
                        try {
                          value = RealValue.valueOf(this.prim_value);
                        }
                        catch (Exception exn) {
                          Device.this.error(exn);
                        }
                      }
                      else if (type.equals(Device.typenames[TypeID.STRING
                        .ordinal()])) {
                        value = StringValue.valueOf(this.prim_value);
                      }
                      else if (type.equals("null")) {
                        value = null;
                      }
                      else if (type.equals(Device.typenames[TypeID.UNDEF
                        .ordinal()])) {
                        value = new Undefined();
                      }

                      if (value != null) {
                        for (String attr : attributes.keySet()) {
                          value.setAttribute(attr, (PrimitiveValue)attributes
                            .get(attr));
                        }
                      }
                      ref.valueReceived(elem, value);
                    }
                    else if (name.equals("val")) {
                      this.prim_value = this.getValue();
                    }
                  }
    });
  }


  protected void send(XMLWriter out, Object value)
      throws IOException {

    if (out != null) {
      synchronized (out) {
        if (value == null) {
          out.printElement(Protocol.XML_VALUE, new String[] { "type" },
                      new Object[] { "null" });
        }
        else {
          this.sendValue(out, Protocol.XML_VALUE, null, value);
        }

        out.flush();

        synchronized (this.deviceListeners) {
          DeviceEvent evt = new DeviceEvent(this, this.getState(), value);
          for (DeviceListener listener : this.deviceListeners) {
            listener.dataSent(evt);
          }
        }
      }
    }
    else {
      throw new ConnectException("Device not connected");
    }
  }


  private void sendValue(XMLWriter out, String tag, String name, Object value) {

    String[] params = null;
    String[] args = null;

    if (name == null) {
      params = new String[] { "type" };
      args = new String[] { Device.getTypeName(value.getClass()) };
    }
    else {
      params = new String[] { "name", "type" };
      args = new String[] { name, Device.getTypeName(value.getClass()) };
    }

    out.openElement(tag, params, args);

    if (value instanceof Value) {
      for (String attr : ((Value)value).getAttributes()) {
        PrimitiveValue o = ((Value)value).getAttribute(attr);
        String s;
        if (o instanceof StringValue) {
          s = ((StringValue)o).getString();
        }
        else {
          s = o.toString();
        }

        out.printElement("att", new String[] { "name", "type" }, new Object[] {
          attr,
                        Device.getTypeName(o.getClass()) }, s);
      }
    }

    if (value instanceof StructValue) {
      StructValue s = (StructValue)value;
      for (Iterator<String> keys = s.labels(); keys.hasNext();) {
        String key = keys.next();
        Value v = s.getValue(key);
        this.sendValue(out, "elem", key, v);
      }
    }
    else if (value instanceof Map) {
      Map<?, ?> m = (Map)value;
      for (Iterator<?> keys = m.keySet().iterator(); keys.hasNext();) {
        String key = keys.next().toString();
        Object v = m.get(key);
        this.sendValue(out, "elem", key, v);
      }
    }
    else if ((value instanceof ListValue) || (value instanceof Collection)
                || (value instanceof Iterable) || (value instanceof Iterator)
                || (value instanceof Enumeration)) {
      Iterator<?> iterator;
      if (value instanceof ListValue) {
        iterator = ((ListValue)value).iterator();
      }
      else if (value instanceof Collection) {
        iterator = ((Collection)value).iterator();
      }
      else if (value instanceof Iterable) {
        iterator = ((Iterable)value).iterator();
      }
      else if (value instanceof Enumeration) {
        final Enumeration<?> e = (Enumeration)value;
        iterator = new Iterator<Object>() {

          public boolean hasNext() {

            return e.hasMoreElements();
          }


          public Object next() {

            return e.nextElement();
          }


          public void remove() {

            throw new UnsupportedOperationException();
          }
        };
      }
      else if (value instanceof Iterator) {
        iterator = (Iterator)value;
      }
      else {
        throw new ClassCastException();
      }

      for (int i = 0; iterator.hasNext(); i++) {
        this.sendValue(out, "elem", String.valueOf(i), iterator.next());
      }
    }
    else if (value.getClass().isArray()) {
      Object[] o = (Object[])value;
      for (int i = 0; i < o.length; i++) {
        this.sendValue(out, "elem", String.valueOf(i), o[i]);
      }
    }
    /*
     * else if (value instanceof PointerValue) { PointerValue p = (PointerValue)
     * value; sendValue(out, "elem", "base", p.getValue()); }
     */
    else if (value instanceof BoolValue) {
      out.printElement("val", String.valueOf(((BoolValue)value).getBool()));
    }
    else if (value instanceof IntValue) {
      out.printElement("val", String.valueOf(((IntValue)value).getInt()));
    }
    else if (value instanceof RealValue) {
      out.printElement("val", String.valueOf(((RealValue)value).getReal()));
    }
    else if (value instanceof StringValue) {
      out.printElement("val", String.valueOf(((StringValue)value).getString()));
    }
    else {
      // for Java builtin types like String, Boolean, Integer, etc.
      out.printElement("val", value.toString());
    }

    out.closeElement(tag);
  }


  public Value receive(DataInputStream in)
      throws IOException {

    Value value;
    synchronized (this.receiveLock) {
      value = this.receiveValue(in, true);
    }

    DeviceEvent evt = new DeviceEvent(Device.this, this.getState(), value);
    synchronized (this.deviceListeners) {
      for (DeviceListener listener : this.deviceListeners) {
        listener.dataReceived(evt);
      }
    }
    return value;
  }

  private static final Value LIST_END = new Value() {

    @Override
    public boolean equals(Object o) {

      return o == this;
    }


    @Override
    public int hashCode() {

      return 0;
    }


    @Override
    public Type getType() {

      return null;
    }


    @Override
    public String toString() {

      return "nil";
    }


    @Override
    public Value copyValue() {

      return this;
    }


	@Override
	public Object getReadableValue()
	{
		return null;
	}
  };


  private Value receiveValue(final DataInputStream in, boolean withAttributes)
      throws IOException {

    byte type = in.readByte();
    if (type == -1) {
      return Device.LIST_END;
    }
    else if (type == TypeID.NULL.ordinal()) {
      return null;
    }
    else {
      if (type >= TypeID.values().length) {
        throw new Protocol.Exception("Unexpected data type: " + type);
      }

      Map<String, Value> atts = new HashMap<String, Value>();
      if (withAttributes) {
        int numAtts = in.readInt();
        for (int i = 0; i < numAtts; i++) {
          String att = in.readUTF();
          atts.put(att, this.receiveValue(in, false));
        }
      }

      Value value;
      TypeID typeID = TypeID.values()[type];
      switch (typeID) {
        case STRUCT:
          String[] labels = new String[in.readInt()];
          Value[] elements = new Value[labels.length];
          for (int i = 0; i < labels.length; i++) {
            labels[i] = in.readUTF();
            elements[i] = this.receiveValue(in, withAttributes);
          }
          value = new StructValue(labels, elements);
          break;
        case LIST:
          List<Value> l = new LinkedList<Value>();
          Value elem;
          while ((elem = this.receiveValue(in, withAttributes)) != Device.LIST_END) {
            l.add(elem);
          }
          value = new ListValue(l.toArray(new Value[l.size()]));
          break;
        case INT:
          value = new IntValue(in.readLong());
          break;
        case REAL:
          value = new RealValue(in.readDouble());
          break;
        case BOOL:
          value = new BoolValue(in.readBoolean());
          break;
        case STRING:
          value = new StringValue(in.readUTF());
          break;
        case UNDEF:
          value = new Undefined();
          break;
        default:
          throw new Protocol.Exception("Unexpected data type: " + typeID);
      }

      for (String key : atts.keySet()) {
        value.setAttribute(key, (PrimitiveValue)atts.get(key));
      }

      return value;
    }
  }


  protected void send(DataOutputStream out, Object value)
      throws IOException {

    if (out != null) {
      synchronized (out) {
        out.writeByte(Protocol.BIN_VALUE);
        if (value == null) {
          out.writeByte(TypeID.NULL.ordinal());
        }
        else {
          this.sendValue(out, value, true);
        }

        out.flush();

        synchronized (this.deviceListeners) {
          DeviceEvent evt = new DeviceEvent(this, this.getState(), value);
          for (DeviceListener listener : this.deviceListeners) {
            listener.dataSent(evt);
          }
        }
      }
    }
    else {
      throw new ConnectException("Device not connected");
    }
  }


  private void sendValue(DataOutputStream out, Object value,
      boolean writeAttributes)
        throws IOException {

    TypeID type = Device.getTypeID(value.getClass());
    out.writeByte(type.ordinal());

    if (writeAttributes) {
      if (value instanceof Value) {
        Collection<String> attributes = ((Value)value).getAttributes();
        out.writeInt(attributes.size());

        for (String attr : attributes) {
          PrimitiveValue o = ((Value)value).getAttribute(attr);
          out.writeUTF(attr);
          this.sendValue(out, o, false);
        }
      }
      else {
        out.writeInt(0);
      }
    }

    if (value instanceof StructValue) {
      StructValue s = (StructValue)value;
      out.writeInt(s.size());
      for (Iterator<String> keys = s.labels(); keys.hasNext();) {
        String key = keys.next();
        Value v = s.getValue(key);
        out.writeUTF(key);
        this.sendValue(out, v, writeAttributes);
      }
    }
    else if (value instanceof Map) {
      Map<?, ?> m = (Map)value;
      out.writeInt(m.size());
      for (Iterator<?> keys = m.keySet().iterator(); keys.hasNext();) {
        String key = keys.next().toString();
        Object v = m.get(key);
        out.writeUTF(key);
        this.sendValue(out, v, writeAttributes);
      }
    }
    else if ((value instanceof ListValue) || (value instanceof Collection)
                || (value instanceof Iterable) || (value instanceof Iterator)
                || (value instanceof Enumeration)) {
      Iterator<?> iterator;
      if (value instanceof ListValue) {
        iterator = ((ListValue)value).iterator();
      }
      else if (value instanceof Collection) {
        iterator = ((Collection)value).iterator();
      }
      else if (value instanceof Iterable) {
        iterator = ((Iterable)value).iterator();
      }
      else if (value instanceof Enumeration) {
        final Enumeration<?> e = (Enumeration)value;
        iterator = new Iterator<Object>() {

          public boolean hasNext() {

            return e.hasMoreElements();
          }


          public Object next() {

            return e.nextElement();
          }


          public void remove() {

            throw new UnsupportedOperationException();
          }
        };
      }
      else if (value instanceof Iterator) {
        iterator = (Iterator)value;
      }
      else {
        throw new ClassCastException();
      }

      for (int i = 0; iterator.hasNext(); i++) {
        this.sendValue(out, iterator.next(), writeAttributes);
      }
      out.writeByte(-1);
    }
    else if (value.getClass().isArray()) {
      Object[] o = (Object[])value;
      for (int i = 0; i < o.length; i++) {
        this.sendValue(out, o[i], writeAttributes);
      }
      out.writeByte(-1);
    }
    /*
     * else if (value instanceof PointerValue) { PointerValue p = (PointerValue)
     * value; sendValue(out, "elem", "base", p.getValue()); }
     */
    else if (value instanceof BoolValue) {
      out.writeBoolean(((BoolValue)value).getBool());
    }
    else if (value instanceof IntValue) {
      out.writeLong(((IntValue)value).getInt());
    }
    else if (value instanceof RealValue) {
      out.writeDouble(((RealValue)value).getReal());
    }
    else if (value instanceof StringValue) {
      out.writeUTF(((StringValue)value).getString());
    }
    else if (value instanceof String) {
      out.writeUTF((String)value);
    }
    else if (value instanceof Boolean) {
      out.writeBoolean(((Boolean)value).booleanValue());
    }
    else if (value instanceof Number) {
      if (type == TypeID.INT) {
        out.writeLong(((Number)value).longValue());
      }
      else if (type == TypeID.REAL) {
        out.writeDouble(((Number)value).doubleValue());
      }
      else {
        throw new IllegalArgumentException();
      }
    }
    else if (value instanceof Undefined) {
    }
    else {
      out.writeUTF(value.toString());
    }
  }

  public static interface ValueReceiver {

    public void valueReceived(String name, Value v);
  }


  public static String toString(Attributes atts) {

    StringBuilder b = new StringBuilder();
    for (int i = 0; i < atts.getLength(); i++) {
      if (i > 0) {
        b.append(' ');
      }
      b.append(atts.getQName(i));
      b.append("=\"");
      b.append(atts.getValue(i));
      b.append("\"");
    }
    return b.toString();
  }
}
