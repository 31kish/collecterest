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
import javax.mail.Session;

import models.HttpConnection;

import org.apache.commons.lang.RandomStringUtils;

import sun.misc.BASE64Encoder;

public class LoginForTwitter {

	public static final String AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize";
	private static final String REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
	private static final String ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token";
	private static final String CALLBACK_URL = "http://127.0.0.1:9000/twitterCallback";
	private static final String USER_SHOW_URL = "https://api.twitter.com/1.1/users/show.json";

	private static final String OAUTH_VERSION = "1.0";
	private static final String SIGNATURE_METHOD = "HMAC-SHA1";
	private static final String CONSUMER_KEY = "dqWkYPcxciaNLPtpt2GvqqgbA";
	private static final String CONSUMER_SECRET = "sd0yrorbVrbWnEm3LKof8puDbvqrsUMh9NKjxaSziNaOfpTa47";

//	private String oauthToken;
	private String oauthTokenSecret;
//	private String userId;
//	private String screenName;

	private String header;

	private static HashMap<String, String> responseBody;


	public LoginForTwitter() {
//		oauthToken = "";
		oauthTokenSecret = "";
	}

	public HashMap<String, String> requestAuthorize() {

		SortedMap<String, String> sortedMap = getRequestPrams();
		sortedMap.put("oauth_callback", URLEncode(CALLBACK_URL));

		String authorizationHeader = getAuthorizationHeader(sortedMap, "POST", REQUEST_TOKEN_URL);
		authorizationHeader = "OAuth " + authorizationHeader;

		HttpConnection httpConnection = new HttpConnection();
		httpConnection.connect(REQUEST_TOKEN_URL, "POST", authorizationHeader);

//		HashMap<String, String> body = httpConnection.getResponseBody();
		responseBody = httpConnection.getResponseBody();
//		String redirectURL = AUTHORIZE_URL + "?oauth_token=" + responseBody.get("oauth_token");

//		return redirectURL;
		return httpConnection.getResponseBody();
	}

	public void requestAccessToken(String token, String verifier) {

		String urlString = ACCESS_TOKEN_URL + "?" + "oauth_verifier=" + verifier;

		SortedMap<String, String> sortedMap = getRequestPrams();
		sortedMap.put("oauth_token", token);

		oauthTokenSecret = responseBody.get("oauth_token_secret");
		System.out.println("requestAccessToken : " + oauthTokenSecret);

		String authorizationHeader = getAuthorizationHeader(sortedMap, "POST", REQUEST_TOKEN_URL);
		authorizationHeader = "OAuth " + authorizationHeader;

		HttpConnection httpConnection = new HttpConnection();
		httpConnection.connect(urlString, "POST", authorizationHeader);

		responseBody = httpConnection.getResponseBody();
//		System.out.println(responseBody);

//		requestUserInfo();
	}

	protected void setAuthorizationHeader(String str) {
		header = str;
	}
	protected String getHeader() {
		return header;
	}

	public String requestUserInfo() {
		System.out.println("------ getUserInfo Start ----");
		String screenName = responseBody.get("screen_name");
		String oauth_token = responseBody.get("oauth_token");
		oauthTokenSecret = responseBody.get("oauth_token_secret");

		String url_string = USER_SHOW_URL + "?screen_name=" + URLEncode(screenName);

		SortedMap<String, String> map = getRequestPrams();
		map.put("oauth_token", oauth_token);
		map.put("screen_name", screenName);

		//signature作成
		//パラメーターを連結
		String paramStr = "";
		for(Entry<String, String>param: map.entrySet()) {
			paramStr += "&" + param.getKey() + "=" + param.getValue();
		}
		paramStr = paramStr.substring(1);
		System.out.println("signature:"+paramStr);

		//署名対象テキスト
		String text = "GET" + "&" + URLEncode(USER_SHOW_URL) + "&" + URLEncode(paramStr);
		System.out.println("signingText :"+text);

		//署名キー
		String key = URLEncode(CONSUMER_SECRET) + "&" + URLEncode(oauthTokenSecret);
		System.out.println("signingKey:"+key);

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

		System.out.println("signature:"+signature);

		//screen_nameを除いてauthorizationHeaderを作成
		map.remove("screen_name");
		map.put("oauth_signature", URLEncode(signature));

		//パラメーターを連結
		String authorizeParam = "";
		for(Entry<String, String>param: map.entrySet()) {
			authorizeParam += "," + param.getKey() + "=" + param.getValue();
		}
		authorizeParam = authorizeParam.substring(1);
//		System.out.println("signature:"+authorizeParam);

		authorizeParam = "OAuth " + authorizeParam;

//		String authorizationHeader = getAuthorizationHeader(map, "GET", url_string);
//		authorizationHeader = "OAuth " + authorizationHeader;
		System.out.println(authorizeParam);

		HttpConnection httpConnection = new HttpConnection();
		httpConnection.connect(url_string, "GET", authorizeParam);

		String body = httpConnection.getOriginResponseBody();

		return body;
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
		System.out.println("signingText :"+text);

		//署名キー
		String key = URLEncode(CONSUMER_SECRET) + "&" + URLEncode(oauthTokenSecret);
		System.out.println("signingKey:"+key);

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

		System.out.println("signature:"+signature);

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
