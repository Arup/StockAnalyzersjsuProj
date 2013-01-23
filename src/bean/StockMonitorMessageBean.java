package bean;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.jboss.ejb3.annotation.ResourceAdapter;

import utility.StockDataProcessor;


/**
 * Message-Driven Bean implementation class for: StockMonitorMessageBean
 *
 */
	
//@org.jboss.ejb3.annotation.Depends("org.hornetq:module=JMS,name=\"myQueue\",type=Queue")
//@ResourceAdapter("jms-ra.rar")
@MessageDriven(   
		activationConfig= {   
		@ActivationConfigProperty(propertyName="destination",propertyValue="queue/myQueue"),   
		@ActivationConfigProperty(propertyName="destinationType",propertyValue="javax.jms.Queue")
//		@ActivationConfigProperty(propertyName = "user", propertyValue = "guest"),
//    	@ActivationConfigProperty(propertyName = "password", propertyValue = "guest")

		}, mappedName="myQueue"  
		)  		
		
public class StockMonitorMessageBean implements MessageListener {

    /**
     * Default constructor. 
     */
    public StockMonitorMessageBean() {
        // TODO Auto-generated constructor stub
    }
	
    /**
     * @see MessageListener#onMessage(Message)
     */
    public void onMessage(Message message) {
    	// TODO Auto-generated method stub



    	try {

    		TextMessage textMsg = (TextMessage)message;
    		String text = textMsg.getText();
    		System.out.println("\n MESSAGE RECIEVED:\n"+text);

    		// Start deque-ing request .. they will be delegated to processing later on ... 
    		this.dequeueClientsRequest(text); 

    	} catch(JMSException jmsE) {
    		jmsE.printStackTrace();
    	}


    }


    private void dequeueClientsRequest(String msg) {
    	// Queue Processing logic
    	// Look for StockDataRecorder & StockDataCalculator is finished with processing 
    	// then prepare result and send back to caller 
    	// if more request on queue - take one and set the corresponding status 
    	// when done, set the status and look for more requests to process
    	// -- all the Logic is in StockDataProcessor -> startProcessing... 

    	StockDataProcessor sdp = StockDataProcessor.getInstance();  
    	sdp.startProcessing(msg);           

    }

}
