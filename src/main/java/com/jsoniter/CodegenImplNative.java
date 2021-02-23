package com.jsoniter;

import com.jsoniter.any.Any;
import com.jsoniter.spi.*;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class CodegenImplNative {
    final static Map<String, String> NATIVE_READS = new HashMap<String, String>() {
        {
            put("float", "iter.readFloat()");
            put("double", "iter.readDouble()");
            put("boolean", "iter.readBoolean()");
            put("byte", "iter.readShort()");
            put("short", "iter.readShort()");
            put("int", "iter.readInt()");
            put("char", "iter.readInt()");
            put("long", "iter.readLong()");
            put(Float.class.getName(), "(iter.readNull() ? null : java.lang.Float.valueOf(iter.readFloat()))");
            put(Double.class.getName(), "(iter.readNull() ? null : java.lang.Double.valueOf(iter.readDouble()))");
            put(Boolean.class.getName(), "(iter.readNull() ? null : java.lang.Boolean.valueOf(iter.readBoolean()))");
            put(Byte.class.getName(), "(iter.readNull() ? null : java.lang.Byte.valueOf((byte)iter.readShort()))");
            put(Character.class.getName(),
                    "(iter.readNull() ? null : java.lang.Character.valueOf((char)iter.readShort()))");
            put(Short.class.getName(), "(iter.readNull() ? null : java.lang.Short.valueOf(iter.readShort()))");
            put(Integer.class.getName(), "(iter.readNull() ? null : java.lang.Integer.valueOf(iter.readInt()))");
            put(Long.class.getName(), "(iter.readNull() ? null : java.lang.Long.valueOf(iter.readLong()))");
            put(BigDecimal.class.getName(), "iter.readBigDecimal()");
            put(BigInteger.class.getName(), "iter.readBigInteger()");
            put(String.class.getName(), "iter.readString()");
            put(Object.class.getName(), "iter.read()");
            put(Any.class.getName(), "iter.readAny()");
        }
    };
    final static Map<Class, Decoder> NATIVE_DECODERS = new HashMap<Class, Decoder>() {
        {
            put(float.class, new Decoder() {
                @Override
                public Object decode(JsonIterator iter) throws IOException {
                    return iter.readFloat();
                }
            });
            put(Float.class, new Decoder() {
                @Override
                public Object decode(JsonIterator iter) throws IOException {
                    return iter.readNull() ? null : iter.readFloat();
                }
            });
            put(double.class, new Decoder() {
                @Override
                public Object decode(JsonIterator iter) throws IOException {
                    return iter.readDouble();
                }
            });
            put(Double.class, new Decoder() {
                @Override
                public Object decode(JsonIterator iter) throws IOException {
                    return iter.readNull() ? null : iter.readDouble();
                }
            });
            put(boolean.class, new Decoder() {
                @Override
                public Object decode(JsonIterator iter) throws IOException {
                    return iter.readBoolean();
                }
            });
            put(Boolean.class, new Decoder() {
                @Override
                public Object decode(JsonIterator iter) throws IOException {
                    return iter.readNull() ? null : iter.readBoolean();
                }
            });
            put(byte.class, new Decoder() {
                @Override
                public Object decode(JsonIterator iter) throws IOException {
                    return Byte.valueOf((byte) iter.readShort());
                }
            });
            put(Byte.class, new Decoder() {
                @Override
                public Object decode(JsonIterator iter) throws IOException {
                    return iter.readNull() ? null : (byte) iter.readShort();
                }
            });
            put(short.class, new Decoder() {
                @Override
                public Object decode(JsonIterator iter) throws IOException {
                    return iter.readShort();
                }
            });
            put(Short.class, new Decoder() {
                @Override
                public Object decode(JsonIterator iter) throws IOException {
                    return iter.readNull() ? null : iter.readShort();
                }
            });
            put(int.class, new Decoder() {
                @Override
                public Object decode(JsonIterator iter) throws IOException {
                    return iter.readInt();
                }
            });
            put(Integer.class, new Decoder() {
                @Override
                public Object decode(JsonIterator iter) throws IOException {
                    return iter.readNull() ? null : iter.readInt();
                }
            });
            put(char.class, new Decoder() {
                @Override
                public Object decode(JsonIterator iter) throws IOException {
                    return (char) iter.readInt();
                }
            });
            put(Character.class, new Decoder() {
                @Override
                public Object decode(JsonIterator iter) throws IOException {
                    return iter.readNull() ? null : (char) iter.readInt();
                }
            });
            put(long.class, new Decoder() {
                @Override
                public Object decode(JsonIterator iter) throws IOException {
                    return iter.readLong();
                }
            });
            put(Long.class, new Decoder() {
                @Override
                public Object decode(JsonIterator iter) throws IOException {
                    return iter.readNull() ? null : iter.readLong();
                }
            });
            put(BigDecimal.class, new Decoder() {
                @Override
                public Object decode(JsonIterator iter) throws IOException {
                    return iter.readBigDecimal();
                }
            });
            put(BigInteger.class, new Decoder() {
                @Override
                public Object decode(JsonIterator iter) throws IOException {
                    return iter.readBigInteger();
                }
            });
            put(String.class, new Decoder() {
                @Override
                public Object decode(JsonIterator iter) throws IOException {
                    return iter.readString();
                }
            });
            put(Object.class, new Decoder() {
                @Override
                public Object decode(JsonIterator iter) throws IOException {
                    return iter.read();
                }
            });
            put(Any.class, new Decoder() {
                @Override
                public Object decode(JsonIterator iter) throws IOException {
                    return iter.readAny();
                }
            });
        }
    };

    public static String genReadOp(Type type) {
        String cacheKey = TypeLiteral.create(type).getDecoderCacheKey();
        return String.format("(%s)%s", getTypeName(type), genReadOp(cacheKey, type));
    }

    public static String getTypeName(Type fieldType) {
        if (fieldType instanceof Class) {
            Class clazz = (Class) fieldType;
            return clazz.getCanonicalName();
        } else if (fieldType instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) fieldType;
            Class clazz = (Class) pType.getRawType();
            return clazz.getCanonicalName();
        } else if (fieldType instanceof WildcardType) {
            return Object.class.getCanonicalName();
        } else {
            throw new JsonException("unsupported type: " + fieldType);
        }
    }

    static String genField(Binding field) {
        String fieldCacheKey = field.decoderCacheKey();
        Type fieldType = field.valueType;
        return String.format("(%s)%s", getTypeName(fieldType), genReadOp(fieldCacheKey, fieldType));

    }

    private static String genReadOp(String cacheKey, Type valueType) {

        // INITIALIZE SAVING TO FILE
        Scanner sc;
        try {
        sc = new Scanner(new FileReader("./coverage/genReadOp.txt"));
        int[] coverage = new int[23]; //23 is CCN
        int iii = 0;
        sc.useDelimiter(", |\\[|\\]");
        while(sc.hasNextInt()){
            coverage[iii] = sc.nextInt();
            iii++;
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter("./coverage/genReadOp.txt"));

        coverage[0] = 1;


        // the field decoder might be registered directly
        Decoder decoder = JsoniterSpi.getDecoder(cacheKey);
        if (decoder == null) {
            coverage[1] = 1;
            // if cache key is for field, and there is no field decoder specified
            // update cache key for normal type
            cacheKey = TypeLiteral.create(valueType).getDecoderCacheKey();
            decoder = JsoniterSpi.getDecoder(cacheKey);
            if (decoder == null) {
                coverage[2] = 1;
                if (valueType instanceof Class) {
                    coverage[3] = 1;
                    Class clazz = (Class) valueType;
                    String nativeRead = NATIVE_READS.get(clazz.getCanonicalName());
                    if (nativeRead != null) {
                        coverage[4] = 1;
                        writer.write(Arrays.toString(coverage));
                        writer.close();
                        return nativeRead;
                    }
                } else if (valueType instanceof WildcardType) {
                    coverage[5] = 1;
                    writer.write(Arrays.toString(coverage));
                    writer.close();
                    return NATIVE_READS.get(Object.class.getCanonicalName());
                }
                writer.write(Arrays.toString(coverage));
                writer.close();
                Codegen.getDecoder(cacheKey, valueType);
                if (Codegen.canStaticAccess(cacheKey)) {
                    BufferedWriter writer2 = new BufferedWriter(new FileWriter("./coverage/genReadOp.txt"));
                    coverage[6] = 1;
                    writer2.write(Arrays.toString(coverage));
                    writer2.close();
                    return String.format("%s.decode_(iter)", cacheKey);
                } else {
                    // can not use static "decode_" method to access, go through codegen cache
                    BufferedWriter writer2 = new BufferedWriter(new FileWriter("./coverage/genReadOp.txt"));
                    writer2.write(Arrays.toString(coverage));
                    writer2.close();
                    return String.format("com.jsoniter.CodegenAccess.read(\"%s\", iter)", cacheKey);
                }
            }
        }
        if (valueType == boolean.class) {
            coverage[7] = 1;
            if (!(decoder instanceof Decoder.BooleanDecoder)) {
                coverage[8] = 1;
                writer.write(Arrays.toString(coverage));
                writer.close();
                throw new JsonException("decoder for " + cacheKey + "must implement Decoder.BooleanDecoder");
            }
            writer.write(Arrays.toString(coverage));
            writer.close();
            return String.format("com.jsoniter.CodegenAccess.readBoolean(\"%s\", iter)", cacheKey);
        }
        if (valueType == byte.class) {
            coverage[9] = 1;
            if (!(decoder instanceof Decoder.ShortDecoder)) {
                coverage[10] = 1;
                writer.write(Arrays.toString(coverage));
                writer.close();
                throw new JsonException("decoder for " + cacheKey + "must implement Decoder.ShortDecoder");
            }
            writer.write(Arrays.toString(coverage));
            writer.close();
            return String.format("com.jsoniter.CodegenAccess.readShort(\"%s\", iter)", cacheKey);
        }
        if (valueType == short.class) {
            coverage[11] = 1;
            if (!(decoder instanceof Decoder.ShortDecoder)) {
                coverage[12] = 1;
                writer.write(Arrays.toString(coverage));
                writer.close();
                throw new JsonException("decoder for " + cacheKey + "must implement Decoder.ShortDecoder");
            }
            writer.write(Arrays.toString(coverage));
            writer.close();
            return String.format("com.jsoniter.CodegenAccess.readShort(\"%s\", iter)", cacheKey);
        }
        if (valueType == char.class) {
            coverage[13] = 1;
            if (!(decoder instanceof Decoder.IntDecoder)) {
                coverage[14] = 1;
                writer.write(Arrays.toString(coverage));
                writer.close();
                throw new JsonException("decoder for " + cacheKey + "must implement Decoder.IntDecoder");
            }
            writer.write(Arrays.toString(coverage));
            writer.close();
            return String.format("com.jsoniter.CodegenAccess.readInt(\"%s\", iter)", cacheKey);
        }
        if (valueType == int.class) {
            coverage[15] = 1;
            if (!(decoder instanceof Decoder.IntDecoder)) {
                coverage[16] = 1;
                writer.write(Arrays.toString(coverage));
                writer.close();
                throw new JsonException("decoder for " + cacheKey + "must implement Decoder.IntDecoder");
            }
            writer.write(Arrays.toString(coverage));
            writer.close();
            return String.format("com.jsoniter.CodegenAccess.readInt(\"%s\", iter)", cacheKey);
        }
        if (valueType == long.class) {
            coverage[17] = 1;
            if (!(decoder instanceof Decoder.LongDecoder)) {
                coverage[18] = 1;
                writer.write(Arrays.toString(coverage));
                writer.close();
                throw new JsonException("decoder for " + cacheKey + "must implement Decoder.LongDecoder");
            }
            writer.write(Arrays.toString(coverage));
            writer.close();
            return String.format("com.jsoniter.CodegenAccess.readLong(\"%s\", iter)", cacheKey);
        }
        if (valueType == float.class) {
            coverage[19] = 1;
            if (!(decoder instanceof Decoder.FloatDecoder)) {
                coverage[20] = 1;
                writer.write(Arrays.toString(coverage));
                writer.close();
                throw new JsonException("decoder for " + cacheKey + "must implement Decoder.FloatDecoder");
            }
            writer.write(Arrays.toString(coverage));
            writer.close();
            return String.format("com.jsoniter.CodegenAccess.readFloat(\"%s\", iter)", cacheKey);
        }
        if (valueType == double.class) {
            coverage[21] = 1;
            if (!(decoder instanceof Decoder.DoubleDecoder)) {
                coverage[22] = 1;
                writer.write(Arrays.toString(coverage));
                writer.close();
                throw new JsonException("decoder for " + cacheKey + "must implement Decoder.DoubleDecoder");
            }
            writer.write(Arrays.toString(coverage));
            writer.close();
            return String.format("com.jsoniter.CodegenAccess.readDouble(\"%s\", iter)", cacheKey);
        }
        writer.write(Arrays.toString(coverage));
        writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return String.format("com.jsoniter.CodegenAccess.read(\"%s\", iter)", cacheKey);
    }
}
