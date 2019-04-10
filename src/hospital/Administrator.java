package hospital;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class Administrator extends Participant {

    private String loggerQueue;

    Administrator() throws IOException, TimeoutException {
        super();

        this.loggerQueue = this.channel.queueDeclare().getQueue();
        this.channel.queueBind(this.loggerQueue, hospitalExchange, "hospital.#");
    }

    public static void main(String args[]) throws IOException, TimeoutException {

        Administrator admin = new Administrator();

        admin.supervise();

    }

    private void supervise() throws IOException {
        System.out.println("Now listening... You can also type a broadcast message here.");
        sniffAssignments();
        while (true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String message = br.readLine();
            broadcastInfo(message);
        }
    }

    private void sniffAssignments() throws IOException {
        Consumer adminConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                System.out.println("LOG: " + new String(body, "UTF-8"));
            }
        };
        this.channel.basicConsume(this.loggerQueue, true, adminConsumer);
    }

    private void broadcastInfo(String info) throws IOException {
        this.channel.basicPublish(adminExchange, "",null, info.getBytes());
    }

}
