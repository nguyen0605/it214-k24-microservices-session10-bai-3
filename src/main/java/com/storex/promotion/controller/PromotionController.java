package com.storex.promotion.controller;

import com.storex.promotion.model.Banner;
import com.storex.promotion.service.PromotionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/store")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping("/banner")
    public Mono<Banner> getActiveBanner() {
        return promotionService.getActiveBanner();
    }
}