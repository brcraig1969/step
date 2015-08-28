package xml_parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class StepOutbound implements IXMLParse {

	private Properties properties;
	
	//Set as default values, override with values from config file if they exist
	private String headerRecord;// = "Product ID,Launch Date,Item Description,Filename";
	private String outputFile;// = "output.csv";
	private String workspacePath;// = "/Users/z048458/Documents/workspace/STEP_XML_PARSE";
	private String productIdTag;// = "Product ID";
	private String launchDateTag;// = "LNCH_D";
	private String itemDescriptionTag;// = "ITEM_DESC_GEN_T";
	private String sourceFileSuffix;// = ".xml";
	
	public StepOutbound () {
		
		//read a config file - this should be passed in as an argument
		getConfiguration("stepOutbound.properties");
	}

	public void getConfiguration(String config) {
		
		try {
			properties = new Properties();
			properties.load(new FileInputStream(config));
			for (String key: properties.stringPropertyNames()) {
				//to do: add some identifiers to flag which ones are getting set from config vs. defaults
				switch (key) {
					case "OUTPUT_FILE"		: outputFile = properties.getProperty(key);
												break;
					case "HEADER_RECORD" 	: headerRecord = properties.getProperty(key);
												break;
					case "WORKSPACE_PATH"	: workspacePath = properties.getProperty(key);
												break;
					case "PRODUCT_ID_TAG" 	: productIdTag = properties.getProperty(key);
												break;
					case "LAUNCH_DATE_TAG" 	: launchDateTag = properties.getProperty(key);
												break;
					case "ITEM_DESCRIPTION_TAG" : itemDescriptionTag = properties.getProperty(key);
												break;
					case "SOURCE_FILE_SUFFIX" : sourceFileSuffix = properties.getProperty(key);
												break;
					default :
						System.err.println("Config item not used: " + key);
						break;
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	public void parse () {

		File files = new File(workspacePath);

		List<String> names = getListOfFiles(files, sourceFileSuffix);
		List<String> fileExtract = new ArrayList<String>();
		
		for(String fileName : names) {
			fileExtract.add(getKeyFields(fileName));
		}

		outputPage(fileExtract, outputFile);
	}
	
	public List<String> getListOfFiles(File files, String extension) {

		List<String> fileNames = new ArrayList<String>();
		
		for(File fileName : files.listFiles()) {
			if(fileName.getName().endsWith(extension)) 
				fileNames.add(fileName.getName());
		}
		return fileNames;
	}
	
	public String getKeyFields(String file) {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		String productId = ",";
		String itemType = ",";
		String launchDate = ",";
		String itemDescription = ",";
		
		try {
			
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(file));

			NodeList nList = document.getElementsByTagName("Product");
			
			Node node;// = nList.item(0);
			Element element;// = (Element) node;

			boolean launchDateSet = false;		
			
			for (int i = 0; i < nList.getLength(); i++) {

				node = nList.item(i);
				element = (Element) node;
				
				//grab the highest level product
				if(node.getParentNode().getNodeName().equals("Products")) {
					productId = element.getAttribute("ID") + ",";
					itemType = element.getAttribute("UserTypeID") + ",";
					NodeList vList = element.getElementsByTagName("Value");
					
					for (int j = 0; j < vList.getLength(); j++) {
						Node value =vList.item(j);
						Element vElement = (Element) value;

						switch (vElement.getAttribute("AttributeID")) {
						case "LNCH_D" : if(!launchDateSet) {
							launchDate = vElement.getTextContent() + ",";
							launchDateSet = true;
						}
							break;
						case "ITEM_DESC_GEN_T" : itemDescription = vElement.getTextContent() + ",";
							break;
						default :
							break;
						}
					}
				}
				else
					continue;
			}
			
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace(); 
		}
		return (productId + itemType + launchDate + itemDescription + file);
	}
	
	//entire method based on assumed input file format ...definitely MVP only
	@Deprecated
	public String getTextFromFile(String file) {
		
		String productId = "";
		String launchDate = "";
		String itemDescription = "";
		String fileExtract = "";
				
		FileReader reader = null;
		BufferedReader bReader = null;

		try {
			reader = new FileReader(file);
			bReader = new BufferedReader(reader);
			String line = "";
					
			int lineCount = 0;
			int beginIndex = -1;
			int endIndex = -1;
			//one file may have multiple LNCH_D records due to collections.  Grab the first one only
			boolean launchDateSet = false;		
					
			while ((line = bReader.readLine() )!= null) {
				lineCount++;
				
				//assumes Product ID is always on line 4 in the step xml format
				if (line.contains(productIdTag) && lineCount == 4) {
					if (line.indexOf("=") > 0) {
						beginIndex = line.indexOf("\"");
						endIndex = line.indexOf("\" ");
						productId = line.substring(beginIndex + 1, endIndex);
						//System.out.println("Product Id: " + productId);
						fileExtract += productId + ",";
					}
				}	

				if (line.contains(launchDateTag) && launchDateSet == false) {
					if (line.indexOf(">") > 0) {
						beginIndex = line.indexOf(">");
						endIndex = line.indexOf("</Value>");
						launchDate = line.substring(beginIndex + 1, endIndex);
						//System.out.println("Launch Date: " + launchDate);
						fileExtract += launchDate + ",";
						launchDateSet = true;	
					}
				}
						
				if(line.contains(itemDescriptionTag)) {
					if (line.indexOf(">") > 0) {
						beginIndex = line.indexOf(">");
						endIndex = line.indexOf("</Value>");
						itemDescription = line.substring(beginIndex + 1, endIndex);
						//System.out.println("Item Description: " + itemDescription);
						fileExtract += itemDescription + ",";
					}
				}
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (bReader != null)
				try {
					reader.close();	
					bReader.close();
				}
			catch (IOException e) {
				e.printStackTrace();
			}
				
		}
		fileExtract += file;
		return fileExtract;
	}
	
	public void outputPage (List<String> fileExtract, String outputFile) {
		
		PrintWriter writer = null;
		
		File output = new File(workspacePath + "/" + outputFile);

		boolean writeHeaderRecord = false;

		try {
			
			if(!output.exists())
				writeHeaderRecord = true;
			
			writer = new PrintWriter(new FileWriter(outputFile, true));
			
			if(writeHeaderRecord)
				writer.println(headerRecord);
			
			for(String record : fileExtract) {				
				writer.println(record);
			}
			
		} catch (IOException e) {
				e.printStackTrace();
		}
		finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	@Override
	public List<String> getListOfFiles(File files) {
		// TODO Auto-generated method stub
		return null;
	}
}
