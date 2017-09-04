package de.pascalwagler.edimaxsmartplug.cli;

import de.pascalwagler.edimaxsmartplug.entities.PowerInformation;
import de.pascalwagler.edimaxsmartplug.entities.ScheduleDay;
import de.pascalwagler.edimaxsmartplug.entities.ScheduleTime;
import de.pascalwagler.edimaxsmartplug.smartplug.SmartPlug.State;

public class TerminalFormatter implements OutputFormatter{
	
	final static String s = System.getProperty("line.separator");
	
	@Override
	public String getSchedule(ScheduleDay[] schedule) {

		StringBuilder sb = new StringBuilder();
		for(int x = 0; x <= 6; x++) {

			String scheduleStatus = schedule[x].isActive ? " (enabled)" : "(disabled)";
			sb.append(s);
			sb.append("Schedule on day " + x + " "+scheduleStatus+s);
			sb.append("————————————————————————————"+s);
			sb.append(s);

			for(ScheduleTime time : schedule[x].scheduleTimes) {
				sb.append("- Start: "+time.start+s);
				sb.append("  End:   "+time.end+s);
				sb.append(time.isActive ? "  (enabled)"+s : "  (disabled)"+s);
				sb.append(""+s);
			}
		}
		return sb.toString();
	}

	@Override
	public String getHistory(float[] history) {
		
		ASCIIPlot plot = new ASCIIPlot(80);
		return plot.plot(history);
	}

	@Override
	public String getState(State status) {
		
		if(status == State.ON) {
			return "Status on";
		}
		return "Status: off";
	}

	@Override
	public String getPowerInformation(PowerInformation powInfo) {
		
		return "Power Information \n—————————————————\n\nlastToggleTime: " + powInfo.getLastToggleTime()
				+ ", \n\nnowCurrent: " + powInfo.getNowCurrent() + ", \nnowPower: "
				+ powInfo.getNowPower() + ", \n\nnowEnergyDay: " + powInfo.getNowEnergyDay()
				+ ", \nnowEnergyWeek: " + powInfo.getNowEnergyWeek()
				+ ", \nnowEnergyMonth: " + powInfo.getNowEnergyMonth() + "\n";		
	}
}
