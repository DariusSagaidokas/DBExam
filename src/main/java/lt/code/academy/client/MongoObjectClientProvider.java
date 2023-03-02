package lt.code.academy.client;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

public class MongoObjectClientProvider {
    private static MongoClient client;

    private MongoObjectClientProvider() {
        CodecRegistry registry = CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), registry);
        MongoClientSettings settings = MongoClientSettings.builder().codecRegistry(codecRegistry).build();

        client = MongoClients.create(settings);
    }

    public static MongoClient getClient() {
        if(client == null) {
            new MongoObjectClientProvider();
        }

        return client;
    }
}
