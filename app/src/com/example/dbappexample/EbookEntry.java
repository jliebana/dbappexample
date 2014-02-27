package com.example.dbappexample;

import java.util.Date;

/**
 * 
 * @author jliebana
 * 
 */
public class EbookEntry {

	private Date date;
	private String title;
	private String localPath;

	public EbookEntry(Date date, String title, String localPath) {
		this.date = date;
		this.title = title;
		this.localPath = localPath;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLocalPath() {
		return localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

}
