import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import utils.KafkaConfiguration;
import java.util.Scanner;

public class UserProducer {

    private static final String TOPIC_BALANCE = "raw-conversation";


    public static void main(String[] args) {

        KafkaSender<String, String> balanceSender = KafkaConfiguration.createSender(StringSerializer.class, StringSerializer.class, "Name");

        try (Scanner scanner = new Scanner(System.in)) {
          System.out.println("Enter your name:");
          String senderName = scanner.nextLine().trim();

          System.out.println("Enter recipient name:");
          String targetUserName = scanner.nextLine().trim();

            // Prompt for messages to send
            System.out.println("Enter messages (type 'exit' to quit):");
            // Create a Flux from user input
            Flux.<ProducerRecord<String, String>>create(sink -> {
                        while (scanner.hasNextLine()) {
                            String input = scanner.nextLine();
                            if ("exit".equalsIgnoreCase(input)) {
                                sink.complete(); // Complete the Flux stream
                                return;
                            } else {
                                sink.next(new ProducerRecord<>(TOPIC_BALANCE, targetUserName, input));
                            }
                        }
                    })
                    .doOnNext(x -> System.out.printf("%s -> %s:  %s\n", senderName, x.key(), x.value()))
                    .map(x -> Mono.just(SenderRecord.create(x, System.currentTimeMillis())))
                    .flatMap(balanceSender::send)
                    .then()
                    .block();
        }
    }
}