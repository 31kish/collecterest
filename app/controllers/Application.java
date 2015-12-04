package controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import models.Article;
import models.HtmlParser;
import models.User;
import play.mvc.Controller;
import sun.misc.BASE64Encoder;

public class Application extends Controller {

	public static boolean isConnected() {
		return session.contains("userId");
	}

	public static void welcome() {
		if(isConnected()) {
			index();
		}
		else {
			renderTemplate("Application/welcome.html");
		}
	}

	public static void index() {

		User user = null;
		if (isConnected()) {
			user = User.findById(session.get("userId"));
			List<Article> articles = user.postedArticle.find("order by id desc").fetch();
//			List<Article> articles = Article.find("order by id desc").fetch();
			renderTemplate("Application/index.html",user,articles);
		}
		else {
			welcome();
		}
	}

	public static void signup() throws Exception {

		final String fbUrl = getFbUrl();
		final String twUrl = getTwUrl();

		if (isConnected()) {
			index();
		}
		else {
			renderTemplate("Application/signup.html",fbUrl,twUrl);
		}
	}

	public static String getFbUrl() {

		final String FB_AUTH_URL ="https://www.facebook.com/dialog/oauth?client_id=";
		final String FB_CLIENT_ID = "816412851817516";
		final String FB_REDIRECT_URI = "http://localhost:9000/loginViaFacebook";
		final String FB_RESPONSE_TYPE = "code";
		String fbUrl = FB_AUTH_URL + FB_CLIENT_ID + "&redirect_uri=" + FB_REDIRECT_URI
					+ "&response_type=" + FB_RESPONSE_TYPE;
		return fbUrl;
	}

	public static String getTwUrl() {
//		String twUrl = "http://localhost:9000/LoginForTwitter";
		String twUrl = "http://localhost:9000/loginViaTwitter";
		return twUrl;
	}


	public static void loginSucceed() {
		boolean isConnected = isConnected();
		if (isConnected) {
			renderTemplate("Application/loginsucceed.html",isConnected);
		}
	}

	public static void submit(String inputUrl) {
		if(inputUrl.isEmpty()) {
			flash.error("URLが入力されていません。");
			index();
			return;
		}

		User user = User.findById(session.get("userId"));
		user.postedArticle = new Article();
		user.postedArticle.url = inputUrl;
		user.postedArticle.isBlackList = checkDomain(user.postedArticle.url);

		if(user.postedArticle.isBlackList) {
			flash.error("不正なURLです。（ブラックリスト）");
			index();
			return;
		}

		try {
			HtmlParser.parse(user.postedArticle);
			if(user.postedArticle.imageUrl.isEmpty()) {
				user.postedArticle.imageUrl = "/public/images/no_image.png";
			}
			user.postedArticle.save();
		} catch (Exception e) {
			e.printStackTrace();
			flash.error("URLが不正です。");
		}
		index();
	}
	/*
	public static void submit(String inputUrl) {
		Article article = new Article();
		article.url = inputUrl;
		article.isBlackList = checkDomain(article.url);

		if (article.url.isEmpty()) {
			flash.error("URLが入力されていません。");
		}
		else if (article.isBlackList) {
			flash.error("ブラックリスト");
		}
		else {
			try {
				HtmlParser.parse(article);
				if(article.imageUrl.isEmpty()) {
					article.imageUrl = "/public/images/no_image.png";
				}
				article.save();
			} catch (Exception e) {
				e.printStackTrace();
				flash.error("URLが不正です。");
			}
		}

		List<Article> articles = article.find("order by id desc").fetch();
		renderTemplate("Application/index.html",articles);
		index();
	}
*/
	public static void liked(Long articleID) {
		Article article = Article.findById(articleID);

		article.liked++;
		article.save();

		renderText(article.liked);
	}

	public static void view(Long articleID) {
		Article article = Article.findById(articleID);

		article.view++;
		article.save();

		renderText(article.view);
	}

	public static void deletePost(Long articleID) {
		Article article = Article.findById(articleID);
		article.findById(articleID)._delete();

		index();
//		List<Article> articles = Article.find("order by id desc").fetch();
//		renderTemplate("Application/index.html",articles);
	}

	public static boolean checkDomain(String url) {
		//TODO:ドメインチェック
		//パターンファイルみたいなので管理したい
		//ドメイン名で判断する
		//fc2はサブディレクトリなのでどうにかする

		if (url.matches(".*"+"xvideos"+".*")) {
			return true;
		}
		else if (url.matches(".*"+"video.fc2.com"+".*")) {
			return true;
		}
		else {
			return false;
		}
	}

}