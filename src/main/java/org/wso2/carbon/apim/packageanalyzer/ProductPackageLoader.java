package org.wso2.carbon.apim.packageanalyzer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apim.packageanalyzer.entity.JARFile;


public class ProductPackageLoader {
    private static final Log log = LogFactory.getLog(ProductPackageLoader.class);
    private static String findUsesRegex = "(\\;*uses:=\"((([a-zA-Z0-9_*]*)\\.*\\,*)*)\")";
    private static Pattern patternForUses = Pattern.compile(findUsesRegex);
    
    private static String findVersionRegex = "(\\;*version=\"((([a-zA-Z0-9_*-*]*)\\.*)*)\")";
    private static Pattern patternForVersion = Pattern.compile(findVersionRegex);

    public static void main(String[] args) throws Throwable {

	PropertyLoader.init();
	
	Map<String,String> exportPkgs = new HashMap<String,String>();
	Map<String,String> exportPkgsWithoutVersion = new HashMap<String,String>();
	
	Map<String,String> duplicates = new HashMap<String,String>();
	Map<String,String> duplicatesWithVersion = new HashMap<String,String>();

	Map<String, JARFile> artifacts = getPluginArtifacts(Constants.COMPONENTS_DIR);
	Set<Entry<String, JARFile>> entries = artifacts.entrySet();
	StringBuilder allPkgs = new StringBuilder();
	StringBuilder allJars = new StringBuilder();
	List<String> allDupWithoutVersionPkgOnly =  new ArrayList<String>();
	int count = 0;
	for (Entry<String, JARFile> entry : entries) {
	    JARFile jar = entry.getValue();
	    log.info("JAR file : "+jar.toString() + " count : "+ ++count);
	    String fileName = entry.getKey();
	    String exports = jar.getExportPkgs();
	    String[] pkgs = exports.split(",");
	    allJars.append("rm ").append(fileName).append("\n");
	    for(String pkg : pkgs){
		pkg = pkg.trim();
		allPkgs.append(pkg).append("\n");
		String withoutVersion = patternForVersion.matcher(pkg).replaceAll("");
		String key = fileName+":"+pkg;
		String keyWithoutVersion = fileName+":"+withoutVersion;
		
		if(exportPkgs.containsKey(pkg)){
		    duplicates.put(key, pkg);
		    String  duplicateFile = exportPkgs.get(pkg);
		    String dupKey = duplicateFile+":"+pkg;
		    duplicates.put(dupKey, pkg);
		}else{
		    exportPkgs.put(pkg, fileName);
		    
		}
		
		if(exportPkgsWithoutVersion.containsKey(withoutVersion)){
		    duplicatesWithVersion.put(keyWithoutVersion, withoutVersion);
		    String  duplicateFile = exportPkgsWithoutVersion.get(withoutVersion);
		    String dupKey = duplicateFile+":"+withoutVersion;
		    duplicatesWithVersion.put(dupKey, withoutVersion);
		    allDupWithoutVersionPkgOnly.add(withoutVersion);
		}else{
		    exportPkgsWithoutVersion.put(withoutVersion, fileName);
		}
	    }
	}
	
	Collections.sort(allDupWithoutVersionPkgOnly, new Comparator<String>() {

	    public int compare(String o1, String o2) {
		return o1.compareToIgnoreCase(o2);
	    }
	    
	});
	
	StringBuilder allDupPkgWV = new StringBuilder();
	for(String pkg : allDupWithoutVersionPkgOnly){
	    allDupPkgWV.append(pkg).append("\n");
	}
	
	log.info("Duplicates with version : "+duplicates.size());
	log.info("Duplicates without version : "+duplicatesWithVersion.size());
	FileHandler.writingToFile(PropertyLoader.getPropertyValue(Constants.PRODUCT_DIR)+"/DupWithVersion.txt", duplicates.toString());
	FileHandler.writingToFile(PropertyLoader.getPropertyValue(Constants.PRODUCT_DIR)+"/DupWithoutVersion.txt", duplicatesWithVersion.toString());
	FileHandler.writingToFile(PropertyLoader.getPropertyValue(Constants.PRODUCT_DIR)+"/exports.txt", allPkgs.toString());
	FileHandler.writingToFile(PropertyLoader.getPropertyValue(Constants.PRODUCT_DIR)+"/allplugins.sh", allJars.toString());
	FileHandler.writingToFile(PropertyLoader.getPropertyValue(Constants.PRODUCT_DIR)+"/allDupsPkgsWV.txt", allDupPkgWV.toString());

    }

