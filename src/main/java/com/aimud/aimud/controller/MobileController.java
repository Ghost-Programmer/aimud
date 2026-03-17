package com.aimud.aimud.controller;

import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.MobileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/mobiles")
@Slf4j
public class MobileController {

    private final MobileService mobileService;

    public MobileController(MobileService mobileService) {
        this.mobileService = mobileService;
    }

    @GetMapping
    public Flux<Mobile> getAllMobiles() {
        log.info("REST request to get all mobiles");
        return mobileService.getAllMobiles();
    }

    @GetMapping("/{id}")
    public Mono<Mobile> getMobile(@PathVariable Long id) {
        log.info("REST request to get mobile: {}", id);
        return mobileService.getMobile(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Mobile> createMobile(@RequestBody Mobile mobile) {
        log.info("REST request to create mobile: {}", mobile.getName());
        return mobileService.saveMobile(mobile);
    }

    @PutMapping("/{id}")
    public Mono<Mobile> updateMobile(@PathVariable Long id, @RequestBody Mobile mobile) {
        log.info("REST request to update mobile: {}", id);
        mobile.setId(id);
        return mobileService.saveMobile(mobile);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMobile(@PathVariable Long id) {
        log.info("REST request to delete mobile: {}", id);
        return mobileService.deleteMobile(id);
    }
}
