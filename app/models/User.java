package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cascade;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class User extends Model {
	public String name;
	public String iconUrl;

	public static User findById(String userId) {
		return User.findById(Long.parseLong(userId));
	}
//	@ManyToOne
//	public String author;

//	@OneToMany(mappedBy="posted_article", cascade=CascadeType.ALL)
//	public List<Article> postedArticles;

//	@OneToMany
//	public List<Group> groupList;

//	@OneToMany
//	public List<Archive> archivedArticleList;

//	public User(String author, String archivedArticleID) {
//		this.author = author;
//		this.groupList = new ArrayList<Group>();
//		this.archivedArticleList = new ArrayList<Archive>();
//		this.postedArticles = new ArrayList<Article>();
//	}
}
