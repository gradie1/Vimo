package vimo.kivubox.www.vimo;

/**
 * Created by Grace Lungu Birindwa on 20/04/2018.
 */

public class User {

    String firstName;
    String lastName;
    String profilPicture;
    String phoneNumber;
    String membership;
    String status;

    public User(String firstName, String lastName, String membership, String status) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.membership = membership;
        this.status = status;
    }

    public User(){

    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProfilPicture() {
        return profilPicture;
    }

    public void setProfilPicture(String profilPicture) {
        this.profilPicture = profilPicture;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMembership() {
        return membership;
    }

    public void setMembership(String membership) {
        this.membership = membership;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
