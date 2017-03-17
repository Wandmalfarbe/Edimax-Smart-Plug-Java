package de.pascalwagler.edimaxsmartplug.entities;

import java.util.ArrayList;

public class ScheduleDay {
	
	public boolean isActive;
	
	public ArrayList<ScheduleTime> scheduleTimes = new ArrayList<ScheduleTime>();
}