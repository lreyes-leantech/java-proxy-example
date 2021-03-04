package com.stream.consumer;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.server.Timer;

public class InputStreamConsumer implements Runnable {

	private InputStream input;
	private OutputStream output;
	private Long readTimeOut;
	private Timer timerRead;

	public InputStreamConsumer(InputStream input, OutputStream output) {
		super();
		this.input = input;
		this.output = output;
		this.timerRead = new Timer();
	}

	public InputStreamConsumer(InputStream input, OutputStream output, Long readTimeOut) {
		this(input, output);
		this.readTimeOut = readTimeOut;
	}

	public void consume() {
		try {
			if (this.readTimeOut == null) {
				this.readTimeOut = 300000L;
			}
			boolean availableBytes = false;
			int bytesReaded = 0;
			byte[] bytesBuffer = new byte[1024];

			do {
				try {
					bytesReaded = this.input.read(bytesBuffer);

					// if bytesReaded is -1 then EOF
					if (bytesReaded == -1) {
						System.out.println("EOF has been readed.");
						break;
					}
				} catch (IOException e) {
				}

				try {
					this.output.write(bytesBuffer, 0, bytesReaded);
					this.output.flush();
				} catch (Exception e) {
				}
				
				this.timerRead.schedule(this.readTimeOut);

				try {
					while (!(this.input.available() > 0) && !this.timerRead.isTimeout()) {
						try {
							Thread.sleep(1);
						} catch (Exception e) {
						}
					}
					availableBytes = this.input.available() > 0;
				} catch (IOException e) {
				}
			} while (availableBytes);
		} finally {
		}
	}

	@Override
	public void run() {
		this.consume();
	}
}