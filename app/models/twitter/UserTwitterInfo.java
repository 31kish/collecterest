package models.twitter;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class UserTwitterInfo extends Model {
	public String twitterId;
	public Long userId;
	public String name;

	public static UserTwitterInfo findbyTwitterId (String id) {
		return UserTwitterInfo.find("twitterId", id).first();
	}
}
