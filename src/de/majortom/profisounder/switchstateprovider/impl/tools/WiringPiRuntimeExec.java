package de.majortom.profisounder.switchstateprovider.impl.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class WiringPiRuntimeExec {
	private static final String GPIO_CMD = "/usr/local/bin/gpio";

	public boolean testRuntime() throws IOException {
		executeCommandIntRet(false, GPIO_CMD, "readall");
		return true;
	}

	public int readPin(int pinNumber) throws IOException {
		executeCommandIntRet(false, GPIO_CMD, "mode", Integer.toString(pinNumber), "in");
		return executeCommandIntRet(true, GPIO_CMD, "read", Integer.toString(pinNumber));
	}

	public void writePin(int pinNumber, boolean high) throws IOException {
		executeCommandIntRet(false, GPIO_CMD, "mode", Integer.toString(pinNumber), "out");
		executeCommandIntRet(true, GPIO_CMD, "write", Integer.toString(pinNumber), high ? "1" : "0");
	}

	private int executeCommandIntRet(boolean parseResult, String... args) throws IOException {
		try {
			ProcessBuilder pb = new ProcessBuilder(args);
			pb.redirectErrorStream(true);
			Process exec = pb.start();

			String result = "";
			try (BufferedReader br = new BufferedReader(new InputStreamReader(exec.getInputStream()))) {
				String line;
				while ((line = br.readLine()) != null)
					result += line;
			}

			int res = exec.waitFor();
			if (res != 0)
				throw new IOException("Process ended abnormally with result code " + res);

			if (!parseResult)
				return -1;
			else
				return Integer.parseInt(result);
		} catch (Exception ex) {
			throw new IOException(ex.getMessage());
		}
	}
}
