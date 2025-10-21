package com.example.tea_quarkus;

import java.util.List;

public class Tea {
    private String name;
    private String description;
    private String recommendation;
    private int brewTime;
    private int waterTemp;
    private List<String> flavor;
    private List<String> purpose;
    private List<String> dayTime;

    public Tea() {}

    public Tea(String name, String description, String recommendation,
               int brewTime, int waterTemp,
               List<String> flavor, List<String> purpose, List<String> dayTime) {
        this.name = name;
        this.description = description;
        this.recommendation = recommendation;
        this.brewTime = brewTime;
        this.waterTemp = waterTemp;
        this.flavor = flavor;
        this.purpose = purpose;
        this.dayTime = dayTime;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getRecommendation() { return recommendation; }
    public int getBrewTime() { return brewTime; }
    public int getWaterTemp() { return waterTemp; }
    public List<String> getFlavor() { return flavor; }
    public List<String> getPurpose() { return purpose; }
    public List<String> getDayTime() { return dayTime; }
}