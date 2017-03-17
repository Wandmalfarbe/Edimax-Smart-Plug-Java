package de.pascalwagler.edimaxsmartplug.cli;

import de.pascalwagler.edimaxsmartplug.entities.PowerInformation;
import de.pascalwagler.edimaxsmartplug.entities.ScheduleDay;
import de.pascalwagler.edimaxsmartplug.smartplug.SmartPlug.State;

public interface OutputFormatter {
	
	public String getSchedule(ScheduleDay[] schedule);

	public String getHistory(float[] history);
	
	public String getState(State status);
	
	public String getPowerInformation(PowerInformation powInfo);
}
