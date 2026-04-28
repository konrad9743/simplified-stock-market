package remitly.stock_market.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import remitly.stock_market.integration.BaseIntegrationTest;
import remitly.stock_market.repository.AuditLogRepository;
import remitly.stock_market.repository.BankStockRepository;
import remitly.stock_market.repository.WalletStockRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class StockMarketIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BankStockRepository bankStockRepository;

    @Autowired
    private WalletStockRepository walletStockRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void cleanDatabase() {
        auditLogRepository.deleteAll();
        walletStockRepository.deleteAll();
        bankStockRepository.deleteAll();
    }

    // -------------------------------------------------------------------------
    // IT_01 — Buy: bank spada o 1, portfel rośnie o 1
    // -------------------------------------------------------------------------

    @Test
    void buy_decreasesBankByOne_andIncreasesWalletByOne() {
        setBankState(Map.of("AAPL", 10));

        ResponseEntity<Void> response = trade("wallet-1", "AAPL", "buy");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getBankQuantity("AAPL")).isEqualTo(9);
        assertThat(getWalletQuantity("wallet-1", "AAPL")).isEqualTo(1);
    }

    @Test
    void buy_createsWallet_whenItDoesNotExist() {
        setBankState(Map.of("AAPL", 5));

        trade("new-wallet", "AAPL", "buy");

        assertThat(getWalletQuantity("new-wallet", "AAPL")).isEqualTo(1);
    }

    @Test
    void buy_returns404_whenStockNotInBank() {
        // Bank jest pusty — stock "UNKNOWN" nie istnieje w ogóle
        ResponseEntity<Void> response = trade("wallet-1", "UNKNOWN", "buy");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void buy_returns400_whenBankHasNoStock() {
        setBankState(Map.of("AAPL", 0));

        ResponseEntity<Void> response = trade("wallet-1", "AAPL", "buy");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // -------------------------------------------------------------------------
    // IT_02 — Sell: bank rośnie o 1, WalletStock usuwany gdy quantity=0
    // -------------------------------------------------------------------------

    @Test
    void sell_increasesBankByOne_andDecreasesWallet() {
        setBankState(Map.of("AAPL", 3));
        trade("wallet-1", "AAPL", "buy");
        trade("wallet-1", "AAPL", "buy");
        trade("wallet-1", "AAPL", "buy");
        // Bank ma 0, portfel ma 3

        ResponseEntity<Void> response = trade("wallet-1", "AAPL", "sell");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getBankQuantity("AAPL")).isEqualTo(1);
        assertThat(getWalletQuantity("wallet-1", "AAPL")).isEqualTo(2);
    }
    @Test
    void sell_removesWalletStockRecord_whenQuantityReachesZero() {
        setBankState(Map.of("AAPL", 1));
        trade("wallet-1", "AAPL", "buy");
        setBankState(Map.of("AAPL", 0));

        trade("wallet-1", "AAPL", "sell");

        boolean recordExists = walletStockRepository
                .findByWalletId("wallet-1")
                .stream()
                .anyMatch(s -> s.getStockName().equals("AAPL"));

        assertThat(recordExists).isFalse();
    }

    @Test
    void sell_returns404_whenStockNotInBank() {
        // Stock nie istnieje w banku w ogóle
        ResponseEntity<Void> response = trade("wallet-1", "UNKNOWN", "sell");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void sell_returns400_whenWalletDoesNotOwnStock() {
        setBankState(Map.of("AAPL", 10));

        ResponseEntity<Void> response = trade("wallet-without-stock", "AAPL", "sell");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // -------------------------------------------------------------------------
    // IT_03 — setBankState jest atomowy (rollback przy błędzie)
    // -------------------------------------------------------------------------

    @Test
    void setBankState_isAtomic_doesNotPartiallyUpdateOnValidationError() {
        // Ustawiamy stan początkowy
        setBankState(Map.of("AAPL", 50));
        assertThat(getBankQuantity("AAPL")).isEqualTo(50);

        // Wysyłamy request z jednym poprawnym i jednym niepoprawnym stockiem
        String body = """
                {
                  "stocks": [
                    {"name": "GOOGL", "quantity": 100},
                    {"name": "INVALID", "quantity": -1}
                  ]
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl() + "/stocks",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Bank musi zostać w stanie sprzed requestu — nie może być pusty
        // Gdyby deleteAll było poza transakcją, bank byłby wyczyszczony
        assertThat(getBankQuantity("AAPL")).isEqualTo(50);
        assertThat(bankStockRepository.findByName("GOOGL")).isEmpty();
    }

    @Test
    void setBankState_replacesEntireState() {
        setBankState(Map.of("AAPL", 10, "GOOGL", 5));
        setBankState(Map.of("MSFT", 20));

        // AAPL i GOOGL powinny zniknąć, tylko MSFT zostaje
        assertThat(bankStockRepository.findByName("AAPL")).isEmpty();
        assertThat(bankStockRepository.findByName("GOOGL")).isEmpty();
        assertThat(getBankQuantity("MSFT")).isEqualTo(20);
    }

    // -------------------------------------------------------------------------
    // IT_04 — Race condition: 10 wątków kupuje 1 stock
    // -------------------------------------------------------------------------

    @Test
    void concurrentBuy_doesNotOversell_whenOnlyOneStockAvailable() throws InterruptedException {
        setBankState(Map.of("AAPL", 1));

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch go = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final String walletId = "wallet-" + i;
            futures.add(executor.submit(() -> {
                ready.countDown();
                try {
                    go.await(); // wszystkie wątki startują jednocześnie
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                ResponseEntity<Void> response = trade(walletId, "AAPL", "buy");
                if (response.getStatusCode() == HttpStatus.OK) {
                    successCount.incrementAndGet();
                } else {
                    failCount.incrementAndGet();
                }
            }));
        }

        ready.await(); // czekaj aż wszystkie wątki są gotowe
        go.countDown(); // start!

        executor.shutdown();
        assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        // Dokładnie 1 transakcja mogła kupić, reszta powinna dostać 400
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(9);

        // Bank musi mieć dokładnie 0 — nie ujemny
        assertThat(getBankQuantity("AAPL")).isEqualTo(0);

        // Audit log musi mieć dokładnie 1 wpis
        assertThat(auditLogRepository.count()).isEqualTo(1);
    }

    @Test
    void concurrentBuy_handlesMultipleStocksCorrectly() throws InterruptedException {
        setBankState(Map.of("AAPL", 5));

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch go = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final String walletId = "wallet-" + i;
            executor.submit(() -> {
                ready.countDown();
                try { go.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                if (trade(walletId, "AAPL", "buy").getStatusCode() == HttpStatus.OK) {
                    successCount.incrementAndGet();
                }
            });
        }

        ready.await();
        go.countDown();
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        assertThat(successCount.get()).isEqualTo(5);
        assertThat(getBankQuantity("AAPL")).isEqualTo(0);
        assertThat(auditLogRepository.count()).isEqualTo(5);
    }

    // -------------------------------------------------------------------------
    // IT_05 — Audit log zachowuje kolejność operacji
    // -------------------------------------------------------------------------

    @Test
    void auditLog_recordsOnlySuccessfulOperations_inOrder() {
        setBankState(Map.of("AAPL", 3));

        trade("wallet-1", "AAPL", "buy");
        trade("wallet-1", "AAPL", "buy");
        trade("wallet-2", "AAPL", "buy");
        trade("wallet-1", "UNKNOWN", "buy"); // 404 — nie powinno trafić do logu

        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl() + "/log", Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<Map<String, String>> log = (List<Map<String, String>>) response.getBody().get("log");

        assertThat(log).hasSize(3); // tylko sukcesy
        assertThat(log.get(0)).containsEntry("wallet_id", "wallet-1").containsEntry("stock_name", "AAPL").containsEntry("type", "buy");
        assertThat(log.get(1)).containsEntry("wallet_id", "wallet-1").containsEntry("stock_name", "AAPL").containsEntry("type", "buy");
        assertThat(log.get(2)).containsEntry("wallet_id", "wallet-2").containsEntry("stock_name", "AAPL").containsEntry("type", "buy");
    }

    @Test
    void auditLog_recordsSell_onSuccess() {
        setBankState(Map.of("AAPL", 2));
        trade("wallet-1", "AAPL", "buy");
        trade("wallet-1", "AAPL", "buy");
        setBankState(Map.of("AAPL", 0));

        trade("wallet-1", "AAPL", "sell");

        ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl() + "/log", Map.class);
        List<Map<String, String>> log = (List<Map<String, String>>) response.getBody().get("log");

        // buy x2 + sell x1 = 3 wpisy (setBankState nie jest logowane)
        assertThat(log).hasSize(3);
        assertThat(log.get(2)).containsEntry("type", "sell");
    }

    @Test
    void auditLog_doesNotRecord_failedOperations() {
        setBankState(Map.of("AAPL", 0)); // brak stocków — buy musi failować

        trade("wallet-1", "AAPL", "buy"); // 400
        trade("wallet-1", "UNKNOWN", "buy"); // 404

        ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl() + "/log", Map.class);
        List<Map<String, String>> log = (List<Map<String, String>>) response.getBody().get("log");

        assertThat(log).isEmpty();
    }

    // -------------------------------------------------------------------------
    // Helpery
    // -------------------------------------------------------------------------

    private void setBankState(Map<String, Integer> stocks) {
        List<Map<String, Object>> stockList = stocks.entrySet().stream()
                .map(e -> Map.<String, Object>of("name", e.getKey(), "quantity", e.getValue()))
                .toList();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.exchange(
                baseUrl() + "/stocks",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("stocks", stockList), headers),
                Void.class
        );
    }

    private ResponseEntity<Void> trade(String walletId, String stockName, String type) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(
                baseUrl() + "/wallets/" + walletId + "/stocks/" + stockName,
                HttpMethod.POST,
                new HttpEntity<>(Map.of("type", type), headers),
                Void.class
        );
    }

    private int getBankQuantity(String stockName) {
        ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl() + "/stocks", Map.class);
        List<Map<String, Object>> stocks = (List<Map<String, Object>>) response.getBody().get("stocks");
        return stocks.stream()
                .filter(s -> stockName.equals(s.get("name")))
                .mapToInt(s -> (Integer) s.get("quantity"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Stock " + stockName + " not found in bank"));
    }

    private int getWalletQuantity(String walletId, String stockName) {
        ResponseEntity<Integer> response = restTemplate.getForEntity(
                baseUrl() + "/wallets/" + walletId + "/stocks/" + stockName,
                Integer.class
        );
        return response.getBody();
    }
}