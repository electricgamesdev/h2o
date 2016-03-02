package com.safik.hydrogen.flume;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.serialization.EventSerializer;

public class HydroFlumePlugin implements EventSerializer {
	Context context = null;
	OutputStream out = null;

	public HydroFlumePlugin(Context context, OutputStream out) {
		this.context = context;
		this.out = out;

	}

	public void afterCreate() throws IOException {
		System.out.println("************* afterCreate");

	}

	public void afterReopen() throws IOException {
		System.out.println("************* afterReopen");

	}

	public void beforeClose() throws IOException {
		System.out.println("************* beforeClose");

	}

	public void flush() throws IOException {
		System.out.println("************* flush");

	}

	public boolean supportsReopen() {
		System.out.println("************* supportsReopen");
		return false;
	}

	public void write(Event event) throws IOException {
		
		

	}

	public static class Builder implements EventSerializer.Builder {
		public EventSerializer build(Context context, OutputStream out) {
			HydroFlumePlugin s = new HydroFlumePlugin(context, out);
			return s;
		}
	}

}
