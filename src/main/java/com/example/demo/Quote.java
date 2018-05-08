package com.example.demo;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Quote {
	private int id;
	private String quote;
	private boolean processed;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getQuote() {
		return quote;
	}

	public void setQuote(String quote) {
		this.quote = quote;
	}

	public boolean isProcessed() {
		return processed;
	}

	public void setProcessed(boolean processed) {
		this.processed = processed;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Quote other = (Quote) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public Quote() {
		super();
	}

	@Override
	public String toString() {
		return "Quote [id=" + id + ", quote=" + quote + ", processed=" + processed + "]";
	}

	public static RowMapper<Quote> rowMapper() {
		return new RowMapper<Quote>() {
			@Override
			public Quote mapRow(ResultSet rs, int arg1) throws SQLException {
				Quote q = new Quote();
				q.setId(rs.getInt("id"));
				q.setQuote(rs.getString("quote"));
				q.setProcessed(rs.getBoolean("processed"));
				return q;
			}
		};
	}
}
