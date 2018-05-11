package vimo.kivubox.www.vimo;

import java.util.List;
import java.util.Map;

/**
 * Created by c55 on 10/05/2018.
 */

public class Product {

    String name;
    List<String> images;
    String category;
    String likes;
    String description;
    String city;

    public Product(String name, List<String> images, String category, String likes, String description, String city) {
        this.name = name;
        this.images = images;
        this.category = category;
        this.likes = likes;
        this.description = description;
        this.city = city;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}


