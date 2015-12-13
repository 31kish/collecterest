package models.facebook;

import java.util.HashMap;

import models.HttpConnection;

public class LoginForFacebook {

	public HashMap<String, String> request(String code) {

		final String FACEBOOK_AUTH_URL = "https://graph.facebook.com/oauth/access_token?";
		final String APP_ID = "816412851817516";
		final String REDIRECT_URI = "http://localhost:9000/loginViaFacebook";
		final String CLIENT_SECRET = "fbaadbcc3e2dc4ae563dc66c18baba40";

		String url_string = FACEBOOK_AUTH_URL + "&client_id=" + APP_ID
				+ "&redirect_uri=" + REDIRECT_URI + "&client_secret="
				+ CLIENT_SECRET + "&code=" + code;

		HttpConnection httpConnection = new HttpConnection();
		httpConnection.connect(url_string, "GET", null);

		return httpConnection.getResponseBody();
	}
}
