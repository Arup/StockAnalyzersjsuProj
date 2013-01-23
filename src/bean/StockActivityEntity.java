package bean;

import java.io.Serializable;
import java.util.Date;
import java.sql.Time;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "StockActivity")
// @IdClass(IdentityClass.class)
public class StockActivityEntity implements Serializable {

	@Id
	// signifies the primary key
	@Column(name = "symbol", nullable = false, length = 15)
	// @GeneratedValue(strategy = GenerationType.AUTO)
	private String symbol;

	@Id
	@Column(name = "date", nullable = false, length = 55)
	private Date date;
	@Id
	@Column(name = "time", nullable = false, length = 55)
	private Time time;

	@Column(name = "exchange", nullable = false, length = 55)
	private String exchange;

	@Column(name = "min_price", nullable = false, length = 55)
	private float min_price;

	@Column(name = "max_price", nullable = false, length = 55)
	private float max_price;

	@Column(name = "volume", nullable = false, length = 100)
	private long volume;

	@Column(name = "avg_vol", nullable = false, length = 100)
	private long avg_volume;

	@Column(name = "percent_change", nullable = false, length = 100)
	private float percentchange;

	public StockActivityEntity() {
	}

	public StockActivityEntity(String symbol, String exchange, Date date,
			Time time, float min_price, float max_price, long volume,
			long avg_volume, float percentchange) {
		this.symbol = symbol;
		this.exchange = exchange;
		this.time = time;
		this.min_price = min_price;
		this.max_price = max_price;
		this.volume = volume;
		this.avg_volume = avg_volume;
		this.percentchange = percentchange;

	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(java.util.Date date) {
		this.date = (Date) date;
	}

	public Time getTime() {
		return time;
	}

	public void setTime(Time time) {
		this.time = time;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public float getMin_price() {
		return min_price;
	}

	public void setMin_price(float min_price) {
		this.min_price = min_price;
	}

	public float getMax_price() {
		return max_price;
	}

	public void setMax_price(float max_price) {
		this.max_price = max_price;
	}

	public long getVolume() {
		return volume;
	}

	public void setVolume(long volume) {
		this.volume = volume;
	}

	public long getAvg_volume() {
		return avg_volume;
	}

	public void setAvg_volume(long avg_volume) {
		this.avg_volume = avg_volume;
	}

	public float getPercentchange() {
		return percentchange;
	}

	public void setPercentchange(float percentchange) {
		this.percentchange = percentchange;
	}

}