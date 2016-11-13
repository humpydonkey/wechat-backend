package data.db;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.bson.types.ObjectId;
import org.mongojack.internal.CalendarSerializer;
import org.mongojack.internal.DBRefSerializer;
import org.mongojack.internal.MongoAnnotationIntrospector;
import org.mongojack.internal.MongoJackDeserializers;
import org.mongojack.internal.MongoJackModule;
import org.mongojack.internal.stream.ServerErrorProblemHandler;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.google.common.base.Throwables;

import data.model.Model;

/**
 * Serializes and deserializes to/from JSON.
 */
@SuppressWarnings("deprecation")
@Slf4j
public class ModelSerializer {

  /** Parse an ObjectId encoded either as a plain string or as an object node as exported by MongoDB (in the form {"$oid": * }) */
  private static class ToObjectIdDeserializer extends JsonDeserializer<ObjectId> {
    @Override public ObjectId deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
      JsonNode node = jp.readValueAsTree();
      if (node == null) {
        return null;
      }
      if (node.getNodeType() == JsonNodeType.OBJECT) {
        return new ObjectId(node.get("$oid").asText());
      }
      return new ObjectId(node.textValue());
    }
  }

  /** Parse an ArrayRealVector as a list of doubles **/
  private static class ArrayRealVectorDeserializer extends JsonDeserializer<ArrayRealVector> {
    @Override public ArrayRealVector deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
      List<Double> values = jp.readValueAs(new TypeReference<List<Double>>() {
      });
      Double[] primitiveArray = new Double[values.size()];
      values.toArray(primitiveArray);
      return new ArrayRealVector(primitiveArray);
    }
  }

  /** Parse an enum type but replace unknown values with specified fallback value. */
  @RequiredArgsConstructor
  public static class IgnoreUnknownValuesDeserializer<E extends Enum<E>> extends JsonDeserializer<E> {
    private final Class<E> enumClass;
    private final E fallbackValue;

    @Override public E deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
      JsonNode node = jp.readValueAsTree();
      if (node == null) {
        return null;
      }
      try {
        return Enum.valueOf(enumClass, node.textValue());
      } catch (IllegalArgumentException e) {
        log.info("Ignoring unknown {} value: {}", enumClass.getSimpleName(), node.textValue());
        return fallbackValue;
      }
    }

