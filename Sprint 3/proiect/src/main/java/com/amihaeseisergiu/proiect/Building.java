/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amihaeseisergiu.proiect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

/**
 * @author Alex
 */
public class Building implements Serializable {
    
    static final long serialVersionUID = 1L;
    
    Map<Integer, Floor> floors = new HashMap<>();
    String name;

    public Map<Integer, Floor> getFloors() {
        return floors;
    }

    public void setFloors(Map<Integer, Floor> floors) {
        this.floors = floors;
    }

    public JSONObject toJson() {
        JSONObject buildingJSON = new JSONObject();
        buildingJSON.put("name", name);

        List<JSONObject> floorList = new ArrayList<JSONObject>();
        for (var floor : floors.entrySet()) {
            floorList.add(floor.getValue().toJson());
        }

        buildingJSON.put("floors", floorList);

        return buildingJSON;
    }
}