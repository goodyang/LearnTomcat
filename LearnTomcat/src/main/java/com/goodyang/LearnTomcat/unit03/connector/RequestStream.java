package com.goodyang.LearnTomcat.unit03.connector;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

import org.apache.catalina.util.StringManager;

import com.goodyang.LearnTomcat.unit03.connector.http.Constant;
import com.goodyang.LearnTomcat.unit03.connector.http.HttpRequest;

public class RequestStream extends ServletInputStream {
	
	protected boolean closed = false;
	
	protected int count = 0;
	
	protected int length = -1;
	
	protected static StringManager sm = 
			StringManager.getManager(Constant.Package);
	
	protected InputStream stream = null;
	
	public RequestStream(HttpRequest request) {
		super();
		closed = true;
		count = 0;
		length = request.getContentLength();
		stream = request.getStream();
	}
	
	@Override
	public void close() throws IOException {
		if(closed) throw new IOException(sm.getString("requestStream.close.closed"));
		
		if(length > 0) {
			while(count < length) {
				int b = read();
				if(b < 0) break;
			}
		}
		
		closed = true;
	}
	
	@Override
	public int read() throws IOException {
		if(closed) throw new IOException(sm.getString("requestStream.read.closed"));
		
		if((length >= 0) && (count >= length)) return -1;
		
		int b = stream.read();
		if(b >= 0) count++;
		return (b);
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		return (read(b, 0, b.length));
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int toRead = len;
		if(length > 0) {
			if(count >= length) return -1;
			if((count + len) > length) toRead = length - count;
		}
		int actuallyRead = super.read(b, off, toRead);
		return (actuallyRead);
	}

	@Override
	public boolean isFinished() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setReadListener(ReadListener arg0) {
		// TODO Auto-generated method stub
		
	}
	
	

	
	
}
