package bean;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import utility.StockDataProcessor;

/**
 * Session Bean implementation class StockMonitorBean
 */
@Stateless
@LocalBean
public class StockMonitorBean implements StockMonitorRemote, StockMonitorLocal {

	// @Resource
	// private SessionContext sc;
	//
	// @Resource(name = "jms/ConnectionFactory", mappedName="ConnectionFactory")
	// private ConnectionFactory connectionFactory;

	// @Resource(name = "jms/myQueue", mappedName="queue/myQueue")
	// private Queue queue;

	// @Resource(mappedName="ConnectionFactory")
	// private ConnectionFactory connectionFactory;

	// @Resource(name="queue/ReceptorMensagemQueue", mappedName="queue/myQueue")
	// private Destination receptor;

	private final int MAX_ALLOWED_THREADS = 10;

	/**
	 * Default constructor.
	 */
	public StockMonitorBean() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String args[]) throws SQLException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		// just testing ...
		String request = "analysis";
		new StockMonitorBean().processClientRequest(request);

	}

	public String monitorStock(String request) throws Exception {
		String reply = this.processClientRequest(request);
		System.out.println("MESSAGE BEAN: Message received: " + reply);
		return reply;
	}

	public String processClientRequest(final String request)
			throws SQLException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		// if new request => add to client request db & enqueueClientsRequest
		// if old request & already processed; reply to client with result
		// if old request & still pending; reply to client with wait message

		// Request Format : <client-req-ID> <function:analysis/consultancy>
		// <stock-symbol> <from-time> <to-time>
		String reply = null;
		String splitArray[] = request.split(" ");
		String query = null;
		ResultSet rs = null;
		utility.DatabaseUtility conn = new utility.DatabaseUtility("cmpe275");
		// Statement stmt = conn.createStatement();

		if (splitArray[0] != null) {
			if (splitArray[0].equalsIgnoreCase("analysis")
					|| splitArray[0].equalsIgnoreCase("consultancy")) {
				// new request ...
				query = "INSERT INTO `ClientRequest` ("
						+ "`serviceName` , `status`, `datetime` ) VALUES ( '"
						+ splitArray[0] + "' , 'pending', CURDATE() ) ;";

				System.out.println("Executing query : " + query + "...");
				conn.doUpdate(query);
				// give back generated ID
				query = "SELECT `clientID` FROM `clientrequest` ORDER BY "
						+ "`clientID` DESC LIMIT 0,1;";
				rs = conn.doQuery(query);
				if (rs.next())
					reply = rs.getString("clientID")
							+ " : your request has been received.. Come back later with given ID"; 
				conn.closeRS(rs);

				// put message in JMS Q; if it is on in the properties
				// Read Properties

				Properties prop = new Properties();

				final ExecutorService executor = Executors
						.newFixedThreadPool(MAX_ALLOWED_THREADS);

				try {

					FileInputStream fis = new FileInputStream(
							"stock_alert.properties");
					prop.load(fis);
					String JMSsetting = prop.getProperty("JMS");
					if (JMSsetting.equalsIgnoreCase("off")) {
						// direct call to processor and return to client
						executor.submit(new Runnable() {
							StockDataProcessor sdp = StockDataProcessor
									.getInstance();

							public void run() {
								sdp.startProcessing(request);
							}
						});

					} else
						// enqueue to JMS and return to client
						enqueueClientsRequest(request);
				} catch (IOException e) {

				}

			} else {
				query = "SELECT * FROM `ClientRequest` WHERE "
						+ "`clientID` = " + splitArray[0] + ";";
				System.out.println("sql : " + query);

				try {
					rs = conn.doQuery(query);

					if (rs.next()) {
						if (rs.getString("status").equalsIgnoreCase("pending")
								|| rs.getString("status").equalsIgnoreCase(
										"in progress")) {
							// old request & still pending
							reply = "Please wait and check back later .. ";

						} else if (rs.getString("status").equalsIgnoreCase(
								"complete")) {
							// old request & already processed

							// prepare result ...
							reply = "Here is your result data.. :"
									+ rs.getString("result");

						}
						conn.closeRS(rs);
					}

				} catch (Exception e) {
					// No ID Match or Request name Match Found
					reply = "Bad Request! \n Request Format : <client-req-ID> <function:analysis/consult> <stock-symbol> <from-time> <to-time> ";
					// System.out.println("reply : " + reply );

				}

			}

		}

		System.out.println("reply : " + reply);
		return reply;

	}

	private String enqueueClientsRequest(String msg) {

		// enQueue Processing logic
		// just put in the queue
		// set the status in the ClientRequest DB to New

		try {

			Context ctx = new InitialContext();
			ConnectionFactory connectionFactory = (ConnectionFactory) ctx
					.lookup("jms/ConnectionFactory");
			Queue queue = (Queue) ctx.lookup("jms/myQueue");
			javax.jms.Connection connection = connectionFactory
					.createConnection();
			javax.jms.Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			MessageProducer messageProducer = session.createProducer(queue);
			TextMessage message = session.createTextMessage();
			// pass the Client request to the Queue
			message.setText(msg);
			System.out.println("MDBEJB:" + message.getText());
			messageProducer.send(message);
			connection.close();
		} catch (Exception e) {
			System.out.println(e.toString());
		}

		return msg + " :)"; 

	}

}
