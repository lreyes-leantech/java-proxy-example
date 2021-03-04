package com.interfaces;

import com.types.HttpRequestMethodType;

public interface Headers {
	public String getHost();
	public HttpRequestMethodType getRequestMethodEnum();
	public Integer getPort();
}
