package io.stricte.spring.gcp;

import com.google.cloud.spanner.Key;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gcp.data.spanner.core.mapping.PrimaryKey;
import org.springframework.cloud.gcp.data.spanner.core.mapping.Table;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.UUID;

@SpringBootApplication
public class GcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(GcpApplication.class, args);
    }

}

@RestController
@RequiredArgsConstructor
@Slf4j
class HelloController {

    private final PersonRepository repository;

    private final PubSubConfig.PubsubOutboundGateway gateway;

    @GetMapping("/hello/{name}")
    String hello(@PathVariable String name) {

        log.info("Hello, {}!", name);

        Person person = new Person();
        person.setName(name);

        Person saved = repository.save(person);

        return "Hello " + saved.getName();
    }

    @GetMapping("/hello/{name}/publish")
    String helloPubSub(@PathVariable String name) {

        log.info("Hello, {}! (Pub/Sub)", name);

        gateway.sendToPubsub(
            new HelloMessage(
                name,
                UUID.randomUUID().toString().replaceAll("-", "")
            ).toString()
        );

        return "Hello, " + name + " (Pub/Sub)";
    }
}

@Data
//TODO: moved to gcp spanner
//@Entity
@Table(name = "people")
class Person {

    //@GeneratedValue(strategy = GenerationType.AUTO)
    //@Id
    //long id;
    @PrimaryKey
    String id = UUID.randomUUID().toString().replaceAll("-", "");

    String name;
}

@RepositoryRestResource
interface PersonRepository extends PagingAndSortingRepository<Person, Key> {

    Collection<Person> findByName(String name);
}
