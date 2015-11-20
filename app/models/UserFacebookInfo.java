package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class UserFacebookInfo extends Model {
	public String facebookId;
	public Long userId;
	public String name;

	public static UserFacebookInfo findByFacebookId(String id) {
		return UserFacebookInfo.find("facebookId", id).first();
	}
}
