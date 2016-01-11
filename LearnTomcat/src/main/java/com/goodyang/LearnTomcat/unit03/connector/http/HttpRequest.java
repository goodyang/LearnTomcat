package com.goodyang.LearnTomcat.unit03.connector.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.ParameterMap;
import org.apache.catalina.util.RequestUtil;

import com.goodyang.LearnTomcat.unit03.connector.RequestStream;
public class HttpRequest implements HttpServletRequest {
	
	private String contentType;
	private int contentLength;
	private InetAddress inetAddress;
	private InputStream input;
	private String method;
	private String protocol;
	private String queryString;
	private String requestURI;
	private String serverName;
	private String serverPort;
	private Socket socket;
	private boolean requestedSessionCookie;
	private String requestedSessionId;
	private boolean requestedSessionURL;
	
	protected HashMap attributes = new HashMap();
	
	protected String authorization = null;
	
	protected String contextPath = "";
	
	protected ArrayList cookies = new ArrayList();
	
	protected static ArrayList empty = new ArrayList();
	
	protected SimpleDateFormat formats[] = {
			new SimpleDateFormat("EEE, dd MMM yyy HH:mm::ss zzz", Locale.US),
			new SimpleDateFormat("EEEEEE, dd-MMM-yyy HH:mm::ss zzz", Locale.US),
			new SimpleDateFormat("EEE MMMM d  HH:mm::ss yyyy", Locale.US)
	};
	
	protected HashMap headers = new HashMap();
	
	protected ParameterMap parameters = null;
	
	protected boolean parsed = false;
	protected String pathInfo = null;
	
	protected BufferedReader reader = null;
	
	protected ServletInputStream stream = null;
	
	public HttpRequest(InputStream input) {
		this.input = input;
	}
	
	public void addHeader(String name, String value) {
		name = name.toLowerCase();
		synchronized (headers) {
			ArrayList values = (ArrayList) headers.get(name);
			if(values == null) {
				values = new ArrayList();
				headers.put(name, values);
			}
			values.add(value);
		}
	}
	
	protected void parseParameters() {
		if(parsed) return;//检测是否已经读取过一次parameters
		
		//parameterMap读取parameter后，lock，即servlet开发者不能修改保存的参数
		ParameterMap results = parameters;
		
		if(results == null) results = new ParameterMap();
		results.setLocked(false);
		
		String encoding = getCharacterEncoding();		
		if(encoding == null) encoding = "ISO-8859-1";
		
		String queryString = getQueryString();
		try {
			RequestUtil.parseParameters(results, queryString, encoding);
		} catch (UnsupportedEncodingException e) {
			;
		}
		
		String contentType = getContentType();
		if(contentType == null) 
			contentType = "";
		int semicolon = contentType.indexOf(';');
		if(semicolon >= 0) {
			contentType = contentType.substring(0, semicolon).trim();
		}else {
			contentType = contentType.trim();
		}
		
		if("POST".equals(getMethod()) && (getContentLength() > 0) && 
				"application/x-www-form-urlencoded".equals(contentType)) {
			try {
				int max = getContentLength();
				int len = 0;
				byte buf[] = new byte[getContentLength()];
				ServletInputStream is = getInputStream();
				while(len < max) {
					int next = is.read(buf, len, max - len);
					if(next < 0) {
						break;
					}
					len += next;
				}
				is.close();
				if(len < max) {
					throw new RuntimeException("Content length mismatch");
				}
				RequestUtil.parseParameters(results, buf, encoding);
			} catch (UnsupportedEncodingException e) {
				;
			} catch (IOException e) {
				throw new RuntimeException("Content read fail");
			}
		}
		
		results.setLocked(true);
		parsed = true;
		parameters = results;
	}
	
	public void addCookie(Cookie cookie) {
		synchronized (cookies) {
			cookies.add(cookie);
		}
	}
	
	public ServletInputStream createInputStream() throws IOException {
		return (new RequestStream(this));
	}
	
	public ServletInputStream getStream() {
		return stream;
	}
	
	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public void setInet(InetAddress inetAddress) {
		this.inetAddress = inetAddress;
	}
	
	public void setContextPath(String contextPath) {
		if(contextPath == null) this.contextPath = "";
		else this.contextPath = contextPath;
	}
	
