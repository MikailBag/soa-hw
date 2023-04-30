/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package com.example.demo.formats.avro;

import org.apache.avro.generic.GenericArray;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.util.Utf8;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

@org.apache.avro.specific.AvroGenerated
public class AvroEnterpriseLevelCustomer extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = 3857950597226512358L;


  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"AvroEnterpriseLevelCustomer\",\"namespace\":\"com.example.demo.formats.avro\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"age\",\"type\":\"int\"},{\"name\":\"balance\",\"type\":\"double\"},{\"name\":\"status\",\"type\":{\"type\":\"enum\",\"name\":\"AvroCustomerStatus\",\"symbols\":[\"ALIVE\",\"DEAD\",\"DEAD_INSIDE\",\"UNKNOWN\"]}},{\"name\":\"cats\",\"type\":{\"type\":\"array\",\"items\":\"string\"}},{\"name\":\"history\",\"type\":{\"type\":\"map\",\"values\":{\"type\":\"array\",\"items\":\"string\"}}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static final SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<AvroEnterpriseLevelCustomer> ENCODER =
      new BinaryMessageEncoder<>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<AvroEnterpriseLevelCustomer> DECODER =
      new BinaryMessageDecoder<>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageEncoder instance used by this class.
   * @return the message encoder used by this class
   */
  public static BinaryMessageEncoder<AvroEnterpriseLevelCustomer> getEncoder() {
    return ENCODER;
  }

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   * @return the message decoder used by this class
   */
  public static BinaryMessageDecoder<AvroEnterpriseLevelCustomer> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   * @return a BinaryMessageDecoder instance for this class backed by the given SchemaStore
   */
  public static BinaryMessageDecoder<AvroEnterpriseLevelCustomer> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<>(MODEL$, SCHEMA$, resolver);
  }

  /**
   * Serializes this AvroEnterpriseLevelCustomer to a ByteBuffer.
   * @return a buffer holding the serialized data for this instance
   * @throws java.io.IOException if this instance could not be serialized
   */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /**
   * Deserializes a AvroEnterpriseLevelCustomer from a ByteBuffer.
   * @param b a byte buffer holding serialized data for an instance of this class
   * @return a AvroEnterpriseLevelCustomer instance decoded from the given buffer
   * @throws java.io.IOException if the given bytes could not be deserialized into an instance of this class
   */
  public static AvroEnterpriseLevelCustomer fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  private java.lang.CharSequence name;
  private int age;
  private double balance;
  private com.example.demo.formats.avro.AvroCustomerStatus status;
  private java.util.List<java.lang.CharSequence> cats;
  private java.util.Map<java.lang.CharSequence,java.util.List<java.lang.CharSequence>> history;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public AvroEnterpriseLevelCustomer() {}

  /**
   * All-args constructor.
   * @param name The new value for name
   * @param age The new value for age
   * @param balance The new value for balance
   * @param status The new value for status
   * @param cats The new value for cats
   * @param history The new value for history
   */
  public AvroEnterpriseLevelCustomer(java.lang.CharSequence name, java.lang.Integer age, java.lang.Double balance, com.example.demo.formats.avro.AvroCustomerStatus status, java.util.List<java.lang.CharSequence> cats, java.util.Map<java.lang.CharSequence,java.util.List<java.lang.CharSequence>> history) {
    this.name = name;
    this.age = age;
    this.balance = balance;
    this.status = status;
    this.cats = cats;
    this.history = history;
  }

  @Override
  public org.apache.avro.specific.SpecificData getSpecificData() { return MODEL$; }

  @Override
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }

  // Used by DatumWriter.  Applications should not call.
  @Override
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return name;
    case 1: return age;
    case 2: return balance;
    case 3: return status;
    case 4: return cats;
    case 5: return history;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  // Used by DatumReader.  Applications should not call.
  @Override
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: name = (java.lang.CharSequence)value$; break;
    case 1: age = (java.lang.Integer)value$; break;
    case 2: balance = (java.lang.Double)value$; break;
    case 3: status = (com.example.demo.formats.avro.AvroCustomerStatus)value$; break;
    case 4: cats = (java.util.List<java.lang.CharSequence>)value$; break;
    case 5: history = (java.util.Map<java.lang.CharSequence,java.util.List<java.lang.CharSequence>>)value$; break;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  /**
   * Gets the value of the 'name' field.
   * @return The value of the 'name' field.
   */
  public java.lang.CharSequence getName() {
    return name;
  }


  /**
   * Sets the value of the 'name' field.
   * @param value the value to set.
   */
  public void setName(java.lang.CharSequence value) {
    this.name = value;
  }

  /**
   * Gets the value of the 'age' field.
   * @return The value of the 'age' field.
   */
  public int getAge() {
    return age;
  }


  /**
   * Sets the value of the 'age' field.
   * @param value the value to set.
   */
  public void setAge(int value) {
    this.age = value;
  }

  /**
   * Gets the value of the 'balance' field.
   * @return The value of the 'balance' field.
   */
  public double getBalance() {
    return balance;
  }


  /**
   * Sets the value of the 'balance' field.
   * @param value the value to set.
   */
  public void setBalance(double value) {
    this.balance = value;
  }

  /**
   * Gets the value of the 'status' field.
   * @return The value of the 'status' field.
   */
  public com.example.demo.formats.avro.AvroCustomerStatus getStatus() {
    return status;
  }


  /**
   * Sets the value of the 'status' field.
   * @param value the value to set.
   */
  public void setStatus(com.example.demo.formats.avro.AvroCustomerStatus value) {
    this.status = value;
  }

  /**
   * Gets the value of the 'cats' field.
   * @return The value of the 'cats' field.
   */
  public java.util.List<java.lang.CharSequence> getCats() {
    return cats;
  }


  /**
   * Sets the value of the 'cats' field.
   * @param value the value to set.
   */
  public void setCats(java.util.List<java.lang.CharSequence> value) {
    this.cats = value;
  }

  /**
   * Gets the value of the 'history' field.
   * @return The value of the 'history' field.
   */
  public java.util.Map<java.lang.CharSequence,java.util.List<java.lang.CharSequence>> getHistory() {
    return history;
  }


  /**
   * Sets the value of the 'history' field.
   * @param value the value to set.
   */
  public void setHistory(java.util.Map<java.lang.CharSequence,java.util.List<java.lang.CharSequence>> value) {
    this.history = value;
  }

  /**
   * Creates a new AvroEnterpriseLevelCustomer RecordBuilder.
   * @return A new AvroEnterpriseLevelCustomer RecordBuilder
   */
  public static com.example.demo.formats.avro.AvroEnterpriseLevelCustomer.Builder newBuilder() {
    return new com.example.demo.formats.avro.AvroEnterpriseLevelCustomer.Builder();
  }

  /**
   * Creates a new AvroEnterpriseLevelCustomer RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new AvroEnterpriseLevelCustomer RecordBuilder
   */
  public static com.example.demo.formats.avro.AvroEnterpriseLevelCustomer.Builder newBuilder(com.example.demo.formats.avro.AvroEnterpriseLevelCustomer.Builder other) {
    if (other == null) {
      return new com.example.demo.formats.avro.AvroEnterpriseLevelCustomer.Builder();
    } else {
      return new com.example.demo.formats.avro.AvroEnterpriseLevelCustomer.Builder(other);
    }
  }

  /**
   * Creates a new AvroEnterpriseLevelCustomer RecordBuilder by copying an existing AvroEnterpriseLevelCustomer instance.
   * @param other The existing instance to copy.
   * @return A new AvroEnterpriseLevelCustomer RecordBuilder
   */
  public static com.example.demo.formats.avro.AvroEnterpriseLevelCustomer.Builder newBuilder(com.example.demo.formats.avro.AvroEnterpriseLevelCustomer other) {
    if (other == null) {
      return new com.example.demo.formats.avro.AvroEnterpriseLevelCustomer.Builder();
    } else {
      return new com.example.demo.formats.avro.AvroEnterpriseLevelCustomer.Builder(other);
    }
  }

  /**
   * RecordBuilder for AvroEnterpriseLevelCustomer instances.
   */
  @org.apache.avro.specific.AvroGenerated
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<AvroEnterpriseLevelCustomer>
    implements org.apache.avro.data.RecordBuilder<AvroEnterpriseLevelCustomer> {

    private java.lang.CharSequence name;
    private int age;
    private double balance;
    private com.example.demo.formats.avro.AvroCustomerStatus status;
    private java.util.List<java.lang.CharSequence> cats;
    private java.util.Map<java.lang.CharSequence,java.util.List<java.lang.CharSequence>> history;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$, MODEL$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(com.example.demo.formats.avro.AvroEnterpriseLevelCustomer.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.name)) {
        this.name = data().deepCopy(fields()[0].schema(), other.name);
        fieldSetFlags()[0] = other.fieldSetFlags()[0];
      }
      if (isValidValue(fields()[1], other.age)) {
        this.age = data().deepCopy(fields()[1].schema(), other.age);
        fieldSetFlags()[1] = other.fieldSetFlags()[1];
      }
      if (isValidValue(fields()[2], other.balance)) {
        this.balance = data().deepCopy(fields()[2].schema(), other.balance);
        fieldSetFlags()[2] = other.fieldSetFlags()[2];
      }
      if (isValidValue(fields()[3], other.status)) {
        this.status = data().deepCopy(fields()[3].schema(), other.status);
        fieldSetFlags()[3] = other.fieldSetFlags()[3];
      }
      if (isValidValue(fields()[4], other.cats)) {
        this.cats = data().deepCopy(fields()[4].schema(), other.cats);
        fieldSetFlags()[4] = other.fieldSetFlags()[4];
      }
      if (isValidValue(fields()[5], other.history)) {
        this.history = data().deepCopy(fields()[5].schema(), other.history);
        fieldSetFlags()[5] = other.fieldSetFlags()[5];
      }
    }

    /**
     * Creates a Builder by copying an existing AvroEnterpriseLevelCustomer instance
     * @param other The existing instance to copy.
     */
    private Builder(com.example.demo.formats.avro.AvroEnterpriseLevelCustomer other) {
      super(SCHEMA$, MODEL$);
      if (isValidValue(fields()[0], other.name)) {
        this.name = data().deepCopy(fields()[0].schema(), other.name);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.age)) {
        this.age = data().deepCopy(fields()[1].schema(), other.age);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.balance)) {
        this.balance = data().deepCopy(fields()[2].schema(), other.balance);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.status)) {
        this.status = data().deepCopy(fields()[3].schema(), other.status);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.cats)) {
        this.cats = data().deepCopy(fields()[4].schema(), other.cats);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.history)) {
        this.history = data().deepCopy(fields()[5].schema(), other.history);
        fieldSetFlags()[5] = true;
      }
    }

    /**
      * Gets the value of the 'name' field.
      * @return The value.
      */
    public java.lang.CharSequence getName() {
      return name;
    }


    /**
      * Sets the value of the 'name' field.
      * @param value The value of 'name'.
      * @return This builder.
      */
    public com.example.demo.formats.avro.AvroEnterpriseLevelCustomer.Builder setName(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.name = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'name' field has been set.
      * @return True if the 'name' field has been set, false otherwise.
      */
    public boolean hasName() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'name' field.
      * @return This builder.
      */
    public com.example.demo.formats.avro.AvroEnterpriseLevelCustomer.Builder clearName() {
      name = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'age' field.
      * @return The value.
      */
    public int getAge() {
      return age;
    }


    /**
      * Sets the value of the 'age' field.
      * @param value The value of 'age'.
      * @return This builder.
      */
    public com.example.demo.formats.avro.AvroEnterpriseLevelCustomer.Builder setAge(int value) {
      validate(fields()[1], value);
      this.age = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'age' field has been set.
      * @return True if the 'age' field has been set, false otherwise.
      */
    public boolean hasAge() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'age' field.
      * @return This builder.
      */
    public com.example.demo.formats.avro.AvroEnterpriseLevelCustomer.Builder clearAge() {
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'balance' field.
      * @return The value.
      */
    public double getBalance() {
      return balance;
    }


    /**
      * Sets the value of the 'balance' field.
      * @param value The value of 'balance'.
      * @return This builder.
      */
    public com.example.demo.formats.avro.AvroEnterpriseLevelCustomer.Builder setBalance(double value) {
      validate(fields()[2], value);
      this.balance = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'balance' field has been set.
      * @return True if the 'balance' field has been set, false otherwise.
      */
    public boolean hasBalance() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'balance' field.
      * @return This builder.
      */
    public com.example.demo.formats.avro.AvroEnterpriseLevelCustomer.Builder clearBalance() {
      fieldSetFlags()[2] = false;
      return this;
    }

    /**
      * Gets the value of the 'status' field.
      * @return The value.
      */
    public com.example.demo.formats.avro.AvroCustomerStatus getStatus() {
      return status;
    }


    /**
      * Sets the value of the 'status' field.
      * @param value The value of 'status'.
      * @return This builder.
      */
    public com.example.demo.formats.avro.AvroEnterpriseLevelCustomer.Builder setStatus(com.example.demo.formats.avro.AvroCustomerStatus value) {
      validate(fields()[3], value);
      this.status = value;
      fieldSetFlags()[3] = true;
      return this;
    }

    /**
      * Checks whether the 'status' field has been set.
      * @return True if the 'status' field has been set, false otherwise.
      */
    public boolean hasStatus() {
      return fieldSetFlags()[3];
    }


    /**
      * Clears the value of the 'status' field.
      * @return This builder.
      */
    public com.example.demo.formats.avro.AvroEnterpriseLevelCustomer.Builder clearStatus() {
      status = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    /**
      * Gets the value of the 'cats' field.
      * @return The value.
      */
    public java.util.List<java.lang.CharSequence> getCats() {
      return cats;
    }


    /**
      * Sets the value of the 'cats' field.
      * @param value The value of 'cats'.
      * @return This builder.
      */
    public com.example.demo.formats.avro.AvroEnterpriseLevelCustomer.Builder setCats(java.util.List<java.lang.CharSequence> value) {
      validate(fields()[4], value);
      this.cats = value;
      fieldSetFlags()[4] = true;
      return this;
    }

    /**
      * Checks whether the 'cats' field has been set.
      * @return True if the 'cats' field has been set, false otherwise.
      */
    public boolean hasCats() {
      return fieldSetFlags()[4];
    }


    /**
      * Clears the value of the 'cats' field.
      * @return This builder.
      */
    public com.example.demo.formats.avro.AvroEnterpriseLevelCustomer.Builder clearCats() {
      cats = null;
      fieldSetFlags()[4] = false;
      return this;
    }

    /**
      * Gets the value of the 'history' field.
      * @return The value.
      */
    public java.util.Map<java.lang.CharSequence,java.util.List<java.lang.CharSequence>> getHistory() {
      return history;
    }


    /**
      * Sets the value of the 'history' field.
      * @param value The value of 'history'.
      * @return This builder.
      */
    public com.example.demo.formats.avro.AvroEnterpriseLevelCustomer.Builder setHistory(java.util.Map<java.lang.CharSequence,java.util.List<java.lang.CharSequence>> value) {
      validate(fields()[5], value);
      this.history = value;
      fieldSetFlags()[5] = true;
      return this;
    }

    /**
      * Checks whether the 'history' field has been set.
      * @return True if the 'history' field has been set, false otherwise.
      */
    public boolean hasHistory() {
      return fieldSetFlags()[5];
    }


    /**
      * Clears the value of the 'history' field.
      * @return This builder.
      */
    public com.example.demo.formats.avro.AvroEnterpriseLevelCustomer.Builder clearHistory() {
      history = null;
      fieldSetFlags()[5] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AvroEnterpriseLevelCustomer build() {
      try {
        AvroEnterpriseLevelCustomer record = new AvroEnterpriseLevelCustomer();
        record.name = fieldSetFlags()[0] ? this.name : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.age = fieldSetFlags()[1] ? this.age : (java.lang.Integer) defaultValue(fields()[1]);
        record.balance = fieldSetFlags()[2] ? this.balance : (java.lang.Double) defaultValue(fields()[2]);
        record.status = fieldSetFlags()[3] ? this.status : (com.example.demo.formats.avro.AvroCustomerStatus) defaultValue(fields()[3]);
        record.cats = fieldSetFlags()[4] ? this.cats : (java.util.List<java.lang.CharSequence>) defaultValue(fields()[4]);
        record.history = fieldSetFlags()[5] ? this.history : (java.util.Map<java.lang.CharSequence,java.util.List<java.lang.CharSequence>>) defaultValue(fields()[5]);
        return record;
      } catch (org.apache.avro.AvroMissingFieldException e) {
        throw e;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<AvroEnterpriseLevelCustomer>
    WRITER$ = (org.apache.avro.io.DatumWriter<AvroEnterpriseLevelCustomer>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<AvroEnterpriseLevelCustomer>
    READER$ = (org.apache.avro.io.DatumReader<AvroEnterpriseLevelCustomer>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

  @Override protected boolean hasCustomCoders() { return true; }

  @Override public void customEncode(org.apache.avro.io.Encoder out)
    throws java.io.IOException
  {
    out.writeString(this.name);

    out.writeInt(this.age);

    out.writeDouble(this.balance);

    out.writeEnum(this.status.ordinal());

    long size0 = this.cats.size();
    out.writeArrayStart();
    out.setItemCount(size0);
    long actualSize0 = 0;
    for (java.lang.CharSequence e0: this.cats) {
      actualSize0++;
      out.startItem();
      out.writeString(e0);
    }
    out.writeArrayEnd();
    if (actualSize0 != size0)
      throw new java.util.ConcurrentModificationException("Array-size written was " + size0 + ", but element count was " + actualSize0 + ".");

    long size1 = this.history.size();
    out.writeMapStart();
    out.setItemCount(size1);
    long actualSize1 = 0;
    for (java.util.Map.Entry<java.lang.CharSequence, java.util.List<java.lang.CharSequence>> e1: this.history.entrySet()) {
      actualSize1++;
      out.startItem();
      out.writeString(e1.getKey());
      java.util.List<java.lang.CharSequence> v1 = e1.getValue();
      long size2 = v1.size();
      out.writeArrayStart();
      out.setItemCount(size2);
      long actualSize2 = 0;
      for (java.lang.CharSequence e2: v1) {
        actualSize2++;
        out.startItem();
        out.writeString(e2);
      }
      out.writeArrayEnd();
      if (actualSize2 != size2)
        throw new java.util.ConcurrentModificationException("Array-size written was " + size2 + ", but element count was " + actualSize2 + ".");
    }
    out.writeMapEnd();
    if (actualSize1 != size1)
      throw new java.util.ConcurrentModificationException("Map-size written was " + size1 + ", but element count was " + actualSize1 + ".");

  }

  @Override public void customDecode(org.apache.avro.io.ResolvingDecoder in)
    throws java.io.IOException
  {
    org.apache.avro.Schema.Field[] fieldOrder = in.readFieldOrderIfDiff();
    if (fieldOrder == null) {
      this.name = in.readString(this.name instanceof Utf8 ? (Utf8)this.name : null);

      this.age = in.readInt();

      this.balance = in.readDouble();

      this.status = com.example.demo.formats.avro.AvroCustomerStatus.values()[in.readEnum()];

      long size0 = in.readArrayStart();
      java.util.List<java.lang.CharSequence> a0 = this.cats;
      if (a0 == null) {
        a0 = new SpecificData.Array<java.lang.CharSequence>((int)size0, SCHEMA$.getField("cats").schema());
        this.cats = a0;
      } else a0.clear();
      SpecificData.Array<java.lang.CharSequence> ga0 = (a0 instanceof SpecificData.Array ? (SpecificData.Array<java.lang.CharSequence>)a0 : null);
      for ( ; 0 < size0; size0 = in.arrayNext()) {
        for ( ; size0 != 0; size0--) {
          java.lang.CharSequence e0 = (ga0 != null ? ga0.peek() : null);
          e0 = in.readString(e0 instanceof Utf8 ? (Utf8)e0 : null);
          a0.add(e0);
        }
      }

      long size1 = in.readMapStart();
      java.util.Map<java.lang.CharSequence,java.util.List<java.lang.CharSequence>> m1 = this.history; // Need fresh name due to limitation of macro system
      if (m1 == null) {
        m1 = new java.util.HashMap<java.lang.CharSequence,java.util.List<java.lang.CharSequence>>((int)size1);
        this.history = m1;
      } else m1.clear();
      for ( ; 0 < size1; size1 = in.mapNext()) {
        for ( ; size1 != 0; size1--) {
          java.lang.CharSequence k1 = null;
          k1 = in.readString(k1 instanceof Utf8 ? (Utf8)k1 : null);
          java.util.List<java.lang.CharSequence> v1 = null;
          long size2 = in.readArrayStart();
          java.util.List<java.lang.CharSequence> a2 = v1;
          if (a2 == null) {
            a2 = new SpecificData.Array<java.lang.CharSequence>((int)size2, SCHEMA$.getField("history").schema().getValueType());
            v1 = a2;
          } else a2.clear();
          SpecificData.Array<java.lang.CharSequence> ga2 = (a2 instanceof SpecificData.Array ? (SpecificData.Array<java.lang.CharSequence>)a2 : null);
          for ( ; 0 < size2; size2 = in.arrayNext()) {
            for ( ; size2 != 0; size2--) {
              java.lang.CharSequence e2 = (ga2 != null ? ga2.peek() : null);
              e2 = in.readString(e2 instanceof Utf8 ? (Utf8)e2 : null);
              a2.add(e2);
            }
          }
          m1.put(k1, v1);
        }
      }

    } else {
      for (int i = 0; i < 6; i++) {
        switch (fieldOrder[i].pos()) {
        case 0:
          this.name = in.readString(this.name instanceof Utf8 ? (Utf8)this.name : null);
          break;

        case 1:
          this.age = in.readInt();
          break;

        case 2:
          this.balance = in.readDouble();
          break;

        case 3:
          this.status = com.example.demo.formats.avro.AvroCustomerStatus.values()[in.readEnum()];
          break;

        case 4:
          long size0 = in.readArrayStart();
          java.util.List<java.lang.CharSequence> a0 = this.cats;
          if (a0 == null) {
            a0 = new SpecificData.Array<java.lang.CharSequence>((int)size0, SCHEMA$.getField("cats").schema());
            this.cats = a0;
          } else a0.clear();
          SpecificData.Array<java.lang.CharSequence> ga0 = (a0 instanceof SpecificData.Array ? (SpecificData.Array<java.lang.CharSequence>)a0 : null);
          for ( ; 0 < size0; size0 = in.arrayNext()) {
            for ( ; size0 != 0; size0--) {
              java.lang.CharSequence e0 = (ga0 != null ? ga0.peek() : null);
              e0 = in.readString(e0 instanceof Utf8 ? (Utf8)e0 : null);
              a0.add(e0);
            }
          }
          break;

        case 5:
          long size1 = in.readMapStart();
          java.util.Map<java.lang.CharSequence,java.util.List<java.lang.CharSequence>> m1 = this.history; // Need fresh name due to limitation of macro system
          if (m1 == null) {
            m1 = new java.util.HashMap<java.lang.CharSequence,java.util.List<java.lang.CharSequence>>((int)size1);
            this.history = m1;
          } else m1.clear();
          for ( ; 0 < size1; size1 = in.mapNext()) {
            for ( ; size1 != 0; size1--) {
              java.lang.CharSequence k1 = null;
              k1 = in.readString(k1 instanceof Utf8 ? (Utf8)k1 : null);
              java.util.List<java.lang.CharSequence> v1 = null;
              long size2 = in.readArrayStart();
              java.util.List<java.lang.CharSequence> a2 = v1;
              if (a2 == null) {
                a2 = new SpecificData.Array<java.lang.CharSequence>((int)size2, SCHEMA$.getField("history").schema().getValueType());
                v1 = a2;
              } else a2.clear();
              SpecificData.Array<java.lang.CharSequence> ga2 = (a2 instanceof SpecificData.Array ? (SpecificData.Array<java.lang.CharSequence>)a2 : null);
              for ( ; 0 < size2; size2 = in.arrayNext()) {
                for ( ; size2 != 0; size2--) {
                  java.lang.CharSequence e2 = (ga2 != null ? ga2.peek() : null);
                  e2 = in.readString(e2 instanceof Utf8 ? (Utf8)e2 : null);
                  a2.add(e2);
                }
              }
              m1.put(k1, v1);
            }
          }
          break;

        default:
          throw new java.io.IOException("Corrupt ResolvingDecoder.");
        }
      }
    }
  }
}










