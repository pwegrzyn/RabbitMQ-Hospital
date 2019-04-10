package hospital;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public abstract class Participant {

    protected Channel channel;
    protected final static String hospitalExchange = "HOSPITAL_EXCHANGE";
    protected final static String adminExchange = "ADMIN_EXCHANGE";
    protected final static String hospitalPrefix = "hospital";
    protected final static String technicianPrefix = "tech";
    protected final static String doctorPrefix = hospitalPrefix + ".doctor";
    protected final static String elbowTechnicianQueueKey = hospitalPrefix + "." + technicianPrefix + ".elbow";
    protected final static String kneeTechnicianQueueKey = hospitalPrefix + "." + technicianPrefix + ".knee";
    protected final static String hipTechnicianQueueKey = hospitalPrefix + "." + technicianPrefix + ".hip";
    protected final static String hipTechnicianQueue = "TECHNICIAN_HIP";
    protected final static String elbowTechnicianQueue = "TECHNICIAN_ELBOW";
    protected final static String kneeTechnicianQueue = "TECHNICIAN_KNEE";


    Participant() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        this.channel = connection.createChannel();

        this.channel.exchangeDeclare(hospitalExchange, BuiltinExchangeType.TOPIC);
        this.channel.exchangeDeclare(adminExchange, BuiltinExchangeType.FANOUT);
    }

}
