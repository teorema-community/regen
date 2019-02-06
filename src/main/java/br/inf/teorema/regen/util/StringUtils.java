package br.inf.teorema.regen.util;

import java.text.Normalizer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StringUtils {

	public static String removerAcentos(String value) {
	    return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
	}
	
	public static String JSONStringify(Object obj) throws JsonProcessingException {
		return new ObjectMapper().writeValueAsString(obj);
	}
	
}
