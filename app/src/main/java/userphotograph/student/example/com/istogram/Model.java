package userphotograph.student.example.com.istogram;

public class Model {

    private String title;
    private String imageUri;
    private String imageName;
    private String numLikes;


    public Model() {
    }


    public Model(String title, String imageUri, String imageName, String numLikes) {
        this.title = title;
        this.imageUri = imageUri;
        this.imageName = imageName;
        this.numLikes=numLikes;
    }

    public Model(String title, String imageUri, String imageName) {
        this.title = title;
        this.imageUri = imageUri;
        this.imageName = imageName;
    }

    public String getImageName() {return imageName;}

    public String getTitle() {
        return title;
    }

    public String getImageUri() {
        return imageUri;
    }

    public String getNumLikes() { return numLikes; }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setNumLikes(String numLikes) { this.numLikes = numLikes; }

}
