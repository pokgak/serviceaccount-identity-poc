package com.test.saidentityserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AuthenticationV1Api;
import io.kubernetes.client.openapi.models.V1TokenReview;
import io.kubernetes.client.openapi.models.V1TokenReviewSpec;
import io.kubernetes.client.util.Config;

@RestController
public class SaIdentityServerController {

	Logger logger = LoggerFactory.getLogger(SaIdentityServerController.class);

	@GetMapping("/")
	public String index() {
		return "Greetings from server!";
	}

	@GetMapping("/someResource")
	public Map<String, Object> accessSomeResource(@RequestHeader("X-Client-Id") String token) {
		logger.info("received request at /testToken: {}", token);

		var response = new HashMap<String, Object>();
		response.put("token", token);

		V1TokenReview tokenReview = null;
		try {
			tokenReview = getTokenReview(token);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
		}

		var authenticated = tokenReview.getStatus().getAuthenticated();
		if (authenticated == null || !authenticated) {
			logger.error("not authenticated :(");
			logger.error(tokenReview.toString());
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, tokenReview.getStatus().getError());
		}

		response.put("tokenReviewStatus", tokenReview.getStatus());

		return response;
	}

	private V1TokenReview getTokenReview(String token) throws IOException, ApiException {
		var client = Config.defaultClient();
		var api = new AuthenticationV1Api(client);

		var spec = new V1TokenReviewSpec();
		spec.setToken(token);
		spec.setAudiences(new ArrayList<>(List.of("server")));

		var tokenReview = new V1TokenReview();
		tokenReview.setSpec(spec);
		
		return api.createTokenReview(tokenReview, null, null, null, null);
	}
}
