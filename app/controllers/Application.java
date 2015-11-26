package controllers;

import static org.hamcrest.CoreMatchers.nullValue;
import play.*;
import play.mvc.*;
import play.mvc.results.RenderText;

import java.io.IOException;
import java.util.*;

import org.omg.CORBA.PUBLIC_MEMBER;
import org.opengraph.OpenGraph;

import com.sun.xml.internal.bind.v2.model.core.ID;

import models.*;

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
		//TODO:Articlesがない

		User user = null;
		if (isConnected()) {
			user = User.findById(session.get("userId"));
			renderTemplate("Application/index.html",user);
		}
		else {
			welcome();
		}
	}

	public static void signup() {

		final String FACEBOOK_AUTH_URL ="https://www.facebook.com/dialog/oauth?client_id=";
		final String CLIENT_ID = "816412851817516";
		final String REDIRECT_URI = "http://localhost:9000/loginViaFacebook";
		final String RESPONSE_TYPE = "code";
		String url = FACEBOOK_AUTH_URL + CLIENT_ID + "&redirect_uri=" + REDIRECT_URI
					+ "&response_type=" + RESPONSE_TYPE;

		User user = null;
		if (isConnected()) {
			user = User.findById(session.get("userId"));
			index();
		}
		else {
			renderTemplate("Application/signup.html",url);
		}
	}

	public static void loginSucceed() {
		boolean isConnected = isConnected();
		if (isConnected) {
			renderTemplate("Application/loginsucceed.html",isConnected);
		}
	}

	public static void submit(String inputUrl) {
		//TODO:renderTemplateのせいでuserがない
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

	}

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

		List<Article> articles = Article.find("order by id desc").fetch();
		renderTemplate("Application/index.html",articles);
	}

	public static boolean checkDomain(String url) {
		//TODO
		//パターンファイルみたいなので管理したい
		//ドメイン名で判断する
		//fc2はサブディレクトリなのでどうにかする

		if (url.matches(".*"+"xvideos"+".*")) {
			return true;
		}
		else if (url.matches(".*"+"video.fc2.com/a/"+".*")) {
			return true;
		}
		else {
			return false;
		}
	}

}