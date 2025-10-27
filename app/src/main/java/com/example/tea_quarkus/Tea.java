package com.example.tea_quarkus;

import java.util.List;

public class Tea {
    private Long id;  // Add this field
    private String name;
    private String description;
    private String recommendation;
    private int brewTime;
    private int waterTemp;
    private List<String> flavor;
    private List<String> purpose;
    private List<String> dayTime;

    public Tea() {}

    public Tea(Long id, String name, String description, String recommendation,
               int brewTime, int waterTemp,
               List<String> flavor, List<String> purpose, List<String> dayTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.recommendation = recommendation;
        this.brewTime = brewTime;
        this.waterTemp = waterTemp;
        this.flavor = flavor;
        this.purpose = purpose;
        this.dayTime = dayTime;
    }

    // Add getter and setter for id
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getRecommendation() { return recommendation; }
    public int getBrewTime() { return brewTime; }
    public int getWaterTemp() { return waterTemp; }
    public List<String> getFlavor() { return flavor; }
    public List<String> getPurpose() { return purpose; }
    public List<String> getDayTime() { return dayTime; }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public void setBrewTime(int brewTime) {
        this.brewTime = brewTime;
    }

    public void setWaterTemp(int waterTemp) {
        this.waterTemp = waterTemp;
    }

    public void setFlavor(List<String> flavor) {
        this.flavor = flavor;
    }

    public void setPurpose(List<String> purpose) {
        this.purpose = purpose;
    }

    public void setDayTime(List<String> dayTime) {
        this.dayTime = dayTime;
    }
}