package models.twitter;

public class TwitterUserObject {
	public String id;
	public String screen_name;
	public Picture picture;

	public class Picture {
		public Data data;

		public class Data {
			public String is_silhouette;
			public String url;
		}

	}
}
