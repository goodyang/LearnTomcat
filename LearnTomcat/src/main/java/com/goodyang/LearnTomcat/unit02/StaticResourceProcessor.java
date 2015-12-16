package com.goodyang.LearnTomcat.unit02;

import java.io.IOException;

public class StaticResourceProcessor {
	public void process(Request request, Response response) {
		try {
			response.SendStaticResource();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
