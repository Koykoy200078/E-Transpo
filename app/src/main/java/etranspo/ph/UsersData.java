package etranspo.ph;

public class UsersData
{
    private String userId;
    private String username;
    private String email;
    private String fullname;
    private String address;
    private String mobile;
    private String gender;
    private String imageUrl;

    public UsersData(String userId, String username, String email, String fullname, String address, String mobile, String gender, String imageURL)
    {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullname = fullname;
        this.address = address;
        this.mobile = mobile;
        this.gender = gender;
        this.imageUrl = imageUrl;
    }

    public UsersData() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getImageURL() {
        return imageUrl;
    }

    public void setImageURL(String imageURL) {
        this.imageUrl = imageURL;
    }
}