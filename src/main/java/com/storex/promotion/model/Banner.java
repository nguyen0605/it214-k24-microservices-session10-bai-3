package com.storex.promotion.model;

public class Banner {
    private String id;
    private String title;
    private String imageUrl;
    private String link;
    private String description;

    public Banner() {}

    public Banner(String id, String title, String imageUrl, String link, String description) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.link = link;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}