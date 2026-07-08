package model;

public class City {

    private int id;
    private String name;
    private String state;
    private double distanceFromCapital;

    public City(int id, String name, String state, double distanceFromCapital) {

        this.id = id;
        this.name = name;
        this.state = state;
        this.distanceFromCapital = distanceFromCapital;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public double getDistanceFromCapital() {
        return distanceFromCapital;
    }

    public void setDistanceFromCapital(double distanceFromCapital) {
        this.distanceFromCapital = distanceFromCapital;
    }
}