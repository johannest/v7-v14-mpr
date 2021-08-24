package org.vaadin;

import com.vaadin.mpr.MprUI;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.ui.UI;

public class OldUI extends MprUI {

	String hello = "Hello";

	public String getHello() {
		return hello;
	}

	public OldUI() {
		System.out.println("**** OLD UI ****");
	}
}
