package com.prolinkli.framework.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * LocalDateUtil
 */
public class LocalDateUtil {

	public static Date toDate(LocalDate localDate) {
		if (localDate == null) {
			return null;
		}
		return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
	}

	public static LocalDate toLocalDate(Date date) {
		if (date == null) {
			return null;
		}
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

}
