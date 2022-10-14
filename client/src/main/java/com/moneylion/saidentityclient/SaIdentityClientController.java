package com.test.saidentityclient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SaIdentityClientController {

	private String token = "";

	Logger logger = LoggerFactory.getLogger(SaIdentityClientController.class);

	@GetMapping("/")
	public String index() {
		logger.info("received request at /");

		return "Greetings from client!";
	}

	@GetMapping("/refreshToken")
	public String loadToken() {
		logger.info("received request at /refreshToken");

		try {
			token = new String(Files.readAllBytes(Paths.get("/var/run/secrets/tokens/client-token")));
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
		}

		logger.info(token);

		return token;
	}

	@GetMapping("/sendRequestToServer")
	public String sendRequestToServer() {
		logger.info("received request at /sendRequestToServer");

		try {
			URL url = new URL(System.getenv("SERVER_CONNSTRING"));
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("X-Client-Id", token);
			con.setConnectTimeout(1000);
			con.setReadTimeout(1000);

			Reader streamReader = null;
			int status = con.getResponseCode();
			logger.info("status: " + status);
			if (status > 299) {
				streamReader = new InputStreamReader(con.getErrorStream());
			} else {
				streamReader = new InputStreamReader(con.getInputStream());
			}

			BufferedReader in = new BufferedReader(streamReader);
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			con.disconnect();

			logger.info("Server response:\n" + content);

			return "Server response:\n" + content;
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
		}

		return "Empty";
	}
}
