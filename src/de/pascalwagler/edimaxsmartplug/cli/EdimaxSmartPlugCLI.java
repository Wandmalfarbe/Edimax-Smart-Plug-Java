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
import de.pascalwagler.edimaxsmartplug.smartplug.LocalConnection;
import de.pascalwagler.edimaxsmartplug.smartplug.PlugConnection;
import de.pascalwagler.edimaxsmartplug.smartplug.SmartPlug;

public class EdimaxSmartPlugCLI {

	public static void main(String[] args) throws Exception {

		Options options = new Options();

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

		// state
		Option switchOpt = Option.builder("s")
				.longOpt("state")
				.desc("Switch the plug on or off by supplying an argument value 'on' or 'off'.")
				.hasArg()
				.argName("STATE")
				.build();
		options.addOption(switchOpt);

		Option helpOpt = new Option( "help", "print this help message" );
		options.addOption(helpOpt);

		Option toggleOpt = new Option( "toggle", "Toggle the plug state on or off depending on the actual state. When the plug is currently on it will be turned off and vice versa." );
		options.addOption(toggleOpt);

		Option scheduleOpt = new Option( "schedule", "Print the formatted schedule." );
		options.addOption(scheduleOpt);

		Option historyOpt = new Option( "history", "Print the unformatted history." );
		options.addOption(historyOpt);

		/*
		 * Parsing the arguments
		 */
		CommandLineParser parser = new DefaultParser();
		CommandLine line = null;
		try {
			line = parser.parse( options, args );
		} catch( ParseException exp ) {
			System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );

			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "EdimaxSmartPlugJava", options );

			return;
		}

		/*
		 * General setup
		 */
		OutputFormatter oFormatter = new TerminalFormatter();
		String ip = line.getOptionValue("ip");
		String username = line.getOptionValue("username", "admin");
		String password = line.getOptionValue("password", "1234");

		PlugCredentials credentials = new PlugCredentials(username, password);
		PlugConnection connection = new LocalConnection(credentials, ip);
		SmartPlug smartPlug = new SmartPlug(connection);

		// get schedule
		if(line.hasOption("schedule")) {

			ScheduleDay[] schedule = smartPlug.getSchedule();
			String scheduleStr = oFormatter.getSchedule(schedule);
			System.out.println(scheduleStr);
		}

		// get history
		if(line.hasOption("history")) {

			// TODO Remove hardcoded parameters
			float[] history = smartPlug.getHistory(SmartPlug.TimeUnit.HOUR, LocalDateTime.now().minusHours(3), LocalDateTime.now().plusHours(1));
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

		// toggle state
		if(line.hasOption("toggle")) {
			smartPlug.toggle();
		}

		// get state
		if(line.hasOption("state")) {
			if(line.getOptionValue("state").equalsIgnoreCase("on")) {
				smartPlug.switchOn();
			} else if(line.getOptionValue("state").equalsIgnoreCase("off")) {
				smartPlug.switchOff();
			}
		}
	}
}