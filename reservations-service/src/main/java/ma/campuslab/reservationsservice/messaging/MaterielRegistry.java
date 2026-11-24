package ma.campuslab.reservationsservice.messaging;

import ma.campuslab.reservationsservice.dto.MaterielDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MaterielRegistry {

    private final Map<String, CompletableFuture<List<MaterielDto>>> pending = new ConcurrentHashMap<>();

    public CompletableFuture<List<MaterielDto>> register(String correlationId) {
        CompletableFuture<List<MaterielDto>> future = new CompletableFuture<>();
        pending.put(correlationId, future);
        return future;
    }

    public void complete(String correlationId, List<MaterielDto> result) {
        CompletableFuture<List<MaterielDto>> future = pending.remove(correlationId);
        if (future != null) {
            future.complete(result);
        }
    }

    public void fail(String correlationId, Throwable error) {
        CompletableFuture<List<MaterielDto>> future = pending.remove(correlationId);
        if (future != null) {
            future.completeExceptionally(error);
        }
    }
}