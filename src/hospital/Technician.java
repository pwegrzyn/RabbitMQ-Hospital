package hospital;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class Technician extends Participant {

    private final String spec1;
    private final String spec2;
    private String queue1;
    private String queue2;
    private String queue1Key;
    private String queue2Key;
    private String adminQueue;

    Technician() throws IOException, TimeoutException {
        super();
        this.channel.basicQos(1);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter first specialization (knee|hip|elbow): ");
        this.spec1 = br.readLine();
        System.out.println("Enter second specialization (knee|hip|elbow): ");
        this.spec2 = br.readLine();

        assignQueues();

        declareTechQueues();
        declareAdminQueue();
    }

    public static void main(String args[]) throws IOException, TimeoutException {

        Technician technician = new Technician();

        technician.handleRequests();

    }

    private void assignQueues() {
        if(this.spec1.equals("hip")) {
            this.queue1 = hipTechnicianQueue;
            this.queue1Key = hipTechnicianQueueKey;
        } else if(this.spec1.equals("knee")) {
            this.queue1 = kneeTechnicianQueue;
            this.queue1Key = kneeTechnicianQueueKey;
        } else if(this.spec1.equals("elbow")) {
            this.queue1 = elbowTechnicianQueue;
            this.queue1Key = elbowTechnicianQueueKey;
        } else {
            System.err.println("Invalid Specialization!");
            System.exit(1);
        }

        if(this.spec2.equals("hip")) {
            this.queue2 = hipTechnicianQueue;
            this.queue2Key = hipTechnicianQueueKey;
        } else if(this.spec2.equals("knee")) {
            this.queue2 = kneeTechnicianQueue;
            this.queue2Key = kneeTechnicianQueueKey;
        } else if(this.spec2.equals("elbow")) {
            this.queue2 = elbowTechnicianQueue;
            this.queue2Key = elbowTechnicianQueueKey;
        } else {
            System.err.println("Invalid Specialization!");
            System.exit(1);
        }
    }

    private void declareTechQueues() throws IOException {
        this.channel.queueDeclare(this.queue1, false, false, false, null);
        this.channel.queueBind(this.queue1, hospitalExchange, this.queue1Key);
        this.channel.queueDeclare(this.queue2, false, false, false, null);
        this.channel.queueBind(this.queue2, hospitalExchange, this.queue2Key);
    }

    private void declareAdminQueue() throws IOException {
        this.adminQueue = this.channel.queueDeclare().getQueue();
        this.channel.queueBind(this.adminQueue, adminExchange, "");
    }

    private void handleRequests() throws IOException {
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received assignment: " + message);
                respondToAssignment(message);
            }
        };
        System.out.println("Waiting for requests...");
        this.channel.basicConsume(this.queue1, true, consumer);
        this.channel.basicConsume(this.queue2, true, consumer);

        Consumer adminConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Info from admin: " + message);
            }
        };
        this.channel.basicConsume(this.adminQueue, true, adminConsumer);
    }

    private void respondToAssignment(String job) throws IOException {
        List<String> parsedJob = Arrays.asList(job.split(","));
        String doctor = parsedJob.get(0);
        String type = parsedJob.get(1);
        String patient = parsedJob.get(2);
        String response = patient + "," + type + "," + "done";
        String key = doctorPrefix + "." + doctor;
        this.channel.basicPublish(hospitalExchange, key,null, response.getBytes());
        System.out.println("Results sent back.");
    }

}
