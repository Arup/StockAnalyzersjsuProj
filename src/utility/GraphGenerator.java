package utility;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import javax.swing.*;

import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;

public class GraphGenerator extends JPanel {

	float data[] = new float[30];
	GraphGenerator() {

		Connection con = null;

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			con = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/cmpe275", "root", "password");

			if (!con.isClosed())
				System.out.println("successfully connected to "
						+ "MySQL server using TCP/IP...");

			Statement stmt = con.createStatement();

			// read config file for volume and price change setup 
			Properties prop = new Properties(); 
			FileInputStream fis;
			fis = new FileInputStream("stock_alert.properties");
			prop.load(fis);
			Float volume_change = Float.parseFloat(prop.getProperty("volume_change"));
			Float percent_change = Float.parseFloat(prop.getProperty("percent_change"));
			
			// Query to retrive fields from database where %change > preconfigured value in the file

			String ChangeQuery  = "select * from stockactivity where  percent_change > " 
				 + percent_change + " AND ((volume - avg_vol )/ avg_vol * 100 ) >= "
				 + volume_change + ";" ;  			
			
			ResultSet PercentchngRS = stmt.executeQuery(ChangeQuery);

			// retriving the data from result set
			int i = 0;
			while (PercentchngRS.next()) {
				data[i] = Float.parseFloat(PercentchngRS.getString("percent_change")); 
					//Integer.parseInt(PercentchngRS.getString("percent_change"));
				System.out.println(data[i]);
				i++;
			}

		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

	}

	final int PAD = 20;

	protected void paintComponent(Graphics g) {
		System.out.println("Drawing Graph...");
		// System.out.println(data[0]);
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		int w = getWidth();
		int h = getHeight();
		// Draw ordinate.
		g2.draw(new Line2D.Double(PAD, PAD, PAD, h - PAD));
		// Draw abcissa.
		g2.draw(new Line2D.Double(PAD, h - PAD, w - PAD, h - PAD));
		// Draw labels.
		Font font = g2.getFont();
		FontRenderContext frc = g2.getFontRenderContext();
		LineMetrics lm = font.getLineMetrics("0", frc);
		float sh = lm.getAscent() + lm.getDescent();
		// Ordinate label.
		String s = "Percent Change";
		float sy = PAD + ((h - 2 * PAD) - s.length() * sh) / 2 + lm.getAscent();
		for (int i = 0; i < s.length(); i++) {
			String letter = String.valueOf(s.charAt(i));
			float sw = (float) font.getStringBounds(letter, frc).getWidth();
			float sx = (PAD - sw) / 2;
			g2.drawString(letter, sx, sy);
			sy += sh;
		}
		// Abcissa label.
		s = "Company name";
		sy = h - PAD + (PAD - sh) / 2 + lm.getAscent();
		float sw = (float) font.getStringBounds(s, frc).getWidth();
		float sx = (w - sw) / 2;
		g2.drawString(s, sx, sy);
		// Draw lines.
		double xInc = (double) (w - 2 * PAD) / (data.length - 1);
		double scale = (double) (h - 2 * PAD) / getMax();
		g2.setPaint(Color.green.darker());
		for (int i = 0; i < data.length - 1; i++) {
			double x1 = PAD + i * xInc;
			double y1 = h - PAD - scale * data[i];
			double x2 = PAD + (i + 1) * xInc;
			double y2 = h - PAD - scale * data[i + 1];
			g2.draw(new Line2D.Double(x1, y1, x2, y2));
		}
		// Mark data points.
		g2.setPaint(Color.red);
		for (int i = 0; i < data.length; i++) {
			double x = PAD + i * xInc;
			double y = h - PAD - scale * data[i];
			g2.fill(new Ellipse2D.Double(x - 2, y - 2, 4, 4));
		}
	}

	private int getMax() {
		int max = -Integer.MAX_VALUE;
		for (int i = 0; i < data.length; i++) {
			if (data[i] > max)
				max = (int)data[i];
		}
		return max;
	}

	public static void drawGraph() {

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(new GraphGenerator());
		f.setSize(400, 400);
		f.setLocation(200, 200);
		f.setVisible(true);
	}

	public static void main(String[] args) {
		drawGraph(); 
	}	
}
