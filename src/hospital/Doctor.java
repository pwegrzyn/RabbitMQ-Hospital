package hospital;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

public class Doctor extends Participant {

    private String callbackQueue;
    private String doctorName;
    private String adminQueue;

    Doctor() throws IOException, TimeoutException {
        super();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Please provide a unique doctor name: ");
        this.doctorName = br.readLine();

        this.callbackQueue = channel.queueDeclare().getQueue();
        this.channel.queueBind(callbackQueue, hospitalExchange, doctorPrefix + "." + this.doctorName);

        this.adminQueue = this.channel.queueDeclare().getQueue();
        this.channel.queueBind(this.adminQueue, adminExchange, "");
    }

    public static void main(String args[]) throws IOException, TimeoutException {

        Doctor doctor = new Doctor();

        doctor.assignJobs();

    }

    private void assignJobs() throws IOException {
        listenCallbackQueue();
        listenAdmin();
        System.out.println("Please assign jobs (format: type,patient)");
        while (true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String message = br.readLine();
            message = this.doctorName + "," + message;
            sendJob(message);
        }
    }

    private void sendJob(String job) throws IOException {
        String type = Arrays.asList(job.split(",")).get(1);
        String key = hospitalPrefix + "." + technicianPrefix + "." + type;
        this.channel.basicPublish(hospitalExchange, key,null, job.getBytes());
        System.out.println("Assignment successfully sent.");
    }

    private void listenCallbackQueue() throws IOException {
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                String message = new String(body);
                System.out.println("Received result: " + message);
            }
        };
        channel.basicConsume(this.callbackQueue, true, consumer);
    }

    private void listenAdmin() throws IOException {
        Consumer adminConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                System.out.println("Info from admin: " + new String(body, "UTF-8"));
            }
        };
        this.channel.basicConsume(this.adminQueue, true, adminConsumer);
    }

}
