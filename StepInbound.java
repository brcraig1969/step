package xml_parse;

import java.io.File;
import java.io.FileInputStream;
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

public class StepInbound {

	private String headerRecord;// = "Product ID,Parent ID,Dept,Class,Item,Product,User ID,Item Description";
	private String outputFile;// = "errorFileParse.csv";
	private String workspacePath;// = "/Users/z048458/Documents/workspace/STEP_XML_PARSE/input";
	private String inputFileName;
	
	public StepInbound () {
	
		//assumes property is in the same directory as run env
		getConfiguration("stepInbound.properties");
	}
	
	public void getConfiguration(String config) {
		
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream(config));
			for (String key: properties.stringPropertyNames()) {
				//to do: add some identifiers to flag which ones are getting set from config vs. defaults
				switch (key) {
					case "OUTPUT_FILE"		: outputFile = properties.getProperty(key);
												break;
					case "HEADER_RECORD" 	: headerRecord = properties.getProperty(key);
												break;
					case "INBOUND_FILE_PATH"	: workspacePath = properties.getProperty(key);
												break;
					case "INPUT_FILE_NAME"	: inputFileName = properties.getProperty(key);
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
		
	public void parse() {
		
		String inputFile = workspacePath + "/" + inputFileName;
		
		outputPage(getKeyFields(inputFile),outputFile);
	}

	public List<String> getKeyFields(String file) {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		List<String> records = new ArrayList<String>();
		
		try {
			
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(file));

			NodeList nList = document.getElementsByTagName("Product");
			
			String productId;
			String parentId;
			String department;
			String mClass;
			String item;
			String product;				
			String userId;				
			String itemDescription;				
		
			
			for(int i = 0; i < nList.getLength(); i++) {
				//initialize with comma to preserve output file format if element is not found
				//pretty Kludgy
				productId = ",";
				parentId = ",";
				department = ",";
				mClass = ",";
				item =",";
				product = ",";				
				userId = ",";				
				itemDescription = ",";				

				//replace hard codes with properties

				Node node = nList.item(i);
				if(node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;

					productId = element.getAttribute("ID") + ",";
					parentId = element.getAttribute("ParentID") + ",";
					
					NodeList vList = element.getElementsByTagName("Value");
					for(int j = 0; j < vList.getLength(); j++) {
						Node value =vList.item(j);
						Element vElement = (Element) value;
						
						switch (vElement.getAttribute("AttributeID")) {
							case "MDSE_DEPT_I" : department = vElement.getTextContent() + ",";
								break;
							case "MDSE_CLAS_I" : mClass = vElement.getTextContent() + ",";
								break;
							case "ITEM_I" : item = vElement.getTextContent() + ",";
								break;
							case "PROD_I" : product = vElement.getTextContent() + ",";
								break;
							case "IMN_ITEM_CRTE_USER_I" : userId = vElement.getTextContent() + ",";
								break;
							case "ITEM_DESC_TEMP_T" : itemDescription = vElement.getTextContent() + ",";
								break;
							default :
								break;
						}
					}
				}
				records.add(productId + parentId + department + mClass + item + product + userId + itemDescription);
			}
			
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace(); 
		}
		return records;
	}

	public void outputPage (List<String> records, String outputFile) {
		
		PrintWriter writer = null;
		
		File output = new File(workspacePath + "/" + outputFile);

		boolean writeHeaderRecord = false;

		try {
			
			if(!output.exists())
				writeHeaderRecord = true;
			
			//create a new file each time.  will overwrite existing file
			writer = new PrintWriter(new FileWriter(outputFile));
			
			if(writeHeaderRecord)
				writer.println(headerRecord);
			
			for(String record : records) {				
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
}
