package com.storex.promotion.service;

import com.storex.promotion.model.Banner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class PromotionService {

    private static final Logger log = LoggerFactory.getLogger(PromotionService.class);
    private final WebClient webClient;

    public PromotionService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Banner> getActiveBanner() {
        return webClient.get()
                .uri("/api/banners/active")
                .retrieve()
                .bodyToMono(Banner.class)
                .timeout(Duration.ofSeconds(2))
                .onErrorResume(throwable -> {
                    log.error("Lỗi hoặc Timeout (2s) khi gọi Promotion Service: {}", throwable.getMessage());
                    return Mono.just(getDefaultBanner());
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Promotion Service trả về rỗng, chuyển sang banner mặc định");
                    return Mono.just(getDefaultBanner());
                }));
    }

    private Banner getDefaultBanner() {
        return new Banner(
                "DEFAULT",
                "Khuyến mãi đang được cập nhật",
                "/images/default-banner.png",
                "/promotions",
                "Hệ thống đang cập nhật chương trình khuyến mãi mới nhất. Vui lòng quay lại sau!"
        );
    }
}