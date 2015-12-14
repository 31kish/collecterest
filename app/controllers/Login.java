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
import models.facebook.LoginForFacebook;
import models.facebook.UserFacebookInfo;
import models.twitter.LoginForTwitter;
import models.twitter.TwitterUserObject;
import models.twitter.UserTwitterInfo;
import play.mvc.Controller;

import com.google.gson.Gson;
import com.sun.xml.internal.bind.v2.model.core.ID;

public class Login extends Controller {

	public static void loginViaFacebook(String code) {
		LoginForFacebook facebook = new LoginForFacebook();
		HashMap<String, String> fbHashMap = facebook.request(code);
		String token = fbHashMap.get("access_token");
		login_fb(token);
		Application.loginSucceed();
	}

	public static void loginViaTwitter() {
		LoginForTwitter twitter = new LoginForTwitter();

		HashMap<String, String> token = twitter.requestAuthorize();
		String redirectURL = twitter.AUTHORIZE_URL + "?oauth_token=" + token.get("oauth_token");

		redirect(redirectURL);
	}

	public static void twitterCallback(String oauth_token, String oauth_verifier) {

		System.out.println("Im home:D token:" + oauth_token + "\n verifier:" +oauth_verifier);

		LoginForTwitter twitter = new LoginForTwitter();
		twitter.requestAccessToken(oauth_token, oauth_verifier);

		login_tw(oauth_token);
		Application.loginSucceed();
	}

	private static void login_tw(String oauth_token) {
		session.put("oauth_token", oauth_token);

		LoginForTwitter twitter = new LoginForTwitter();
		String json = twitter.requestUserInfo();
		TwitterUserObject twObject = new Gson().fromJson(json, TwitterUserObject.class);
		System.out.println("id : " + twObject.id);
		System.out.println("screen_name : " + twObject.screen_name);
		System.out.println("profile_image_url : " + twObject.profile_image_url);

		if(!session.contains("userId") || (twObject != null && twObject.id != null && twObject.id.equals(""))) {
			//
//			System.out.println("ユーザーを作成");
			UserTwitterInfo twitterInfo = UserTwitterInfo.findbyTwitterId(twObject.id);
			if(twitterInfo == null) {
				User user = new User();
				UserTwitterInfo newTwitterInfo = new UserTwitterInfo();
				user.name = twObject.screen_name;
				user.iconUrl = twObject.profile_image_url;
				user.save();

				newTwitterInfo.userId = user.id;
				newTwitterInfo.twitterId = twObject.id;
				newTwitterInfo.name = twObject.screen_name;
				newTwitterInfo.save();

				session.put("userId", user.id);
			}
			else {
				session.put("userId", twitterInfo.userId);
			}
		}
	}

	private static void login_fb(String accessToken) {
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