//    public static class UserFlag extends IgnoreUnknownValuesDeserializer<User.Flag> {
//      UserFlag() {
//        super(User.Flag.class, null);
//      }
//    }
  }

  private static final ObjectMapper JSON_MAPPER;

  public static final ObjectMapper MONGO_MAPPER;

  static {
    // Create a custom Mongo BSON mapper
    MONGO_MAPPER = new ObjectMapper();
    TweakedMongoJackModule.configure(MONGO_MAPPER);

    // Don't include null props or empty lists
    MONGO_MAPPER.setSerializationInclusion(Include.NON_EMPTY);

    // Only include whitelisted props
    MONGO_MAPPER.setVisibilityChecker(
        MONGO_MAPPER.getSerializationConfig().getDefaultVisibilityChecker()
            .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

    JSON_MAPPER = new ObjectMapper();
    JSON_MAPPER.setSerializationInclusion(Include.NON_EMPTY);
    JSON_MAPPER.setVisibilityChecker(
        JSON_MAPPER.getSerializationConfig().getDefaultVisibilityChecker()
            .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
    JSON_MAPPER.registerModule(new JsonModule());
  }

  public static String toString(Object o) {
    try {
      return toString(JSON_MAPPER.writeValueAsBytes(o));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String toPrettyString(Object o) {
    try {
      return toPrettyString(JSON_MAPPER.writeValueAsBytes(o));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String toString(byte[] bytes) {
    try {
      return JSON_MAPPER.readTree(bytes).toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] toBytes(Model<?> o) {
    try {
      return JSON_MAPPER.writeValueAsBytes(o);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static JsonNode valueToTree(Model<?> o) {
    return JSON_MAPPER.valueToTree(o);
  }

  public static byte[] toBytes(JsonNode rootNode) {
    try {
      return JSON_MAPPER.writer().writeValueAsBytes(rootNode);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static String toPrettyString(byte[] bytes) {
    StringWriter w = new StringWriter();
    try {
      JSON_MAPPER.getFactory()
          .createGenerator(w).useDefaultPrettyPrinter().writeTree(JSON_MAPPER.readTree(bytes));
      return w.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

//  public static Person toPerson(String s) {
//    return fromString(s, Person.class);
//  }

//  public static Person toPersonWithVersion(String s, long version) {
//    Person p = toPerson(s);
//    p.setVersion(version);
//    return p;
//  }

  public static <T> T fromString(String string, Class<T> clazz) {
    try {
      return JSON_MAPPER.readValue(string, clazz);
    } catch (IOException e) {
      log.error("Invalid " + clazz.getSimpleName() + " JSON " + string + ": " + e.getMessage(), e);
      throw Throwables.propagate(e);
    }
  }

  public static <T> T fromBytes(byte[] bytes, Class<T> clazz) {
    try {
      return JSON_MAPPER.readValue(bytes, clazz);
    } catch (IOException e) {
      log.error("Invalid " + clazz.getSimpleName() + " JSON " + bytes + ": " + e.getMessage(), e);
      throw Throwables.propagate(e);
    }
  }

  public static class JsonModule extends Module {

    @Override
    public String getModuleName() {
      return "Connectifier json module";
    }

    @Override
    public Version version() {
      return new Version(1, 0, 0, null, null, null);
    }

    @Override
    public void setupModule(SetupContext context) {
      MongoAnnotationIntrospector annotationIntrospector = new MongoAnnotationIntrospector(context.getTypeFactory());
      context.insertAnnotationIntrospector(annotationIntrospector);
      context.addSerializers(new CustomSerializers());
      context.addDeserializers(new CustomDeserializers());
    }
  }

  private static class CustomSerializers extends SimpleSerializers {

    private static final long serialVersionUID = 1L;

    public CustomSerializers() {
      addSerializer(ObjectId.class, new ObjectIdSerializer());
      addSerializer(ArrayRealVector.class, new ArrayRealVectorSerializer());
      addSerializer(LocalDate.class, new StdDelegatingSerializer(LocalDateToDate.INSTANCE));
    }
  }

  private static class CustomDeserializers extends SimpleDeserializers {

    private static final long serialVersionUID = 1L;

    public CustomDeserializers() {
      addDeserializer(ObjectId.class, new ToObjectIdDeserializer());
      addDeserializer(ArrayRealVector.class, new ArrayRealVectorDeserializer());
      addDeserializer(LocalDate.class, new StdDelegatingDeserializer<>(DateToLocalDate.INSTANCE));
// TODO(kevin): this is making all the dates appear as 1970 in search
//      addDeserializer(Date.class, new ToDateDeserializer());
    }
  }

  private static class ObjectIdSerializer extends JsonSerializer<ObjectId> {
    @Override
    public void serialize(ObjectId value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
      jgen.writeString(value.toString());
    }
  }

  private static class ArrayRealVectorSerializer extends JsonSerializer<ArrayRealVector> {
    @Override
    public void serialize(ArrayRealVector value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
      gen.writeStartArray(value.getDimension());
      for (int i = 0 ; i < value.getDimension(); i++) {
        gen.writeNumber(value.getEntry(i));
      }
      gen.writeEndArray();
    }
  }

  private static class LocalDateToDate extends StdConverter<LocalDate, Date> {
    public static final LocalDateToDate INSTANCE = new LocalDateToDate();

    @Override
    public Date convert(LocalDate value) {
      return Date.from(value.atStartOfDay(ZoneOffset.UTC).toInstant());
    }
  }

  private static class DateToLocalDate extends StdConverter<Date, LocalDate> {
    public static final DateToLocalDate INSTANCE = new DateToLocalDate();

    @Override
    public LocalDate convert(Date value) {
      return value.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
    }
  }

  /**
   * Serialize generic object as a scalar, not as a bean.
   * Currently handles only ObjectId, String, Boolean, and (standard subtypes of) Number.
   * Note: does not follow custom serialization rules that may be specified elsewhere for these types.
   */
//  public static class GenericScalarSerializer extends JsonSerializer<Object> {
//
//    @Override
//    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers)
//        throws IOException {
//      if (value instanceof ObjectId) {
//        if (gen.canWriteObjectId()) {
//          gen.writeObjectId(value);
//        } else {
//          serializers.findValueSerializer(ObjectId.class).serialize(value, gen, serializers);
//        }
//      } else if (value instanceof URI) {
//        gen.writeString(value.toString());
//      } else if (value instanceof Date) {
//        serializers.findValueSerializer(Date.class).serialize(value, gen, serializers);
//      } else if (value instanceof URI) {
//        serializers.findValueSerializer(URI.class).serialize(value, gen, serializers);
//      } else if (value instanceof String) {
//        gen.writeString((String) value);
//      } else if (value instanceof Boolean) {
//        gen.writeBoolean(Boolean.valueOf((Boolean) value));
//      } else if (value instanceof Number) {
//        Number num = (Number) value;
//        if (num instanceof BigDecimal) {
//          gen.writeNumber((BigDecimal) num);
//        } else if (num instanceof BigInteger) {
//          gen.writeNumber((BigInteger) num);
//        } else if (num instanceof Double || num instanceof DoubleAccumulator || num instanceof DoubleAdder) {
//          gen.writeNumber(num.doubleValue());
//        } else if (num instanceof Float) {
//          gen.writeNumber(num.floatValue());
//        } else if (num instanceof Long || num instanceof AtomicLong || num instanceof LongAccumulator || num instanceof LongAdder) {
//          gen.writeNumber(num.longValue());
//        } else if (num instanceof Integer || num instanceof AtomicInteger) {
//          gen.writeNumber(num.intValue());
//        } else if (num instanceof Short) {
//          gen.writeNumber(num.shortValue());
//        } else if (num instanceof Byte) {
//          gen.writeNumber(num.byteValue());
//        } else {
//          throw new IllegalArgumentException("Unsupported java.lang.Number subtype: " + num.getClass().getName());
//        }
//      } else {
//        throw new IllegalArgumentException("Unsupported scalar type: " + value.getClass().getName());
//      }
//    }
//  }

  private static class TweakedMongoJackModule extends Module {

    @SuppressWarnings("serial")
    private static class TweakedMongoJackDeserializers extends SimpleDeserializers {
      public TweakedMongoJackDeserializers() {
        addDeserializer(ArrayRealVector.class, new ArrayRealVectorDeserializer());
        addDeserializer(LocalDate.class, new StdDelegatingDeserializer<>(DateToLocalDate.INSTANCE));
      }
    }

    @SuppressWarnings("serial")
    public static class TweakedMongoJackSerializers extends SimpleSerializers {
      @SuppressWarnings("unchecked")
      public TweakedMongoJackSerializers() {
          addSerializer(new DBRefSerializer());
          addSerializer(ObjectId.class, new TweakedObjectIdSerializer());
          addSerializer(Date.class, new TweakedDateSerializer());
          addSerializer(LocalDate.class, new StdDelegatingSerializer(LocalDateToDate.INSTANCE));
          addSerializer(Calendar.class, new CalendarSerializer());
          addSerializer(ArrayRealVector.class, new ArrayRealVectorSerializer());
      }
    }

    /**
     * Avoids StackOverflowError in TokenBuffer / BeanDeserializer.deserializeWithUnwrapped().
     * @author kevin
     */
    private static class TweakedObjectIdSerializer extends org.mongojack.internal.ObjectIdSerializer {
      @Override
      public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider)
          throws IOException {
        if (jgen instanceof TokenBuffer) {
          jgen.setCodec(null); // So next writeObject will embed ObjectId directly, not invoke codec again
        }
        super.serialize(value, jgen, provider);
      }
    }

    /**
     * Avoids StackOverflowError in TokenBuffer / BeanDeserializer.deserializeWithUnwrapped().
     * @author kevin
     */
    private static class TweakedDateSerializer extends org.mongojack.internal.DateSerializer {
      @Override
      public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider)
          throws IOException {
        if (jgen instanceof TokenBuffer) {
          jgen.setCodec(null); // So next writeObject will embed Date directly, not invoke codec again
        }
        super.serialize(value, jgen, provider);
      }
    }

    /** Sets default type used to deserialize Sets to LinkedHashSet (not HashSet) */
    @SuppressWarnings("serial")
    private static class CustomAbstractTypeResolver extends SimpleAbstractTypeResolver {
      public CustomAbstractTypeResolver() {
        addMapping(Set.class, LinkedHashSet.class);
      }
    }

    @Override
    public String getModuleName() {
      return "Tweaked MongoJack Module";
    }

    public static void configure(ObjectMapper objectMapper) {
      objectMapper.registerModule(new TweakedMongoJackModule());
    }

    @Override
    public void setupModule(SetupContext context) {
      MongoAnnotationIntrospector annotationIntrospector = new MongoAnnotationIntrospector(
          context.getTypeFactory());
      context.insertAnnotationIntrospector(annotationIntrospector);
      // Only include non null properties, this makes it possible to use
      // object templates for querying and
      // partial object retrieving
      context.addDeserializationProblemHandler(new ServerErrorProblemHandler());
      context.addSerializers(new TweakedMongoJackSerializers());
      context.addDeserializers(new TweakedMongoJackDeserializers());
      context.addDeserializers(new MongoJackDeserializers());
      context.addAbstractTypeResolver(new CustomAbstractTypeResolver());
    }

    @Override
    public Version version() {
      return MongoJackModule.INSTANCE.version();
    }
  }
}
