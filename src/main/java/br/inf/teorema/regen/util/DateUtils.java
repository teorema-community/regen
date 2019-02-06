package br.inf.teorema.regen.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

	private static final String DEFAULT_JSON_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	
	public static Date parseDate(String str) throws ParseException {
		return new SimpleDateFormat(DEFAULT_JSON_FORMAT).parse(str);
	}
	
}
