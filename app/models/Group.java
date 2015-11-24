package models;

import java.util.List;

import javax.persistence.OneToMany;

import play.db.jpa.Model;

public class Group extends Model {

	public String gourpName;

	@OneToMany
	public List<Group> userList;

	@OneToMany
	public List<Group> articleList;

	public Group(String groupName) {
		this.gourpName = groupName;

//		userList = new ArrayList<>();
	}
}
