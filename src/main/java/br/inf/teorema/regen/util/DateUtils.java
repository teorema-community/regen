package br.inf.teorema.regen.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

	private static final String DEFAULT_JSON_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	
	public static Date parseDate(String str) throws ParseException {
		return new SimpleDateFormat(DEFAULT_JSON_FORMAT).parse(str);
	}

	public static Date getDateValue(Object value) throws ParseException {
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
