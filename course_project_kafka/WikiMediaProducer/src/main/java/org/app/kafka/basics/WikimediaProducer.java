package org.app.kafka.basics;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class WikimediaProducer {

    public static final Logger log = LoggerFactory.getLogger(WikimediaProducer.class.getSimpleName());


    public static void main(String[] args) throws Exception {
        String topicName = "events";
        URLConnection conn = new URL("https://stream.wikimedia.org/v2/stream/recentchange").openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        try (KafkaProducer<Long, String> producer = new KafkaProducer<>(kafkaProps())) {
            for (long eventCount = 0; ; eventCount++) {
                String event = reader.readLine();
                producer.send(new ProducerRecord<>(topicName, eventCount, event));
                log.info("Published {} to Kafka topic {}", event, topicName);
                Thread.sleep(20 * (eventCount % 20));
            }
        }
    }

    private static Properties kafkaProps() {
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092,localhost:29092,localhost:29094");
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getCanonicalName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());

        // Safe Producer
        props.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.setProperty(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, "120000");
        props.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
        props.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, "64");
        props.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");
        props.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
        return props;
    }
}

