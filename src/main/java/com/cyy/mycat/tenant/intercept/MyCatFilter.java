package com.cyy.mycat.tenant.intercept;

import org.springframework.util.StringUtils;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class MyCatFilter implements Filter {

	private static final ThreadLocal<String> SCHEMA_LOCAL = new ThreadLocal<>();
	private static final ThreadLocal<String> TENANT_ID_LOCAL = new ThreadLocal<>();

	public static String getSchema() {
		String schema = SCHEMA_LOCAL.get();
		if (!StringUtils.isEmpty(schema)) {
			return schema;
		} else {
			return "";
		}
	}

	public static String getTenantId() {
		String tenantId = TENANT_ID_LOCAL.get();
		if (!StringUtils.isEmpty(tenantId)) {
			return tenantId;
		} else {
			return "";
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		try {
			HttpServletRequest req = (HttpServletRequest) request;
			String tenantId = req.getParameter("tenant_id");
			//根据租户id查询租户对应的数据库
			if (!StringUtils.isEmpty(tenantId)) {
				// 静态的本地线程变量来存储租户信息
				TENANT_ID_LOCAL.set(tenantId);
				// TODO 根据租户id查询schema逻辑数据库  从redis或者数据库中查询
				String schema = null;
				// 静态的本地线程变量来存储数据库信息
				SCHEMA_LOCAL.set(schema);
				// chain
				chain.doFilter(request, response);
			} else {
				chain.doFilter(request, response);
			}
		} finally {
			SCHEMA_LOCAL.remove();
		}
	}

	@Override
	public void destroy() {}
}
