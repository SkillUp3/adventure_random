package com.company;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by idejesus on 01/04/2017.
 */
public class Location implements Serializable {
    private final int locationID;
    private final String description;
    private final Map<String, Integer> exits;

    private long serialVersionUID = 1L;


    public Location(int locationID, String description, Map<String, Integer> exits) {
        this.locationID = locationID;
        this.description = description;
        if(exits!= null){
            this.exits = new LinkedHashMap<>(exits);

        }else{
            this.exits = new LinkedHashMap<>();
        }
        this.exits.put("Q", 0);
    }
    protected void addExit(String direction, int location){
        exits.put(direction, location);
    }
//Here is your challenge and the challenges to work out what is wrong with the location constructor that would
//    allow the program to compile and crash at runtime so when you identify the problem modify the code to fix it
//    so I just i’ll just say that again your challenge here is to work out what is wrong with the location constructor
//    that you can see on screen line 14 that will allow the program to compile but crash on run time when you identify
//    the problem modify the code to fix it.

    public int getLocationID() {
        return locationID;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Integer> getExits() {
        return new LinkedHashMap<>(exits);
    }
}

