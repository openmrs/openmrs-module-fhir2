package org.openmrs.module.fhir2.web.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;

public class AuthenticationFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		// skip if the session has timed out, we're already authenticated, or it's not an HTTP request
		if (request instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			if (httpRequest.getRequestedSessionId() != null && !httpRequest.isRequestedSessionIdValid()) {
				HttpServletResponse httpResponse = (HttpServletResponse) response;
				httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Session timed out");
				return;
			}

			if (!Context.isAuthenticated()) {
				String basicAuth = httpRequest.getHeader("Authorization");
				if (!StringUtils.isBlank(basicAuth)) {
					// this is "Basic ${base64encode(username + ":" + password)}"
					try {
						basicAuth = basicAuth.substring(6); // remove the leading "Basic "
						String decoded = new String(Base64.decodeBase64(basicAuth), StandardCharsets.UTF_8);
						String[] userAndPass = decoded.split(":");
						Context.authenticate(userAndPass[0], userAndPass[1]);
					}
					catch (Exception ignored) {
						HttpServletResponse httpResponse = (HttpServletResponse) response;
						httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated");
					}
				} else {
					// This sends 401 error if not authenticated
					HttpServletResponse httpResponse = (HttpServletResponse) response;
					httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated");
					return;
				}
			}
		}

		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}
}
