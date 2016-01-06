package com.goodyang.LearnTomcat.unit03.startup;

import com.goodyang.LearnTomcat.unit03.connector.http.HttpConnector;

public final class BootStrap {
	public static void main(String[] args) {
		HttpConnector connector = new HttpConnector();
		connector.start();
	}
}
