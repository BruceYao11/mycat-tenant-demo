package com.cyy.mycat.tenant.intercept;

import java.sql.Connection;
import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

@Intercepts({
		@Signature(method = "query", type = Executor.class, args = {
				MappedStatement.class, Object.class, RowBounds.class,
				ResultHandler.class } ),
		@Signature(method = "prepare", type = StatementHandler.class, args = { Connection.class } ) } )
public class MyInterceptor implements Interceptor {

	private final static String[] scrm_public = {"public_tenant"};

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		String schema = MyCatFilter.getSchema();
		if (invocation.getTarget() instanceof RoutingStatementHandler) {
			RoutingStatementHandler statementHandler = (RoutingStatementHandler) invocation
					.getTarget();
			StatementHandler delegate = (StatementHandler) ReflectHelper
					.getFieldValue(statementHandler, "delegate");
			BoundSql boundSql = delegate.getBoundSql();
			Object obj = boundSql.getParameterObject();
			// 通过反射获取delegate父类BaseStatementHandler的mappedStatement属性
			MappedStatement mappedStatement = (MappedStatement) ReflectHelper
					.getFieldValue(delegate, "mappedStatement");
			// 获取当前要执行的Sql语句，也就是我们直接在Mapper映射语句中写的Sql语句
			String sql = boundSql.getSql();
			String NowSql = "/*!mycat:schema = " + schema + " */  " + sql;
			System.out.println(NowSql);
			// 利用反射设置当前BoundSql对应的sql属性为我们修改完的Sql语句
			ReflectHelper.setFieldValue(boundSql, "sql", NowSql);
		}
		return invocation.proceed();
	}

    @Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {}
}