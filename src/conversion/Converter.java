package conversion;

import fields.Class;
import fields.Field;
import io.Reader;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class Converter {
	/**
	 * Run the converted based off command-line arguments
	 */
	public static void main(String [] args) {
		String inputFilePath = null;
		String outputFilePath = null;
		boolean flipAxis = true; // Default to flip the axis so world is not mirrored
		boolean foundFlipArg = false;
		for(int i = 0; i < args.length; i++) {
			String curArg = args[i];
			if(curArg.equals("-i") || curArg.equals("-input")) {
				if(inputFilePath != null) {
					System.err.println("Error: Duplicate -input paramater");
					System.exit(1);
				}

				// Set next argument as inputFilePath
				i++;
				inputFilePath = args[i];
			} else if(curArg.equals("-o") || curArg.equals("-output")) {
				if(outputFilePath != null) {
					System.err.println("Error: Duplicate -output paramater");
					System.exit(1);
				}

				// Set next argument as outputFilePath
				i++;
				outputFilePath = args[i];
			} else if(curArg.equals("-no_flip")) {
				if(foundFlipArg) {
					System.err.println("Error: Duplicate -no_flip paramater");
					System.exit(1);
				}

				flipAxis = false;
			} else if(curArg.equals("-h") || curArg.equals("-help") || curArg.equals("--h") || curArg.equals("--help")) {
				printHelp();
				System.exit(0);
			} else {
				System.err.println("Error: Unknown argument " + curArg + ". Use -help for help on usage.");
				System.exit(1);
			}
		}
		
		// If a specific output was specified, input has to be specified
		if(inputFilePath == null && outputFilePath != null) {
			System.err.println("Error: Output path specified but no input path specified.");
			System.exit(1);
		}

		// If input was specified but no output was specified, set to <input file name>.json
		if(inputFilePath != null && outputFilePath == null) {
			int lastDotIndex = inputFilePath.lastIndexOf('.');
			outputFilePath = inputFilePath.substring(0, lastDotIndex) + ".json";
		}

		if(inputFilePath == null && outputFilePath == null) {
			// If no input and output specified, convert all .dat and .mine files in current directory
			String[] files = (new File(".")).list();
			for(String f : files) {
				if(f.endsWith(".dat") || f.endsWith(".mine")) {
					int lastDotIndex = f.lastIndexOf('.');
					String curOutputFilePath = f.substring(0, lastDotIndex) + ".json";
					convertToJS(f, curOutputFilePath, flipAxis);
				}
			}
		} else {
			convertToJS(inputFilePath, outputFilePath, flipAxis);
		}
	}

	/**
	 * Print out help message when used with -h argument
	 */
	public static void printHelp() {
		System.out.println("Convert Pre-Classic or Classic Minecraft World to JavaScript Classic Minecraft Remake.");
		System.out.println("When run without any parameters, defaults to converting all .dat and .mine files in its folder into corresponding .json files.");
		System.out.println("Example usage: java -jar JavaScriptMCConverter.jar -i level.dat -o level.json -no_flip");
		System.out.println();
		System.out.println("Options:");
		System.out.println("-h or -help: print help menu");
		System.out.println();
		System.out.println("-i or -input: followed by the input file path");
		System.out.println();
		System.out.println("-o or -output: followed by the output file path");
		System.out.println();
		System.out.println("-no_flip: do not flip the x-axis during conversion, keep original coordinates");
		System.out.println();
	}
	
	/**
	 * Convert the Classic or Pre-Classic inputFile to a Classic JavaScript Remake
	 * JSON file saved to outputFile. If flipAxis is true, the x-axis is flipped
	 * so that the world does not get mirrored. If flipAxis is false, the x-axis
	 * is not flipped resulting in a mirrored world.
	 */
	public static void convertToJS(String inputFilePath, String outputFilePath, boolean flipAxis) {
		File inputFile = new File(inputFilePath);
		
		Class readClass = null;
		try {
			readClass = Reader.read(inputFile);
		} catch(Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}

		//Extract dimensions and block array from file
		ArrayList<Field> fields = readClass.getFields();
		int width = 0;
		int height = 0;
		int depth = 0;
		for(Field f : fields) {
			if(f.getFieldName().equals("width")) {
				width = (int) f.getField();
			} else if(f.getFieldName().equals("height")) {
				height = (int) f.getField();
			} else if(f.getFieldName().equals("depth")) {
				depth = (int) f.getField();
			}
		}
		if(width == 0 && height == 0 && depth == 0) {
			//Set default values for width, height, and depth on pre-Classic 0.13a-dev worlds
			width = 256;
			height = 256;
			depth = 64;
		}
		
		StringBuilder jsRemakeSavedGameSB = new StringBuilder();
		jsRemakeSavedGameSB.append("{\"worldSeed\":3141,\"changedBlocks\":{");

		// Add changedBlock for every block
		for(int i = 0; i < Reader.blockBytes.size(); i++) {
			int y = i / (width * height);
			int z = i % (width * height) / width;
			int x = i % (width * height) % width;
			if(flipAxis) {
				x = width - x - 1;
			}
			byte currBlock = Reader.blockBytes.get(i);

			// Add stringfor changedBlocks
			jsRemakeSavedGameSB.append("\"p");
			jsRemakeSavedGameSB.append(x);
			jsRemakeSavedGameSB.append("_");
			jsRemakeSavedGameSB.append(y);
			jsRemakeSavedGameSB.append("_");
			jsRemakeSavedGameSB.append(z);
			jsRemakeSavedGameSB.append("\":{\"a\":1,\"bt\":");
			jsRemakeSavedGameSB.append(convertBlockID(currBlock));
			jsRemakeSavedGameSB.append("},");
		}
		jsRemakeSavedGameSB.setLength(jsRemakeSavedGameSB.length() - 1); // remove final comma

		// The actual JavaScript remake world dimension is 1 less that the worldSize
		jsRemakeSavedGameSB.append("},\"worldSize\":");
		int worldSize = Math.max(width, height);
		jsRemakeSavedGameSB.append(worldSize + 1);

		jsRemakeSavedGameSB.append(",\"version\":1}");
		String jsRemakeSavedGame = jsRemakeSavedGameSB.toString();

		//Write to file
		try {
			File outputFile = new File(outputFilePath);
			FileWriter fw = new FileWriter(outputFile);
			fw.write(jsRemakeSavedGame);
			fw.close();
		} catch(Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	// Convert from Java Classic ID to JavaScript Remake ID. See:
	// https://minecraft.wiki/w/Java_Edition_Classic_data_values
	// https://minecraft.wiki/w/Classic_remake_data_values
	private static int convertBlockID(int classicBlockID) {
		if(classicBlockID == 0) {return 0;}
		if(classicBlockID == 1) {return 2;}
		if(classicBlockID == 2) {return 1;}
		if(classicBlockID == 3) {return 3;}
		if(classicBlockID == 4) {return 9;}
		if(classicBlockID == 5) {return 4;}
		if(classicBlockID == 6) {return 8;}
		if(classicBlockID == 7) {return 10;}
		if(classicBlockID == 8) {return 7;}
		if(classicBlockID == 9) {return 7;}
		if(classicBlockID == 10) {return 17;}
		if(classicBlockID == 11) {return 17;}
		if(classicBlockID == 12) {return 11;}
		if(classicBlockID == 13) {return 12;}
		if(classicBlockID == 14) {return 18;}
		if(classicBlockID == 15) {return 19;}
		if(classicBlockID == 16) {return 20;}
		if(classicBlockID == 17) {return 13;}
		if(classicBlockID == 18) {return 14;}
		if(classicBlockID == 19) {return 22;}
		if(classicBlockID == 20) {return 23;}
		if(classicBlockID == 21) {return 24;}
		if(classicBlockID == 22) {return 25;}
		if(classicBlockID == 23) {return 26;}
		if(classicBlockID == 24) {return 27;}
		if(classicBlockID == 25) {return 28;}
		if(classicBlockID == 26) {return 29;}
		if(classicBlockID == 27) {return 30;}
		if(classicBlockID == 28) {return 31;}
		if(classicBlockID == 29) {return 32;}
		if(classicBlockID == 30) {return 33;}
		if(classicBlockID == 31) {return 34;}
		if(classicBlockID == 32) {return 35;}
		if(classicBlockID == 33) {return 36;}
		if(classicBlockID == 34) {return 37;}
		if(classicBlockID == 35) {return 38;}
		if(classicBlockID == 36) {return 39;}
		if(classicBlockID == 37) {return 6;}
		if(classicBlockID == 38) {return 5;}
		if(classicBlockID == 39) {return 16;}
		if(classicBlockID == 40) {return 15;}
		if(classicBlockID == 41) {return 21;}
		return 0; // any other block set to air (0)
	}
}
