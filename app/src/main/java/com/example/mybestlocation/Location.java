package com.example.mybestlocation;

public class Location {
    private int idposition; // Added ID field
    private String pseudo;
    private double latitude;
    private double longitude;
    public Location() {
        // Provide default values, for example:

        this.pseudo = "";
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    // Constructor
    public Location(int id, String pseudo, double latitude, double longitude) {
        this.idposition = id;
        this.pseudo = pseudo;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Location( String pseudo, double latitude, double longitude) {
    }


    // Getters and Setters
    public int getId() {
        return idposition;
    }

    public void setId(int id) {
        this.idposition = id;
    }


    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    @Override
    public String toString() {
        return "Location{" +
                "idposition=" + idposition +
                ", pseudo='" + pseudo + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

}
