package com.dev175.chat.model;

import java.io.Serializable;
import java.util.Comparator;

public class Contact implements Serializable {
    private String contactId;
    private String profileImg;
    private String name;
    private String phoneNumber;
    private String email;
    private String availability;

    public Contact() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    //Sort Contacts By Name
    public static Comparator<Contact> contactListSortByName = new Comparator<Contact>() {

        public int compare(Contact s1, Contact s2) {
            String contact1 = s1.getName().toUpperCase();
            String contact2 = s2.getName().toUpperCase();

            //ascending order
            return contact1.compareTo(contact2);

            //descending order
            //return StudentName2.compareTo(StudentName1);
        }};


    //Sort Contacts By Status
    public static Comparator<Contact> contactListSortByStatus = new Comparator<Contact>() {

        public int compare(Contact s1, Contact s2) {
            String contact1 = s1.getAvailability().toUpperCase();
            String contact2 = s2.getAvailability().toUpperCase();

            //ascending order
            return contact1.compareTo(contact2);

            //descending order
            //return StudentName2.compareTo(StudentName1);
        }};

}
