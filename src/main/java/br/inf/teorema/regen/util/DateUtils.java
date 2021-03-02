package br.inf.teorema.regen.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

	private static final String DEFAULT_JSON_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	
	public static Object parseDate(String str) {
		try {
			return new SimpleDateFormat(DEFAULT_JSON_FORMAT).parse(str);
		} catch (ParseException e) {
			System.out.println("Campo " + str + " não está em formato JSON");
			return str;
		}
	}

	public static Object getDateValue(Object value) {
		if (value != null) {
			if (value instanceof Date) {
				return (Date) value;
			} else {
				Long longValue = ObjectUtils.getLongOrNull(value);

				if (longValue != null) {
					return new Date(longValue);
				} else {
					return parseDate(value.toString());
				}
			}
		}

		return null;
	}
	
}
