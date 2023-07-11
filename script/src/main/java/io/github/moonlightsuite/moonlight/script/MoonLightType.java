package io.github.moonlightsuite.moonlight.script;

import java.util.Objects;

public class MoonLightType {

    public static final int NONE_CODE = -1;
    public static final int BOOLEAN_CODE = 0;
    public static final int INT_CODE = 1;
    public static final int REAL_CODE = 2;
    public static final int CUSTOM_CODE = 3;

    public static final String NONE_VALUE = "none";
    public static final String BOOLEAN_VALUE = "boolean";
    public static final String INT_VALUE = "int";
    public static final String REAL_VALUE = "real";

    private final int code;
    private final String value;

    private MoonLightType(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public static MoonLightType typeOf(String value) {
        if ("int".equals(value)) {
            return MoonLightType.INT;
        }
        if ("real".equals(value)) {
            return MoonLightType.REAL;
        }
        if ("bool".equals(value)) {
            return MoonLightType.BOOLEAN;
        }
        return MoonLightType.customType(value);
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MoonLightType that = (MoonLightType) o;
        if (getCode()<=3) {
            return getCode()==that.getCode();
        }
        return getCode() == that.getCode() && Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCode(), getValue());
    }

    @Override
    public String toString() {
        return value;
    }

    public static final MoonLightType NONE = new MoonLightType(NONE_CODE, NONE_VALUE);

    public static final MoonLightType BOOLEAN = new MoonLightType(BOOLEAN_CODE, BOOLEAN_VALUE);

    public static final MoonLightType REAL = new MoonLightType(REAL_CODE, REAL_VALUE);

    public static final MoonLightType INT = new MoonLightType(INT_CODE, INT_VALUE);

    public static MoonLightType customType(String name) {
        return new MoonLightType(CUSTOM_CODE,name);
    }

    public boolean isNone() {
        return getCode()== NONE_CODE;
    }

    public boolean isANumber() {
        return isInteger()||isReal();
    }

    public boolean isInteger() {
        return getCode()== INT_CODE;
    }

    public boolean isReal() {
        return getCode()== REAL_CODE;
    }

    public boolean isBoolean() {
        return getCode()== BOOLEAN_CODE;
    }

    public static MoonLightType mix(MoonLightType mType1, MoonLightType mType2) {
        if (mType1.equals(mType2)) {
            return mType1;
        }
        if (mType1.isANumber()&&mType2.isANumber()) {
            return REAL;
        }
        return NONE;
    }

    public boolean isCompatible(MoonLightType type) {
        return this.equals(type)||(this.isReal()&&type.isANumber());
    }

    public boolean canBeComparedWith(String text, MoonLightType other) {
        if (!this.isANumber()) {
            if (text.equals("==")) {
                return this.equals(other);
            } else {
                return false;
            }
        } else {
            return other.isANumber();
        }
    }
}
