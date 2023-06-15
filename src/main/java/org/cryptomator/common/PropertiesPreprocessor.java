package org.cryptomator.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Pattern;

public class PropertiesPreprocessor {

	private static final Logger LOG = LoggerFactory.getLogger(PropertiesPreprocessor.class);
	private static final Pattern TEMPLATE = Pattern.compile("@\\((.+)\\)");
	private static final LoggingEnvironment ENV = new LoggingEnvironment(System.getenv(), LOG);

	public static void run() {
		var properties = System.getProperties();
		properties.stringPropertyNames().stream() //
				.filter(s -> s.startsWith("cryptomator.")) //
				.forEach(key -> {
					var value = properties.getProperty(key);
					var newValue = process(value);
					if(! value.equals(newValue)) {
						LOG.info("Changed property {} from {} to {}.", key, value, newValue);
					}
					properties.setProperty(key, newValue);
				});
		LOG.info("Preprocessed cryptomator properties.");
	}

	private static String process(String value) {
		return TEMPLATE.matcher(value).replaceAll(match -> //
				switch (match.group().substring(2, match.group().length() - 1)) {
					case "appdir" -> ENV.get("APPDIR");
					case "appdata" -> ENV.get("APPDATA");
					case "userhome" -> System.getProperty("user.home");
					default -> {
						LOG.warn("Found unknown keyword {} in property value {}. Keyword is not replaced.", match.group(), value);
						yield match.group();
					}
				});
	}

	private static class LoggingEnvironment {

		private final Map<String, String> env;
		private final Logger logger;

		LoggingEnvironment(Map<String, String> env, Logger logger) {
			this.env = env;
			this.logger = logger;
		}

		String get(String key) {
			var val = env.get(key);
			if (val == null) {
				logger.warn("Variable {} used for substitution not found in environment", key);
				return "";
			} else {
				return val;
			}
		}

	}


}
