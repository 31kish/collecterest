package models;

public class FacebookUserObject {
	public String id;
	public String name;
	public Picture picture;

	public class Picture {
		public Data data;

		public class Data {
			public String is_silhouette;
			public String url;
		}
	}

}
