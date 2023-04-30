package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
	private static class Event {
		private boolean fired = false;
		void await() throws InterruptedException {
			synchronized (this) {
				while (!fired) {
					wait();
				}
			}
		}

		void fire() {
			synchronized (this) {
				if (fired) {
					return;
				}
				fired = true;
				notifyAll();
			}
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
		Event ev = new Event();
		Runtime.getRuntime().addShutdownHook(new Thread(ev::fire));
		try {
			ev.await();
		} catch (InterruptedException ignored) {
		}
	}

}
