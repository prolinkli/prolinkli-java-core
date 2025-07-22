package com.prolinkli.framework.abstractprovider.convertor;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Date;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;

public class DateToLocalDateConverter extends BidirectionalConverter<LocalDate, Date> {

  @Override
  public Date convertTo(LocalDate source, Type<Date> destinationType, MappingContext mappingContext) {
    if (source == null) {
      return null;
    }
    // Convert DateTime to Date
    return Date.from(source.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant());
  }

  @Override
  public LocalDate convertFrom(Date source, Type<LocalDate> destinationType, MappingContext mappingContext) {
    if (source == null) {
      return null;
    }
    // Convert Date to LocalDate
    try {
      return source.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("Invalid date: " + source, e);
    }
  }

  
}
