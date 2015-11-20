package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class Article extends Model {
	public String title;
	public String description;
	public String imageUrl;
	public String url;
	public Integer view;
	public Integer liked;
	public Integer favorite;
	public boolean isBlackList;

	public Article() {
		view = 0;
		liked = 0;
		favorite = 0;
	}
}

