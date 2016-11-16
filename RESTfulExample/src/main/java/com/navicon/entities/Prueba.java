package com.navicon.entities;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.navicon.util.CustomCalendarDeserializer;
import com.navicon.util.CustomCalendarSerializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Prueba {

	String title;
	String singer;
	@JsonSerialize(using = CustomCalendarSerializer.class)
    @JsonDeserialize(using = CustomCalendarDeserializer.class)
	Date date;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSinger() {
		return singer;
	}

	public void setSinger(String singer) {
		this.singer = singer;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "Track [title=" + title + ", singer=" + singer + ", date=" + date + "]";
	}
}
