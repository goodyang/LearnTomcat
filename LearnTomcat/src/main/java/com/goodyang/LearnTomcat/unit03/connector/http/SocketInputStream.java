package com.goodyang.LearnTomcat.unit03.connector.http;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.catalina.util.StringManager;

public class SocketInputStream extends InputStream {

	//---------------------------------------------------常量
	/**
	 * 回车符
	 */
	private static final byte CR = (byte) '\r';
	
	/**
	 * 换行符
	 */
	private static final byte LF = (byte) '\n';
	
	private static final byte SP = (byte) ' ';
	
	/**
	 *制表符 
	 */
	private static final byte HT = (byte) '\t';
	
	/**
	 * 冒号
	 */
	private static final byte COLON = (byte) ':';
	
	/**
	 * Lower case offset
	 */
	private static final byte LC_OFFSET = 'A' - 'a';
	
	protected byte buf[];
	
	/**
	 * 当前读取到buf中的byte长度
	 */
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
		if(requestLine.methodEnd != 0) requestLine.recycle();//重置再利用
		
		int chr = 0;
		
		//跳过空白字符
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
		
		pos--;//检测完之后，需要后退一个位置
		
		int maxRead = requestLine.method.length;
		int readStart = pos;
		int readCount = 0;//记录读取长度
		
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
			
			//已空格分隔
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
	
	public void readHeader(HttpHeader header) throws IOException {
		
		if(header.nameEnd != 0) header.recycle();
		
		int chr = read();
		if((chr == CR) || (chr == LF)) {
			if(chr == CR) read();
			header.nameEnd = 0;
			header.valueEnd = 0;
			return;
		} else {
			pos--;
		}
		
		int maxRead = header.name.length;
		int readStart = pos;
		int readCount = 0;
		
		boolean colon = false;
		
		while(!colon) {
			if(readCount >= maxRead) {
				if((2*maxRead) <= HttpHeader.MAX_NAME_SIZE) {
					char[] newBuffer = new char[2 * maxRead];
					System.arraycopy(header.name, 0, newBuffer, 0, maxRead);
					header.name = newBuffer;
					maxRead = header.name.length;
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
			
			if(buf[pos] == COLON) {
				colon = true;
			}
			
			char val = (char) buf[pos];
			if((val >= 'A') && (val <= 'Z')) {
				val = (char) (val - LC_OFFSET);
			}
			header.name[readCount] = val;
			readCount++;
			pos++;
		}
		
		header.nameEnd = readCount - 1;
		
		maxRead = header.value.length;
		readStart = pos;
		readCount = 0;
		
		int crPos = -2;
		
		boolean eol = false;
		boolean validLine = true;
		
		while(validLine) {
			boolean space = true;
			
			while(space) {
				if(pos >= count) {
					int val = read();
					if(val == -1) {
						throw new IOException
							(sm.getString("requestStream.readline.error"));
					}
					pos = 0;
					readStart = 0;
				}
				if((buf[pos] == SP) || (buf[pos] == HT)) {
					pos++;
				}else {
					space = false;
				}
			}
			
			while(!eol) {
				if (readCount >= maxRead) {
                    if ((2 * maxRead) <= HttpHeader.MAX_VALUE_SIZE) {
                        char[] newBuffer = new char[2 * maxRead];
                        System.arraycopy(header.value, 0, newBuffer, 0,
                                         maxRead);
                        header.value = newBuffer;
                        maxRead = header.value.length;
                    } else {
                        throw new IOException
                            (sm.getString("requestStream.readline.toolong"));
                    }
                }
				
				if(pos >= count) {
					int val = read();
                    if (val == -1)
                        throw new IOException
                            (sm.getString("requestStream.readline.error"));
                    pos = 0;
                    readStart = 0;
				}
				if(buf[pos] == CR) {
				}else if(buf[pos] == LF) {
					eol = true;
				}else {
					int ch = buf[pos] & 0xff;
					header.value[readCount] = (char) ch;
					readCount++;
				}
				pos++;
			}
			
			int nextChr = read();
			
			if((nextChr != SP) && (nextChr != HT)) {
				pos--;
				validLine = false;
			} else {
				eol = false;
				if(readCount >= maxRead) {
					if ((2 * maxRead) <= HttpHeader.MAX_VALUE_SIZE) {
                        char[] newBuffer = new char[2 * maxRead];
                        System.arraycopy(header.value, 0, newBuffer, 0,
                                         maxRead);
                        header.value = newBuffer;
                        maxRead = header.value.length;
                    } else {
                        throw new IOException
                            (sm.getString("requestStream.readline.toolong"));
                    }
				}
				header.value[readCount] = ' ';
				readCount++;
			}
		}
		
		header.valueEnd = readCount;
	}
	
	@Override
	public int read() throws IOException {
		if(pos >= count) {
			fill();//读取一次，重新填满
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
	
	/**
	 * buf中字符解析完后，再从input中读取一次
	 * @throws IOException
	 */
	protected void fill() throws IOException {
		pos = 0;
		count = 0;
		int nRead = is.read(buf, 0, buf.length);
		if(nRead > 0) {
			count = nRead;
		}
	}
	
}
