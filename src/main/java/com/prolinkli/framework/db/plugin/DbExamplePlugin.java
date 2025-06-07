package com.prolinkli.framework.db.plugin;

import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

import java.util.List;

public class DbExamplePlugin extends PluginAdapter {

	public DbExamplePlugin() {
	}

	@Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable) {
		String modelName = introspectedTable.getBaseRecordType();
		FullyQualifiedJavaType superClass = new FullyQualifiedJavaType(
				"com.prolinkli.framework.db.base.DbExample<" + modelName + ">");
		topLevelClass.setSuperClass(superClass);
		return true;
	}

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}
}
