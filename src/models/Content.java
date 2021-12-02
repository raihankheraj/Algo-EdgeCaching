package models;

import java.util.ArrayList;
import java.util.UUID;

public class Content {
    private UUID id = UUID.randomUUID();
    private String title;
    private double size;
    private double length;
    private PopularityLevel popularityLevel;
    private double daysSinceRelease;
    private ArrayList<ContentPart> parts = new ArrayList<>();


    // maybe change constructor params?? take in days since release
    // adding days since release
    public Content(String title, double daysSinceRelease, PopularityLevel popularityLevel) {
        this.title = title;
        // added this line
        this.daysSinceRelease = daysSinceRelease;
        this.popularityLevel = popularityLevel;
        
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public PopularityLevel getPopularityLevel() {
        return popularityLevel;
    }

    public void setPopularityLevel(PopularityLevel popularityLevel) {
        this.popularityLevel = popularityLevel;
    }

    public double getDaysSinceRelease() {
        return daysSinceRelease;
    }

    public void setDaysSinceRelease(double daysSinceRelease) {
        this.daysSinceRelease = daysSinceRelease;
    }

    public ArrayList<ContentPart> getParts() {
        return parts;
    }

    public void setParts(ArrayList<ContentPart> parts) {
        this.parts = parts;
    }

    @Override
    public String toString() {
        return "Content{" +
                "title='" + title + '\'' +
                ", popularityLevel=" + popularityLevel +
                ", daysSinceRelease=" + daysSinceRelease +
                '}';
    }
}
