package de.pascalwagler.edimaxsmartplug.cli;

import java.time.LocalDateTime;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.pascalwagler.edimaxsmartplug.entities.PlugCredentials;
import de.pascalwagler.edimaxsmartplug.entities.PowerInformation;
import de.pascalwagler.edimaxsmartplug.entities.ScheduleDay;
import de.pascalwagler.edimaxsmartplug.smartplug.LocalHTTPConnection;
import de.pascalwagler.edimaxsmartplug.smartplug.PlugConnection;
import de.pascalwagler.edimaxsmartplug.smartplug.SmartPlug;
import de.pascalwagler.edimaxsmartplug.smartplug.SmartPlug.State;

public class EdimaxSmartPlugCLI {

	private static Options getOptions() {
		Options options = new Options();

		/*
		 * general options
		 */

		// IP
		Option ipaddressOpt = Option.builder("i")
				.longOpt("ip")
				.desc("IP address of the plug.")
				.required()
				.hasArg()
				.argName("IP")
				.build();
		options.addOption(ipaddressOpt);

		// username
		Option usernameOpt = Option.builder("u")
				.longOpt("username")
				.desc("Username for the login.")
				.hasArg()
				.argName("USERNAME")
				.build();
		options.addOption(usernameOpt);

		// password
		Option passwordOpt = Option.builder("p")
				.longOpt("password")
				.desc("Password for the login.")
				.hasArg()
				.argName("PASSWORD")
				.build();
		options.addOption(passwordOpt);

		// help
		Option helpOpt = Option.builder("h")
				.longOpt("help")
				.desc("Print this help message.")
				.build();
		options.addOption(helpOpt);

		/*
		 * state
		 */

		// state
		Option stateOpt = Option.builder("s")
				.longOpt("state")
				.desc("Switch the plug on or off by supplying an argument value 'on' or 'off'.")
				.hasArg()
				.optionalArg(true)
				.argName("STATE")
				.build();
		options.addOption(stateOpt);

		// toggle
		Option toggleOpt = Option.builder("t")
				.longOpt("toggle")
				.desc("Toggle the plug state on or off depending on the actual state. When the plug is currently on it will be turned off and vice versa.")
				.build();
		options.addOption(toggleOpt);

		/*
		 * schedule and history
		 */

		// schedule
		Option scheduleOpt = Option.builder("S")
				.longOpt("schedule")
				.desc("Print the formatted schedule.")
				.build();
		options.addOption(scheduleOpt);

		// history
		Option historyOpt = Option.builder("H")
				.longOpt("history")
				.desc("Print the formatted history.")
				.build();
		options.addOption(historyOpt);

		Option unitOpt = Option.builder("U")
				.longOpt("unit")
				.desc("The time unit for the plug history. Possible values are 'hour', 'day' and 'month'.")
				.hasArg()
				.optionalArg(true)
				.argName("TIME UNIT")
				.build();
		options.addOption(unitOpt);
		
		Option fromOpt = Option.builder("F")
				.longOpt("from")
				.desc("The start time and date for the history option.")
				.hasArg()
				.optionalArg(true)
				.argName("START DATE")
				.build();
		options.addOption(fromOpt);
		
		Option toOpt = Option.builder("T")
				.longOpt("to")
				.desc("The end time and date for the history option.")
				.hasArg()
				.optionalArg(true)
				.argName("END DATE")
				.build();
		options.addOption(toOpt);

		/*
		 * plug information
		 */
		// power
		Option powerOpt = Option.builder("P")
				.longOpt("power")
				.desc("Print information about the current power consumption.")
				.build();
		options.addOption(powerOpt);
		
		return options;
	}

	private static void processOptions(CommandLine line, Options options) throws Exception {

		OutputFormatter oFormatter = new TerminalFormatter();
		String ip = line.getOptionValue("ip");
		String username = line.getOptionValue("username", "admin");
		String password = line.getOptionValue("password", "1234");

		PlugCredentials credentials = new PlugCredentials(username, password);
		PlugConnection connection = new LocalHTTPConnection(credentials, ip);
		SmartPlug smartPlug = new SmartPlug(connection);

		// help
		if(line.hasOption("help")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("EdimaxSmartPlugJava", options);
		}


		// set state
		if(line.hasOption("state") && line.getOptionValue("state") != null) {
			if(line.getOptionValue("state").equalsIgnoreCase("on")) {
				smartPlug.switchOn();
			} else if(line.getOptionValue("state").equalsIgnoreCase("off")) {
				smartPlug.switchOff();
			}
		}

		// get state
		if(line.hasOption("state") && line.getOptionValue("state") == null) {
			State state = smartPlug.getState();
			String stateStr = oFormatter.getState(state);
			System.out.println(stateStr);
		}

		// toggle state
		if(line.hasOption("toggle")) {
			smartPlug.toggle();
		}

		// get schedule
		if(line.hasOption("schedule")) {
			ScheduleDay[] schedule = smartPlug.getSchedule();
			String scheduleStr = oFormatter.getSchedule(schedule);
			System.out.println(scheduleStr);
		}

		// get history
		if(line.hasOption("history")) {
			
			SmartPlug.TimeUnit unit = SmartPlug.TimeUnit.HOUR;
			if(line.hasOption("unit")) {
				switch(line.getOptionValue("unit")) {
				case "month":
					unit = SmartPlug.TimeUnit.MONTH;
					break;
				case "day":
					unit = SmartPlug.TimeUnit.DAY;
					break;
				default:
					unit = SmartPlug.TimeUnit.HOUR;
					break;
				}
			}
			
			float[] history = smartPlug.getHistory(unit, LocalDateTime.now().minusHours(3), LocalDateTime.now().plusHours(1));
			String historyStr = oFormatter.getHistory(history);
			System.out.println(historyStr);
		}

		// get power information
		if(line.hasOption("power")) {
			PowerInformation powInfo = smartPlug.getPowerInformation();
			String powInfoStr = oFormatter.getPowerInformation(powInfo);
			System.out.println(powInfoStr);
		}

		// get system time
		if(line.hasOption("time")) {
			String sysTime = smartPlug.getSystemTime();
			//String powInfoStr = oFormatter.getPowerInformation(sysTime);
			// TODO Parse result.
			System.out.println(sysTime);
		}
	}

	public static void main(String[] args) throws Exception {

		Options options = getOptions();

		CommandLineParser parser = new DefaultParser();
		CommandLine line = null;
		try {
			line = parser.parse(options, args);
			processOptions(line, options);
		} catch(ParseException exp) {
			System.err.println(exp.getMessage());

			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("EdimaxSmartPlugJava", options);
		}
	}
}