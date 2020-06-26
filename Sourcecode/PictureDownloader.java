import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * @Author: https://github.com/anre18/
 * @Version: v.1.0 
 * */

public class PictureDownloader {
	final String[] types = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".ico"}; //The File Types, that'll get extracted
	final String[] regexPatterns = {"href=\".[^\\s]*.\"", "<img src=\".[^\\s]*.\""}; //The used Regex Patterns, too distinguish possible files

	public PictureDownloader(String[] urls) { //uses given urls as Array
		for(String u : urls) { //Iterates over thoose URLS
			try {				
				ArrayList<String> validFiles = new ArrayList<String>(); //Saves valid found-files (in regard to the types)
				URL url = new URL(u);
				URLConnection uc = url.openConnection(); 
				BufferedReader bf = new BufferedReader(new InputStreamReader(uc.getInputStream())); //Reads in HTML Doc
				String s, sc = "";
				while ((s = bf.readLine()) != null) //Concates the Lines to a Full String
					sc = sc + s;
				bf.close();
				
				for(String link : scanForPatterns(sc, regexPatterns))//iterates over found links
					if(checkForWantedTypes(link))
						validFiles.add(link);
				
				for(String clean : validFiles) {
					String cleaned = cleanUpHyperlink(clean, u);
					String filename = cleaned.substring(cleaned.lastIndexOf('/')+1);
					
					try(InputStream in = new URL(cleaned).openStream()){
					    Files.copy(in, Paths.get(filename));
					}
				}
				
			} catch (Exception e) {e.printStackTrace();}
		}		
	}
	
	//Scans a given input String for given Regex Patterns
	private ArrayList<String> scanForPatterns(String input, String[] regPatterns){
		ArrayList<String> found = new ArrayList<String>();
		for(String pattern : regPatterns) {
			Pattern currentPattern = Pattern.compile(pattern);
			Matcher match = currentPattern.matcher(input); //Runns the pattern on the extracted Html-Code
			while (match.find()) 
				found.add(match.group()); //Adds found patterns to the list
		}
		return found;
	}
	
	//Returns true if string contains a wanted type
	private boolean checkForWantedTypes(String s) {
		for(String type : types)
			if(s.contains(type))
				return true;
		return false;//if no type was found		
	}
	
	//Gets the mainsites Hyperlink(the site, where the file was grabbed) and the filesString, to clean this up to a valid url
	private String cleanUpHyperlink(String inputUrl, String mainSite) { 
		inputUrl = inputUrl.substring(inputUrl.indexOf('"')+1, inputUrl.length()-1); //Cleans up the url -> removes unwanted code
		if(!inputUrl.contains("http://") && !inputUrl.contains("https://")) //Uncomplete link -> needs completion
			inputUrl = mainSite + inputUrl;
							
		return inputUrl;
	}

	public static void main(String[] args) {
		PictureDownloader pdl = new PictureDownloader(args);
	}

}
