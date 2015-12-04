package models.twitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.RandomStringUtils;

import sun.misc.BASE64Encoder;

public class LoginForTwitter {

	private static final String AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize";
	private static final String REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
	private static final String ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token";

	private static final String OAUTH_VERSION = "1.0";
	private static final String SIGNATURE_METHOD = "HMAC-SHA1";
	private static final String CONSUMER_KEY = "dqWkYPcxciaNLPtpt2GvqqgbA";
	private static final String CONSUMER_SECRET = "sd0yrorbVrbWnEm3LKof8puDbvqrsUMh9NKjxaSziNaOfpTa47";
	private String oauthToken;
	private String oauthTokenSecret;

	private String userId;
	private String screenName;

	public LoginForTwitter() {
		oauthToken = "";
		oauthTokenSecret = "";
	}

	public HashMap<String, String> requestAuthorize() {
//		SortedMap<String, String> requestParams = getRequestPrams();

		HttpURLConnection connection = null;

		SortedMap<String, String> sortedMap = getRequestPrams();
		sortedMap.put("oauth_callback", URLEncode("http://127.0.0.1:9000/twitterCallback"));
		String authorizationHeader = getAuthorizationHeader(sortedMap,"POST", REQUEST_TOKEN_URL);
//		authorizationHeader = "oauth_callback=" + URLEncode("http://127.0.0.1:9000/twitterCallback") + "," +
//							authorizationHeader;
//		System.out.println("head="+authorizationHeader);

		String url_string = REQUEST_TOKEN_URL;
//		System.out.println("URL:"+url_string);

		try {
			URL url = new URL(url_string);
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Authorization", "OAuth " + authorizationHeader);
			System.out.println("OAuth "+authorizationHeader);
			connection.connect();
			System.out.println("code:" + connection.getResponseCode() + "  mes:" + connection.getResponseMessage());
			System.out.println("response:" + connection.getHeaderFields());
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				String responseBody = "";
				try (InputStreamReader isr = new InputStreamReader(
						connection.getInputStream(), StandardCharsets.UTF_8);
						BufferedReader reader = new BufferedReader(isr)) {
					String line;
					while((line = reader.readLine()) != null) {
						responseBody = responseBody + line;
					}
				}
				System.out.println(responseBody);
				String[] data = responseBody.split("&");
				for(String string : data) {
					String[] p = string.split("=");
					if(p.length == 2 && p[0].equals("oauth_token")) {
//						login(p[1]);
						oauthToken = p[1];
						System.out.println("oauth_token is " + p[1]);
					}
					else if(p.length == 2 && p[0].equals("oauth_token_secret")) {
						oauthTokenSecret = p[1];
						System.out.println("secret is "+p[1]);
					}
				}
			}
		else {
			System.out.println("failed");
//				flash.error("ログインに失敗しました");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(connection != null) {
				connection.disconnect();
			}
		}

		String redirectURL = AUTHORIZE_URL + "?oauth_token=" + oauthToken;

		HashMap<String, String> allParams = new HashMap<String, String>();
		allParams.put("oauth_token", oauthToken);
		allParams.put("oauth_token_secret", oauthTokenSecret);
		allParams.put("authorizeUrl", redirectURL);
//		allParams.put("oauth_signature_method", requestParams.get("oauth_signature_method"));
//		allParams.put("oauth_timestamp", requestParams.get("oauth_timestamp"));
//		allParams.put("oauth_nonce", requestParams.get("oauth_nonce"));
//		allParams.put("oauth_version", requestParams.get("oauth_version"));

		return allParams;
	}

	public HashMap<String, String> requestAccessToken(String token, String verifier) {

		String url_string = ACCESS_TOKEN_URL + "?" + "oauth_verifier=" + verifier;

		SortedMap<String, String> sortedMap = getRequestPrams();
		sortedMap.put("oauth_token", token);
		String authorizationHeader = getAuthorizationHeader(sortedMap, "POST", REQUEST_TOKEN_URL);

		HttpURLConnection connection = null;
		//TODO JAVA　POST　リクエストで検索
		//query parameterを送る
		try {
			URL url = new URL(url_string);
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Authorization", "OAuth " + authorizationHeader);
			System.out.println("OAuth "+authorizationHeader);
			connection.connect();
			System.out.println("code:" + connection.getResponseCode() + "  mes:" + connection.getResponseMessage());
			System.out.println("response:" + connection.getHeaderFields());
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				String responseBody = "";
				try (InputStreamReader isr = new InputStreamReader(
						connection.getInputStream(), StandardCharsets.UTF_8);
						BufferedReader reader = new BufferedReader(isr)) {
					String line;
					while((line = reader.readLine()) != null) {
						responseBody = responseBody + line;
					}
				}
				System.out.println("responseBody:"+responseBody);
				String[] data = responseBody.split("&");
				for(String string : data) {
					String[] p = string.split("=");
					if(p.length == 2 && p[0].equals("oauth_token")) {
//						login(p[1]);
						oauthToken = p[1];
						System.out.println("oauth_token is " + p[1]);
					}
					else if(p.length == 2 && p[0].equals("oauth_token_secret")) {
						oauthTokenSecret = p[1];
						System.out.println("secret is "+p[1]);
					}
					else if (p.length == 2 && p[0].equals("user_id")) {
						userId = p[1];
						System.out.println("userID is "+p[1]);
					}
					else if (p.length == 2 && p[0].equals("screen_name")) {
						screenName = p[1];
						System.out.println("ScreenName is "+p[1]);
					}
				}
			}
		else {
			System.out.println("failed");
//				flash.error("ログインに失敗しました");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(connection != null) {
				connection.disconnect();
			}
		}


