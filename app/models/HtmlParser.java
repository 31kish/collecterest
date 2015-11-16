package models;

import java.io.IOException;

import org.opengraph.OpenGraph;

import com.mchange.v2.cfg.PropertiesConfigSource.Parse;

public class HtmlParser {

	public static Article parse(Article article) throws IOException, Exception {

		OpenGraph openGraph = new OpenGraph(article.url,true);

		article.title = openGraph.getContent("title");
		if (article.title == null) {
			
		}
		System.out.println(article.title);
		article.description = openGraph.getContent("description");
		System.out.println(article.description);
		article.imageUrl = openGraph.getContent("image");

		return article;
	}
}
