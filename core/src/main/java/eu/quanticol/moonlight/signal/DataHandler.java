/**
 * 
 */
package eu.quanticol.moonlight.signal;

/**
 * @author loreti
 *
 */
public interface DataHandler<S> {
	
	public final static String REAL_CODE = "real";
	public final static String INT_CODE = "integer";
	public final static String BOOLEAN_CODE = "boolean";
	public final static String STRING_CODE = "string";

	public Class<S> getTypeOf();
	
	public S fromObject( Object value );
	
	public S fromString( String str );

	public boolean checkType(Object o);

	public boolean checkTypeCode(String type);

	public String getTypeCode();
	
	public boolean checkValueFromString(String value);

	
	public static final DataHandler<Double> REAL = new DataHandler<Double>() {

		@Override
		public Class<Double> getTypeOf() {
			return Double.class;
		}

		@Override
		public Double fromObject(Object value) {
			return (Double) value;
		}

		@Override
		public Double fromString(String str) {
			return Double.parseDouble(str);
		}

		@Override
		public boolean checkType(Object o) {
			return (o instanceof Double);
		}

		@Override
		public boolean checkTypeCode(String type) {
			return REAL_CODE.equals(type);
		}

		@Override
		public String getTypeCode() {
			return REAL_CODE;
		}

		@Override
		public boolean checkValueFromString(String value) {
			try {
				Double.parseDouble(value);
				return true;
			} catch (NumberFormatException e) {
				return false;				
			}
		}
	};
	
	public static final DataHandler<Integer> INTEGER = new DataHandler<Integer>() {

		@Override
		public Class<Integer> getTypeOf() {
			return Integer.class;
		}

		@Override
		public Integer fromObject(Object value) {
			return (Integer) value;
		}

		@Override
		public Integer fromString(String str) {
			return Integer.parseInt(str);
		}

		@Override
		public boolean checkType(Object o) {
			return (o instanceof Integer);
		}

		@Override
		public boolean checkTypeCode(String type) {
			return INT_CODE.equals(type);
		}

		@Override
		public String getTypeCode() {
			return INT_CODE;
		}
		
		@Override
		public boolean checkValueFromString(String value) {
			try {
				Integer.parseInt(value);
				return true;
			} catch (NumberFormatException e) {
				return false;				
			}
		}
		
		
	};

	public static final DataHandler<Boolean> BOOLEAN = new DataHandler<Boolean>() {

		@Override
		public Class<Boolean> getTypeOf() {
			return Boolean.class;
		}

		@Override
		public Boolean fromObject(Object value) {
			return (Boolean) value;
		}

		@Override
		public Boolean fromString(String str) {
			return Boolean.parseBoolean(str);
		}

		@Override
		public boolean checkType(Object o) {
			return (o instanceof Boolean);
		}

		@Override
		public boolean checkTypeCode(String type) {
			return BOOLEAN_CODE.equals(type);
		}

		@Override
		public String getTypeCode() {
			return BOOLEAN_CODE;
		}

		@Override
		public boolean checkValueFromString(String value) {
			return "true".equals(value)||"false".equals(value);
		}

		
	};

	public static final DataHandler<String> STRING = new DataHandler<String> () {

		@Override
		public Class<String> getTypeOf() {
			return String.class;
		}

		@Override
		public String fromObject(Object value) {
			return (String) value;
		}

		@Override
		public String fromString(String str) {
			return str;
		}

		@Override
		public boolean checkType(Object o) {
			return (o instanceof String);
		}

		@Override
		public boolean checkTypeCode(String type) {
			return STRING_CODE.equals(type);
		}

		@Override
		public String getTypeCode() {
			return STRING_CODE;
		}

		@Override
		public boolean checkValueFromString(String value) {
			return true;
		}
		
	};

	

}
