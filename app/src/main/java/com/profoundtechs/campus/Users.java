package com.profoundtechs.campus;

/**
 * Created by HP on 3/9/2018.
 */

public class Users {

    public String name;
    public String image;
    public String university;
    public String role;
    public String thumb_image;

    public Users() {

    }

    public Users(String name, String image, String university, String role, String thumb_image) {
        this.name = name;
        this.image = image;
        this.university = university;
        this.role = role;
        this.thumb_image = thumb_image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }
}