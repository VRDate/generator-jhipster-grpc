package <%= packageName %>.grpc;

<%_ if (databaseType === 'cassandra') { _%>
import <%= packageName %>.AbstractCassandraTest;
<%_ } _%>
import <%= packageName %>.<%=mainClass%>;
<%_ if (authenticationType === 'uaa' && applicationType !== 'uaa') { _%>
import <%= packageName %>.config.SecurityBeanOverrideConfiguration;
<%_ } _%>

import com.google.protobuf.Empty;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

<%_ if (authenticationType === 'uaa' && applicationType !== 'uaa') { _%>
@SpringBootTest(classes = {SecurityBeanOverrideConfiguration.class, <%= mainClass %>.class})
<%_ } else { _%>
@SpringBootTest(classes = <%= mainClass %>.class)
<%_ } _%>
public class HealthServiceIntTest <% if (databaseType === 'cassandra') { %>extends AbstractCassandraTest <% } %>{

    @Autowired
    HealthAggregator healthAggregator;

    @Autowired
    Map<String, org.springframework.boot.actuate.health.HealthIndicator> healthIndicators;

    private Server mockServer;

    private HealthServiceGrpc.HealthServiceBlockingStub stub;

    @BeforeEach
    public void setUp() throws IOException {
        HealthService service = new HealthService(healthAggregator, healthIndicators);
        String uniqueServerName = "Mock server for " + HealthService.class;
        mockServer = InProcessServerBuilder
            .forName(uniqueServerName).directExecutor().addService(service).build().start();
        InProcessChannelBuilder channelBuilder =
            InProcessChannelBuilder.forName(uniqueServerName).directExecutor();
        stub = HealthServiceGrpc.newBlockingStub(channelBuilder.build());
    }

    @AfterEach
    public void tearDown() throws Exception {
        mockServer.shutdownNow();
        mockServer.awaitTermination(10, TimeUnit.SECONDS);
    }

    @Test
    public void testHealth() {
        Health health = stub.getHealth(Empty.newBuilder().build());
        assertThat(health.getStatus()).isNotEqualTo(Status.UNKNOWN);
    }
}
