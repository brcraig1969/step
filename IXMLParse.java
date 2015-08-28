package xml_parse;

import java.io.File;
import java.util.List;

public interface IXMLParse {

	public void parse();
	public List<String> getListOfFiles(File files);
	public String getTextFromFile(String file);
	public void outputPage (List<String> fileExtract, String outputFile);
}
