package ma.campuslab.reservationsservice.config;

import ma.campuslab.usagersservice.grpc.UsagerServiceGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcClientConfig {

    @Bean
    UsagerServiceGrpc.UsagerServiceBlockingStub usagerStub(GrpcChannelFactory channels,
   @Value("${spring.grpc.client.channels.usagers.address}") String target)
    {
        return UsagerServiceGrpc.newBlockingStub(channels.createChannel(target));
    }
}