//		HashMap<String, String> userInfo = new HashMap<String, String>();
//		userInfo.put("oauth_token", oauthToken);
//		userInfo.put("oauth_secret", oauthTokenSecret);
//		userInfo.put("user_id", userId);
//		userInfo.put("screen_name", screenName);

		return getUserInfo(sortedMap);
	}

	private HashMap<String, String> getUserInfo(SortedMap<String, String> map) {

		final String USER_SHOW_URL = "https://api.twitter.com/1.1/users/show.json";
		String url_string = USER_SHOW_URL + "?" + "screen_name=" + screenName;
		System.out.println(url_string);

		map.put("oauth_token", oauthToken);

		String authorizationHeader = getAuthorizationHeader(map,"GET", USER_SHOW_URL);

		HttpURLConnection connection = null;
		try {
			URL url = new URL(USER_SHOW_URL);
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Authorization", "OAuth " + authorizationHeader);
			System.out.println("OAuth "+authorizationHeader);
			connection.connect();
			System.out.println("code:" + connection.getResponseCode() + "  mes:" + connection.getResponseMessage());
			System.out.println("response:" + connection.getHeaderFields());
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				String responseBody = "";
				try (InputStreamReader isr = new InputStreamReader(
						connection.getInputStream(), StandardCharsets.UTF_8);
						BufferedReader reader = new BufferedReader(isr)) {
					String line;
					while((line = reader.readLine()) != null) {
						responseBody = responseBody + line;
					}
				}
				System.out.println("responseBody:"+responseBody);
//				String[] data = responseBody.split("&");
//				for(String string : data) {
//					String[] p = string.split("=");
//					if(p.length == 2 && p[0].equals("oauth_token")) {
//						login(p[1]);
//						oauthToken = p[1];
//						System.out.println("oauth_token is " + p[1]);
//					}
//					else if(p.length == 2 && p[0].equals("oauth_token_secret")) {
//						oauthTokenSecret = p[1];
//						System.out.println("secret is "+p[1]);
//					}
//					else if (p.length == 2 && p[0].equals("user_id")) {
//						userId = p[1];
//						System.out.println("userID is "+p[1]);
//					}
//					else if (p.length == 2 && p[0].equals("screen_name")) {
//						screenName = p[1];
//						System.out.println("ScreenName is "+p[1]);
//					}
//				}
			}
		else {
			System.out.println("failed");
//				flash.error("ログインに失敗しました");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(connection != null) {
				connection.disconnect();
			}
		}

		return null;
	}

	private String getAuthorizationHeader(SortedMap<String, String>sortedMap, String method, String url) {
		SortedMap<String, String> params = sortedMap;
		params.put("oauth_signature", getSignature(params, method, url));

		//パラメーターを連結
		String paramStr = "";
		for(Entry<String, String>param: params.entrySet()) {
			paramStr += "," + param.getKey() + "=" + param.getValue();
		}
		paramStr = paramStr.substring(1);
//		System.out.println("signature:"+paramStr);

		return paramStr;
	}

	private SortedMap<String, String> getRequestPrams() {
		SortedMap<String, String> params = new TreeMap<String, String>();
		params.put("oauth_consumer_key", CONSUMER_KEY);
		params.put("oauth_signature_method", SIGNATURE_METHOD);
		params.put("oauth_timestamp", getTimeStamp());
		params.put("oauth_nonce", getNonce());
		params.put("oauth_version", OAUTH_VERSION);
//		params.put("oauth_callback", URLEncode("http://127.0.0.1:9000/twitterCallback"));
		return params;
	}

	private String getTimeStamp() {
		return Long.toString(System.currentTimeMillis() / 1000);
	}

	private String getSignature(SortedMap<String, String>params, String method, String url) {
		//パラメーターを連結
		String paramStr = "";
		for(Entry<String, String>param: params.entrySet()) {
			paramStr += "&" + param.getKey() + "=" + param.getValue();
		}
		paramStr = paramStr.substring(1);
//		System.out.println("signature:"+paramStr);

		//署名対象テキスト
		String text = method + "&" + URLEncode(url) + "&" + URLEncode(paramStr);
//		System.out.println("signingText :"+text);

		//署名キー
		String key = URLEncode(CONSUMER_SECRET) + "&" + URLEncode(oauthTokenSecret);
//		System.out.println("signingKey:"+key);

		SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA1");

		Mac mac = null;
		try {
			mac = Mac.getInstance(signingKey.getAlgorithm());
			mac.init(signingKey);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		byte[] rawHmac = mac.doFinal(text.getBytes());

		String signature = new BASE64Encoder().encode(rawHmac);

//		System.out.println("signature:"+signature);

		return URLEncode(signature);
	}

	private String getNonce() {
		String nonce = RandomStringUtils.randomAlphanumeric(16);
		return nonce;
//		return new String(nonce);
	}

	private String URLEncode(String beforeEncode) {
		String afterEncode;
		try {
			beforeEncode = beforeEncode.replace(" ", "&nbsp;");
			afterEncode = URLEncoder.encode(beforeEncode,"UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		return afterEncode;
	}
 }
