package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class User extends Model {
	public String name;
	public String iconUrl;

	public static User findById(String userId) {
		return User.findById(Long.parseLong(userId));
	}

}
