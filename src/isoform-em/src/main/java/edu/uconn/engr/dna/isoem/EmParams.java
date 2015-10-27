package edu.uconn.engr.dna.isoem;

import edu.uconn.engr.dna.io.DefaultTwoFieldParser;
import edu.uconn.engr.dna.probability.MapProbabilityDistribution;
import edu.uconn.engr.dna.probability.NormalProbabilityDistribution;
import edu.uconn.engr.dna.util.GroupedRandomAccessMap;
import edu.uconn.engr.dna.util.StringToDoubleRandomAccessMap;
import edu.uconn.engr.dna.util.Utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;



public class EmParams {

	private static final long serialVersionUID = 1L;

	static enum CommandLineArgs {
		isFirstReadFromCodingStrand(CommandLineArgsType.bool, false), 
		isPairedReads(CommandLineArgsType.bool, false),
		convertToGenomeCoords(CommandLineArgsType.bool, true), 
		checkReadsAgainstGenome(CommandLineArgsType.bool, false), 
		isGeneLevelEstimation(CommandLineArgsType.bool, false), 
		clusterFile(CommandLineArgsType.file, ""),
		knownIsoformsFileName(CommandLineArgsType.file, "'"), 
		genomeFileName(CommandLineArgsType.file, ""),
		fragLenDistrib(CommandLineArgsType.fraglen, "normal,250.0,0.0");
		
		private CommandLineArgsType type;
		private Object defaultValue;
		CommandLineArgs(CommandLineArgsType type, Object defaultValue) {
			this.type = type;
			this.defaultValue = defaultValue;
		}
		public Object getDefaultValue() {
			return defaultValue;
		}
	}

	enum CommandLineArgsType {
		bool(){
			@Override
			public Object getValue(String stringValue) throws IllegalArgumentException {
				if ("true".equalsIgnoreCase(stringValue) || "false".equalsIgnoreCase(stringValue)) {
					return Boolean.parseBoolean(stringValue);	
				} else {
					throw new IllegalArgumentException("expected true or false, got " + stringValue);
				}
			}
		}, 
		file() {
			@Override
			public Object getValue(String stringValue)
					throws IllegalArgumentException {
				return stringValue;
			}
		}, 
		real() {
			@Override
			public Object getValue(String stringValue)
					throws IllegalArgumentException {
				try {
					return Double.parseDouble(stringValue);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("expected real number, got " + stringValue, e);
				}
			}
		}, fraglen() {
			@Override
			public Object getValue(String stringValue)
					throws IllegalArgumentException {
				if (stringValue.equalsIgnoreCase("auto")) {
					return null;
				} else if (stringValue.startsWith("normal")) {
					try {
						String[] parts = stringValue.split(",");
						double mean = Double.parseDouble(parts[1]);
						double dev = Double.parseDouble(parts[2]);
						return new NormalProbabilityDistribution(mean, dev, 0xcafebabe);
					} catch (Exception e) {
						throw new IllegalArgumentException("expected 'auto' or 'normal,<mean>,<stddev>' or 'custom,<filename>' got " + stringValue, e);
					}

				} else if (stringValue.startsWith("custom")) {
					try {
						String[] parts = stringValue.split(",");
						String fileName = parts[1];
						System.out.println("Loading prob distrib from " + fileName);
						GroupedRandomAccessMap<String, String, Double> map = new StringToDoubleRandomAccessMap<String>();
						DefaultTwoFieldParser.getRegularTwoFieldParser(map)
								.parse(new FileInputStream(fileName));
						Map<Integer, Double> map2 = new HashMap<Integer, Double>();
						for (int i = 0; i < map.size(); ++i) {
							String key = map.getKey(i);
							map2.put(Integer.parseInt(key), map.getValue(key));
						}
						map2 = Utils.normalizeMap(map2);
						return new MapProbabilityDistribution(map2);
					} catch (Exception e) {
						throw new IllegalArgumentException("expected 'auto' or 'normal,<mean>,<stddev>' or 'custom,<filename>' got " + stringValue, e);
					}
				}
				return null;
			}
		};
		abstract Object getValue(String stringValue) throws IllegalArgumentException;
	}
	

	private EnumMap<CommandLineArgs, Object> argsMap;
	
	public Double getDouble(CommandLineArgs arg) {
		return (Double)getArgValue(arg);
	}

	public Boolean getBoolean(CommandLineArgs arg) {
		return (Boolean)getArgValue(arg);
	}

	public String getString(CommandLineArgs arg) {
		return (String)getArgValue(arg);
	}

	@SuppressWarnings("unchecked")
	public <T> T getArgValue(CommandLineArgs arg) {
		return (T)argsMap.get(arg);
	}
	
	private void help() {
		System.out.print("Expected arguments:");
		for (CommandLineArgs arg : CommandLineArgs.values()) {
			System.out.println(arg.name() + " ");
		} 
		System.out.println();
	}

	private void parseParams(Properties prop) {
		CommandLineArgs[] argNames = CommandLineArgs.values();
		if (prop.size() < argNames.length) {
			help();
			System.exit(1);
		}
		argsMap = new EnumMap<CommandLineArgs, Object>(CommandLineArgs.class);
		for (CommandLineArgs carg : argNames) {
			try {
				String p = prop.getProperty(carg.name());
				argsMap.put(carg, carg.type.getValue(p==null?p:p.trim()));
			} catch (IllegalArgumentException e) {
				help();
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public synchronized void load(InputStream inStream) throws IOException {
		Properties prop = new Properties();
		prop.load(inStream);
		parseParams(prop);
	}

}
