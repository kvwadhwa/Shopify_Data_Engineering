package com.kunal.image.data.entity;

import com.kunal.image.data.AbstractEntity;
import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
public class ImageEntity extends AbstractEntity {

    private String title;
    @Lob
    private String image;
    private boolean isPublic;
    private String username;

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public boolean isIsPublic() {
        return isPublic;
    }
    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

}
