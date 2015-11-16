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

    	List<Article> articles = Article.find("order by id desc").fetch();
        render(articles);
    }

    public static void submit(String inputUrl) {
    	Article article = new Article();
    	article.url = inputUrl;

    	try {
			HtmlParser.parse(article);
	    	if(!article.url.isEmpty()) {
	    		article.save();
	    	}
		} catch (Exception e) {
			e.printStackTrace();
			flash.error("URLが不正です");
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
    	//TODO
    	//記事IDを受け取り、その記事を削除する
    	//削除して更新したArticlesを渡す

    	Article article = Article.findById(articleID);

//    	article.delete(query, params);
//    	Article.deleteAll();//ためしに全部消してみた

    	System.out.println("DeletePost");

    	List<Article> articles = Article.find("order by id desc").fetch();
    	renderTemplate("Application/index.html",articles);
    }
}