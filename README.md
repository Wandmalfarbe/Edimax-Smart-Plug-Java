# EdimaxSmartPlugJava

A Java library for controlling the Edimax Smart Plug SP-1101W.

## Usage

Create a SmartPlug object to control your plug.

```java
PlugCredentials credentials = new PlugCredentials("admin","1234");
PlugConnection connection = new LocalConnection(credentials,"192.168.178.34");
SmartPlug smartPlug = new SmartPlug(connection);
```

Use the various methods of the `smartPlug` object to get information or toggle the plug state.

```java
ScheduleDay[] schedule = smartPlug.getSchedule();
PowerInformation powInfo = smartPlug.getPowerInformation();

// Toggle your plug on or off depending on the current status.
smartPlug.toggle();

// Switch your plug on.
smartPlug.switchOn();

// get the history
float[] history = smartPlug.getHistory(SmartPlug.TimeUnit.HOUR, LocalDateTime.now().minusHours(3), LocalDateTime.now().plusHours(1));
```

## Implemented Functionality

### Switching On and Off

- [x] switch on and off
- [x] get current state (on and off)
- [x] toggle the current state

### History

- [ ] get the history (partially)

### Schedule

- [x] get the schedule
- [ ] change the schedule

### System and Power Information

- [x] get the power information
- [x] get the system information (including the name)
- [x] change the plug name

### Budget Control

- [ ] get budget control information
- [ ] change budget control information

### Email Notifications

- [ ] get email notification information
- [ ] change email notification information

### Time

- [ ] get the plug time settings
- [ ] change the plug time settings

### Communication

- [ ] local communication
    - [x] send messages
    - [x] receive messages
    - [ ] plug discovery
- [ ] cloud communication
    - [ ] send messages
    - [ ] receive messages
    - [ ] plug discovery (is this possible with cloud communication?)

### Known Bugs

1. [History] The history values are accumulated values per hour (only when requesting the history for a day).
2. [Command Line] Not all methods from `SmartPlug` can be accessed via the command line.
3. [Command Line] There is no command line help text.