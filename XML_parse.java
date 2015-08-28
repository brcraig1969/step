package xml_parse;

public class XML_parse {

	public static void main(String[] args) {

		if (args.length != 1) {
			System.err.println("Must pass exactly one argument <in> or <out>");
		}
		else {
			switch (args[0]) {
			case "out" : 
				StepOutbound stepOut = new StepOutbound();
				stepOut.parse();
				System.out.println("Parsing STEP outbound files");
				break;
			case "in" :
				StepInbound stepIn = new StepInbound();
				stepIn.parse();
				System.out.println("Parsing STEP inbound files");
				break;
			default : 
				System.err.println("Argument: " + args[0] + " not recognized");
				break;
			}
		}	
	}
}
