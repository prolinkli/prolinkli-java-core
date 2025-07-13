package com.prolinkli.framework.abstractprovider;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import com.prolinkli.framework.abstractprovider.convertor.DateToLocalDateConverter;


import lombok.AllArgsConstructor;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.converter.builtin.DateToStringConverter;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;

public abstract class AbstractProvider<FROM, TO> {

  private final MapperFactory mapperFactory;
  private Class<FROM> fromClass;
  private Class<TO> toClass;

  public AbstractProvider() {
    this.fromClass = getFromClass();
    this.toClass = getToClass();

    mapperFactory = new DefaultMapperFactory.Builder()
        .useBuiltinConverters(true)
        .build();

    mapperFactory.getConverterFactory()
      .registerConverter(new DateToLocalDateConverter());

    ClassMapBuilder<FROM, TO> builder = mapperFactory.classMap(fromClass, toClass);
    builder.byDefault();
    ClassProviderBuilder defineMap = new ClassProviderBuilder(builder);
    defineMap(defineMap);
    builder.register();
  }

  public final TO map(FROM from) {
    return mapperFactory.getMapperFacade().map(from, toClass);
  }

  public final List<TO> mapAll(List<FROM> fromList) {
    return mapperFactory.getMapperFacade().mapAsList(fromList, toClass);
  }

  public final FROM reverseMap(TO to) {
    return mapperFactory.getMapperFacade().map(to, fromClass);
  }

  public final List<FROM> reverseMapAll(List<TO> toList) {
    return mapperFactory.getMapperFacade().mapAsList(toList, fromClass);
  }

  public abstract void defineMap(ClassProviderBuilder mapper);

  @SuppressWarnings("unchecked")
  private Class<FROM> getFromClass() {
    return fromClass == null ? (Class<FROM>) ((ParameterizedType) getClass().getGenericSuperclass())
        .getActualTypeArguments()[0] : fromClass;
  }

  @SuppressWarnings("unchecked")
  private Class<TO> getToClass() {
    return toClass == null ? (Class<TO>) ((ParameterizedType) getClass().getGenericSuperclass())
        .getActualTypeArguments()[1] : toClass;
  }

  @AllArgsConstructor
  public class ClassProviderBuilder {

    private final ClassMapBuilder<FROM, TO> classMapBuilder;

    public ClassProviderBuilder field(String fieldA, String fieldB) {

      // glass is reversed...
      classMapBuilder.field(fieldA, fieldB);

      return this;
    }

  }

}
