package de.pascalwagler.edimaxsmartplug.cli;

public class ASCIIPlot {

	private int widthInCharacters;
	
	public ASCIIPlot(int widthInCharacters) {
		this.widthInCharacters = widthInCharacters;
	}
	
	public String plot(float[] array) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("┌"+StringUtil.repeat("─", 8)+"╥");
		sb.append(StringUtil.repeat("─", widthInCharacters-11)+"┐\n");
		
		double best = -1;
		for(int x = 0; x<array.length; x++) {
			if(array[x] > best) {
				best = array[x];
			}
		}
		
		for(int x = 0; x<array.length; x++) {
			double realValue = array[x];
			double scaledValue = (realValue / best) * (widthInCharacters-13);
			
			String bar = StringUtil.repeat("▄", (int)Math.round(scaledValue));
			String completeString = String.format("│%7.3f ║ %-"+(widthInCharacters-13)+"s │%n", realValue, bar);
			
			sb.append(completeString);
		}
		
		sb.append("└"+StringUtil.repeat("─", 8)+"╨");
		sb.append(StringUtil.repeat("─", widthInCharacters-11)+"┘\n");
		return sb.toString();
	}
}

class StringUtil {
	
	static String repeat(String str, int times) {
		StringBuilder sb = new StringBuilder();
		
		for(int x = 0; x < times; x++) {
			sb.append(str);
		}
		
		return sb.toString();
	}
}