    private static Map<String, JARFile> getPluginArtifacts(final String directoryType) throws Throwable {
	String basePath = PropertyLoader.getPropertyValue(directoryType);
	Map<String, JARFile> artifacts = new HashMap<String, JARFile>();
	findPlugins(basePath, artifacts);
	return artifacts;
    }

    public static void findPlugins(String basePath, Map<String, JARFile> artifacts) throws Throwable {
	log.debug("Scanning path: " + basePath);

	File file = null;
	File folder = new File(basePath);
	StringBuilder allPluginInfo = new StringBuilder();

	// Get the list of files in particular directory
	File[] listFiles = folder.listFiles();
	if (listFiles == null || listFiles.length == 0) {
	    return;
	}
	ArrayList<File> listOfFiles = new ArrayList<File>(Arrays.asList(listFiles));
	if (listOfFiles.size() == 0) {
	    log.info("No files found for the scan: " + folder.getName());
	    return;
	}

	// Check the next level in the directory for files and directory
	for (int i = 0; i < listOfFiles.size(); i++) {
	    file = listOfFiles.get(i);

	    if (artifacts.containsKey(file.getName())) {
		continue;
	    }

	    // If the file is a file not a folder and if files has the
	    // expected pattern
	    if (file.isFile()) {
		String content = null;
		try {
		    log.info("++++++++++++++++++++++++++++++++++++++++++++ " + file.getName());
		    
		    
		    content = FileHandler.readZipFile(basePath + "/" + file.getName(), "META-INF/MANIFEST.MF");
		    
		    Manifest manifest = new Manifest(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
		    String exportPkgs = manifest.getMainAttributes().getValue("Export-Package");
		    
		    if (exportPkgs != null) {
			StringBuilder wholeString = new StringBuilder();
			String onlyPkgs = patternForUses.matcher(exportPkgs).replaceAll("");
			wholeString.append(onlyPkgs);
			/*String[] arrayOfString = exportPkgs.split(";");
			
			if (arrayOfString.length > 1) {
			    for (String each : arrayOfString) {
				log.info("######################################### " + each);
				 String onlyPkgs = pattern.matcher(each).replaceAll("");
				 wholeString.append(onlyPkgs).append(";");
				
			    }
			   
			}else{
			    String onlyPkgs = pattern.matcher(exportPkgs).replaceAll("");
			    wholeString.append(onlyPkgs);
			}*/

			allPluginInfo.append(wholeString.toString());
			artifacts.put(file.getName(), getComponent(file,wholeString.toString()));
			log.info("--------------------------------  " + wholeString.toString());

		    }
		    
		    
		    
		} catch (Throwable ex) {
		    String msg = "**********************-Error-************************  " + file.getName() + "\n" + content; 
		    System.out.println(msg);
		    log.error(msg + "\n" +ex);
		    throw ex;
		}

	    } else {
		 log.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% " + file.getName());
		findPlugins(file.getName(), artifacts);
	    }

	}

	log.info( allPluginInfo.toString());
    }

    private static JARFile getComponent(final File file, final String expPkg) throws Exception {

	String fileName = file.getName();
	int indexOfUnderscore = fileName.indexOf("_");
	int indexOfJar = fileName.lastIndexOf(".jar");
	String componentName = null;
	String version = null;
	try {
	    componentName = fileName.substring(0, indexOfUnderscore);
	    version = fileName.substring(indexOfUnderscore + 1, indexOfJar);
	} catch (Exception ex) {
	    System.out.println(" indexOfUnderscore : " + indexOfUnderscore
		    + " --------------------------  indexOfJar : " + indexOfJar);
	    throw ex;
	}

	return new JARFile(file, componentName, version,expPkg);

    }
}
