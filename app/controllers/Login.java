package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import models.User;
import models.facebook.FacebookUserObject;
import models.facebook.UserFacebookInfo;
import models.twitter.LoginForTwitter;
import models.twitter.TwitterUserObject;
import play.mvc.Controller;

import com.google.gson.Gson;

public class Login extends Controller {

	public static void loginViaFacebook(String code) {
		System.out.println("return code = "+ code);

		final String FACEBOOK_AUTH_URL = "https://graph.facebook.com/oauth/access_token?";
		final String APP_ID = "816412851817516";
		final String REDIRECT_URI = "http://localhost:9000/loginViaFacebook";
		final String CLIENT_SECRET = "fbaadbcc3e2dc4ae563dc66c18baba40";

		String url_string = FACEBOOK_AUTH_URL + "&client_id=" + APP_ID
				+ "&redirect_uri=" + REDIRECT_URI + "&client_secret="
				+ CLIENT_SECRET + "&code=" + code;

		System.out.println("url string = "+ url_string);

		HttpURLConnection connection = null;
		try {
			URL url = new URL(url_string);

			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");

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
					if(p.length == 2 && p[0].equals("access_token")) {
						login(p[1]);
						System.out.println("access token is " + p[1]);
					}
				}
			} else {
				flash.error("ログインに失敗しました");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(connection != null) {
				connection.disconnect();
			}
		}
//		Application.index();
		Application.loginSucceed();
	}

	public static void loginViaTwitter() {
		LoginForTwitter twitter = new LoginForTwitter();
//		redirect(twitter.request());

		HashMap<String, String> token = twitter.requestAuthorize();
		String url = token.get("authorizeUrl");
		redirect(url);
	}

	public static void twitterCallback(String oauth_token, String oauth_verifier) {

		System.out.println("Im home:D token:" + oauth_token + "\n verifier:" +oauth_verifier);

		LoginForTwitter twitter = new LoginForTwitter();
		HashMap<String, String> userInfo = twitter.requestAccessToken(oauth_token,oauth_verifier);
//		params.put("oautu_token" oauth_token);
		System.out.println("COME!!! " + userInfo);
	}

	//TODO:使ってない
	private static void loginForTwitter(HashMap<String, String> userInfo) {
		session.put("oauth_token", userInfo.get("oauth_token"));

		final String USER_SHOW_URL = "https://api.twitter.com/1.1/users/show.json";
		String url = USER_SHOW_URL + "?" + "screen_name=" + userInfo.get("screen_name");
		System.out.println(url);

		String json = getJson(USER_SHOW_URL + "?screen_name=" + userInfo.get("screen_name"));
//		System.out.println(json);
		TwitterUserObject twObject = new Gson().fromJson(json, TwitterUserObject.class);
//		System.out.println(twObject);
	}

	private static void login(String accessToken) {
		//セッションにプットする（ことで毎回のリクエストに利用可能にする）
		session.put("access_token", accessToken);

		//ユーザー情報を確認し、なければ作成
		//まずはfacebookのユーザーIDを取得
		final String URL_ME = "https://graph.facebook.com/v2.5/me?fields=id,name,picture.type(large)&access_token=";
		String json = getJson(URL_ME + accessToken);
		FacebookUserObject fbObject = new Gson().fromJson(json,
				FacebookUserObject.class);
		System.out.println("id : "+ fbObject.id);
		System.out.println("image: "+fbObject.picture.data.url);

		if(!session.contains("userId") || (fbObject != null && fbObject.id != null && fbObject.id.equals(""))){
			//もしユーザーのfacebook情報が見つからなかったらアカウントを作成する
			UserFacebookInfo facebookInfo = UserFacebookInfo.findbyFacebookId(fbObject.id);
			if(facebookInfo == null) {
				User user = new User();
				UserFacebookInfo newFacebookInfo = new UserFacebookInfo();
				user.name = fbObject.name;
				user.iconUrl = fbObject.picture.data.url;
				user.save();

				newFacebookInfo.userId = user.id;
				newFacebookInfo.facebookId = fbObject.id;
				newFacebookInfo.name = fbObject.name;
				newFacebookInfo.save();

				session.put("userId", user.id);
			} else {
				session.put("userId", facebookInfo.userId);
			}
		}

	}

	public static void logout() {
		session.clear();
		Application.index();
	}

	private static String getJson(String urlString) {
		HttpURLConnection connection = null;
		try {
			URL url = new URL(urlString);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				String responseBody = "";
				try(InputStreamReader isr = new InputStreamReader(
						connection.getInputStream(), StandardCharsets.UTF_8);
						BufferedReader reader = new BufferedReader(isr)) {
					String line;
					while((line = reader.readLine()) != null) {
						responseBody = responseBody + line;
					}
					return responseBody;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(connection != null) {
				connection.disconnect();
			}
		}
		return null;
	}
}
