package remitly.stock_market.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(path = "/chaos")
public class ChaosController {

    @PostMapping
    public ResponseEntity<Void> kill() {
        CompletableFuture.runAsync(
                () -> Runtime.getRuntime().halt(1),
                CompletableFuture.delayedExecutor(100, TimeUnit.MILLISECONDS)
        );

        return ResponseEntity.ok().build();
    }
}
