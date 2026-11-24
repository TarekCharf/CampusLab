package ma.campuslab.reservationsservice.messaging;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MaterielReset {

    private final Map<String, CompletableFuture<String>> pending = new ConcurrentHashMap<>();

    public CompletableFuture<String> register(String correlationId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        pending.put(correlationId, future);
        return future;
    }

    public void complete(String correlationId, String result) {
        CompletableFuture<String> future = pending.remove(correlationId);
        if (future != null) {
            future.complete(result);
        }
    }

    public void fail(String correlationId, Throwable error) {
        CompletableFuture<String> future = pending.remove(correlationId);
        if (future != null) {
            future.completeExceptionally(error);
        }
    }
}
