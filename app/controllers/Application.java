package controllers;

import play.*;
import play.mvc.*;
import play.mvc.results.RenderText;

import java.io.IOException;
import java.util.*;

import org.opengraph.OpenGraph;

import com.sun.xml.internal.bind.v2.model.core.ID;

import models.*;

public class Application extends Controller {

	public static void index() {

//		List<Article> articles = Article.find("order by id desc").fetch();
//		render(articles);

		renderTemplate("Application/welcome.html");
	}

	public static void top() {
		List<Article> articles = Article.find("order by id desc").fetch();
		renderTemplate("Application/index.html",articles);
	}

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