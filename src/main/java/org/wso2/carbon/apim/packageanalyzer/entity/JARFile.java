package org.wso2.carbon.apim.packageanalyzer.entity;

import java.io.File;

public class JARFile {
	
	private final File file;
	private final String component;
	private final String version;
	private final String exportPkgs;
	
	public JARFile(File file, String component, String version, String exportPkgs) {
	    super();
	    this.file = file;
	    this.component = component;
	    this.version = version;
	    this.exportPkgs = exportPkgs;
	}

	public File getFile() {
		return file;
	}

	public String getComponent() {
		return component;
	}

	public String getVersion() {
		return version;
	}
	

	public String getExportPkgs() {
	    return exportPkgs;
	}

	@Override
	public String toString() {
		return "JARFile [file=" + file.getName() + ", component=" + component
				+ ", version=" + version + "]";
	}
	
	

}