	public void setMethod(String method) {
		this.method = method;
	}
	
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	
	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}
	
	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}
	
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	
	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}
	
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	public void setRequestedSessionCookie(boolean flag) {
		this.requestedSessionCookie = flag;
	}
	
	public void setRequestedSessionId(String requestedSessionId) {
		this.requestedSessionId = requestedSessionId;
	}
	
	public void setRequestedSessionURL(boolean requestedSessionURL) {
		this.requestedSessionURL = requestedSessionURL;
	}
	
	public Object getAttribute(String name) {
		synchronized(attributes) {
			return (attributes.get(name));
		}
	}
	
	public AsyncContext getAsyncContext() {
		// TODO Auto-generated method stub
		return null;
	}

	public Enumeration<String> getAttributeNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getContentLength() {
		return contentLength;
	}

	public long getContentLengthLong() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getContentType() {
		return contentType;
	}

	public DispatcherType getDispatcherType() {
		// TODO Auto-generated method stub
		return null;
	}

	public ServletInputStream getInputStream() throws IOException {
		if(reader != null) 
			throw new IllegalStateException("getInputStream has been called");
		
		if(stream == null) stream = createInputStream();
		return (stream);
	}

	public String getLocalAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getLocalPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	public Enumeration<Locale> getLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getParameter(String name) {
		parseParameters();
		String values[] = (String[]) parameters.get(name);
		if(values != null) return (values[0]);
		else return null;
	}

	public Map<String, String[]> getParameterMap() {
		parseParameters();
		return this.parameters;
	}

	public Enumeration<String> getParameterNames() {
		parseParameters();
		return (new Enumerator(parameters.keySet()));
	}

	public String[] getParameterValues(String name) {
		parseParameters();
		String values[] = (String[]) parameters.get(name);
		if(values != null) return values;
		else return null;
	}

	public String getProtocol() {
		return protocol;
	}

	public BufferedReader getReader() throws IOException {
		if(stream != null) 
			throw new IllegalStateException("getInputStream has been called");
		if(reader == null) {
			String encoding = getCharacterEncoding();
			if(encoding == null) encoding = "ISO-8859-1";
			InputStreamReader isr = 
					new InputStreamReader(createInputStream(), encoding);
			reader = new BufferedReader(isr);
		}
		return reader;
	}

	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRemoteAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRemoteHost() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getRemotePort() {
		// TODO Auto-generated method stub
		return 0;
	}

	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getScheme() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServerName() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getServerPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isAsyncStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isAsyncSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeAttribute(String arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setAttribute(String arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {		
	}

	public AsyncContext startAsync() throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1)
			throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean authenticate(HttpServletResponse arg0) throws IOException,
			ServletException {
		// TODO Auto-generated method stub
		return false;
	}

	public String changeSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAuthType() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getContextPath() {
		return contextPath;
	}

	public Cookie[] getCookies() {
		synchronized(cookies) {
			if(cookies.size() < 1) return null;
			
			Cookie results[] = new Cookie[cookies.size()];
			return ((Cookie[]) cookies.toArray(results));
		}
	}

	public long getDateHeader(String name) {
		String value = getHeader(name);
		if(value == null) return (-1L);
		
		value += " ";
		
		for(int i=0; i<formats.length; i++) {
			try {
				Date date = formats[i].parse(value);
				return (date.getTime());
			} catch (ParseException e) {
				;
			}
		}
		throw new IllegalArgumentException(value);
	}

	public String getHeader(String name) {
		name = name.toLowerCase();
		synchronized(headers) {
			ArrayList values = (ArrayList) headers.get(name);
			if(values != null) return ((String) values.get(0));
			else return null;
		}
	}

	public Enumeration<String> getHeaderNames() {
		synchronized (headers) {
			return (new Enumerator(headers.keySet()));
		}
	}

	public Enumeration<String> getHeaders(String name) {
		name = name.toLowerCase();
		synchronized (headers) {
			ArrayList values = (ArrayList) headers.get(name);
			if(values != null) return (new Enumerator(values));
			else return (new Enumerator(empty));
		}
	}

	public int getIntHeader(String name) {
		String value = getHeader(name);
		if(value == null) return -1;
		else return Integer.parseInt(value);
	}

	public String getMethod() {
		return method;
	}

	public Part getPart(String arg0) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Part> getParts() throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPathInfo() {
		return pathInfo;
	}

	public String getPathTranslated() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getQueryString() {
		return queryString;
	}

	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRequestURI() {
		return requestURI;
	}

	public StringBuffer getRequestURL() {
		return null;
	}

	public String getRequestedSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServletPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public HttpSession getSession() {
		// TODO Auto-generated method stub
		return null;
	}

	public HttpSession getSession(boolean create) {
		// TODO Auto-generated method stub
		return null;
	}

	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRequestedSessionIdFromURL() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRequestedSessionIdFromUrl() {
		return isRequestedSessionIdFromURL();
	}

	public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isUserInRole(String role) {
		// TODO Auto-generated method stub
		return false;
	}

	public void login(String arg0, String arg1) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	public void logout() throws ServletException {
		// TODO Auto-generated method stub
		
	}

	public <T extends HttpUpgradeHandler> T upgrade(Class<T> arg0)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setAuthorization(String authorization) {
	    this.authorization = authorization;
	}
	
}
