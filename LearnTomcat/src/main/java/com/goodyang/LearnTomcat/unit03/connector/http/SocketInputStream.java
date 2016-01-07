package com.goodyang.LearnTomcat.unit03.connector.http;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.catalina.util.StringManager;

public class SocketInputStream extends InputStream {

	//---------------------------------------------------常量
	
	private static final byte CR = (byte) '\r';
	
	private static final byte LF = (byte) '\n';
	
	private static final byte SP = (byte) ' ';
	
	private static final byte HT = (byte) '\t';
	
	private static final byte COLON = (byte) ':';
	
	/**
	 * Lower case offset
	 */
	private static final byte LC_OFFSET = 'A' - 'a';
	
	protected byte buf[];
	
	protected int count;
	
	/**
	 * position in the buffer
	 */
	protected int pos;
	
	/**
	 * 对象中包裹的input stream
	 */
	protected InputStream is;
	
	
	public SocketInputStream(InputStream is, int bufferSize) {
		this.is = is;
		buf = new byte[bufferSize];
	}
	
	protected static StringManager sm = 
			StringManager.getManager(Constant.Package);
	
	
	//------------------------------------public--------------
	
	public void readRequestLine(HttpRequestLine requestLine) 
			throws IOException{
		if(requestLine.methodEnd != 0) requestLine.recycle();
		
		int chr = 0;
		do{
			try {
				chr = read();
			} catch (IOException e) {
				chr = -1;
			}
		} while ((chr == CR) || (chr == LF));
		
		if(chr == -1) {
			throw new EOFException(sm.getString("requestStream.readline.error"));
		}
		
		pos--;//
		
		int maxRead = requestLine.method.length;
		int readStart = pos;
		int readCount = 0;
		
		boolean space = false;
		
		while(!space) {
			
			//如果buffer已满，可以扩容
			if(readCount >= maxRead) {
				if((2 * maxRead) <= HttpRequestLine.MAX_METHOD_SIZE) {
					char[] newBuffer = new char[2 * maxRead];
					System.arraycopy(requestLine.method, 0, newBuffer, 0, maxRead);
					requestLine.method = newBuffer;
					maxRead  = requestLine.method.length;
				} else {
					throw new IOException
						(sm.getString("requestStream.readline.toolong"));
				}
			}
			
			if(pos >= count) {
				int val = read();
				if(val == -1) {
					throw new IOException
						(sm.getString("requestStream.readline.error"));
				}
				pos = 0;
				readStart = 0;
			}
			
			if(buf[pos] == SP) {
				space = true;
			}
			
			requestLine.method[readCount] = (char)buf[pos];
			readCount++;
			pos++;
		}
		
		requestLine.methodEnd = readCount - 1;
		
		//读取 RUI
		
		maxRead = requestLine.uri.length;
		readStart = pos;
		readCount = 0;
		
		space = false;
		
		boolean eol = false;
		
		while(!space) {
			if(readCount >= maxRead) {
				if((2 * maxRead) <= HttpRequestLine.MAX_URI_SIZE) {
					char[] newBuffer = new char[2 * maxRead];
					System.arraycopy(requestLine.uri, 0, newBuffer, 0, maxRead);
					requestLine.uri = newBuffer;
					maxRead = requestLine.uri.length;
				} else {
					throw new IOException
						(sm.getString("requestStream.readline.toolong"));
				}
			}
			
			if(pos >= count) {
				int val = read();
				if(val == -1) {
					throw new IOException
						(sm.getString("requestStream.readline.error"));
				}
				pos = 0;
				readStart = 0;
			}
			
			if(buf[pos] == SP) {
				space = true;
			}else if((buf[pos] == CR) || (buf[pos] == LF)) {
				eol = true;
				space = true;
			}
			requestLine.uri[readCount] = (char) buf[pos];
			readCount++;
			pos++;
		}
		
		requestLine.uriEnd = readCount - 1;
		
		//Reading Protocol
		
		maxRead = requestLine.protocol.length;
		readStart = pos;
		readCount = 0;
		
		while (!eol) {
            // if the buffer is full, extend it
            if (readCount >= maxRead) {
                if ((2 * maxRead) <= HttpRequestLine.MAX_PROTOCOL_SIZE) {
                    char[] newBuffer = new char[2 * maxRead];
                    System.arraycopy(requestLine.protocol, 0, newBuffer, 0,
                                     maxRead);
                    requestLine.protocol = newBuffer;
                    maxRead = requestLine.protocol.length;
                } else {
                    throw new IOException
                        (sm.getString("requestStream.readline.toolong"));
                }
            }
            // We're at the end of the internal buffer
            if (pos >= count) {
                // Copying part (or all) of the internal buffer to the line
                // buffer
                int val = read();
                if (val == -1)
                    throw new IOException
                        (sm.getString("requestStream.readline.error"));
                pos = 0;
                readStart = 0;
            }
            if (buf[pos] == CR) {
                // Skip CR.
            } else if (buf[pos] == LF) {
                eol = true;
            } else {
                requestLine.protocol[readCount] = (char) buf[pos];
                readCount++;
            }
            pos++;
        }

        requestLine.protocolEnd = readCount;
	}
	
	@Override
	public int read() throws IOException {
		if(pos >= count) {
			fill();
			if(pos >= count) return -1;
		}
		
		return buf[pos++] & 0xff;
	}
	
	public int available() throws IOException {
		return (count - pos) + is.available();
	}
	
	public void close() throws IOException {
		if(is ==null) return;
		is.close();
		is = null;
		buf = null;
	}
	
	protected void fill() throws IOException {
		pos = 0;
		count = 0;
		int nRead = is.read(buf, 0, buf.length);
		if(nRead > 0) {
			count = nRead;
		}
	}
	
}
