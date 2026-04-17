package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.model.MobileAction;
import io.nadia.ai.aimud.service.MobileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST Controller exposing HTTP API endpoints for Mobile manipulation.
 */
@RestController
@RequestMapping("/api/mobiles")
@Slf4j
public class MobileController {

    private final MobileService mobileService;

    public MobileController(MobileService mobileService) {
        this.mobileService = mobileService;
    }

    /**
     * Handles HTTP GET requests to get all mobiles.
     * @return dynamic reactive Flux<Mobile> response payload
     */
    @GetMapping
    public Flux<Mobile> getAllMobiles() {
        log.info("REST request to get all mobiles");
        return mobileService.getAllMobiles();
    }

    /**
     * Handles HTTP GET requests to get mobile.
     * @param id bound request payload or parameter
     * @return dynamic reactive Mono<Mobile> response payload
     */
    @GetMapping("/{id}")
    public Mono<Mobile> getMobile(@PathVariable Long id) {
        log.info("REST request to get mobile: {}", id);
        return mobileService.getMobile(id);
    }

    /**
     * Handles HTTP POST requests to create mobile.
     * @param mobile bound request payload or parameter
     * @return dynamic reactive Mono<Mobile> response payload
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Mobile> createMobile(@RequestBody Mobile mobile) {
        log.info("REST request to create mobile: {}", mobile.getName());
        return mobileService.saveMobile(mobile);
    }

    /**
     * Handles HTTP PUT requests to update mobile.
     * @param id bound request payload or parameter
     * @param mobile bound request payload or parameter
     * @return dynamic reactive Mono<Mobile> response payload
     */
    @PutMapping("/{id}")
    public Mono<Mobile> updateMobile(@PathVariable Long id, @RequestBody Mobile mobile) {
        log.info("REST request to update mobile: {}", id);
        mobile.setId(id);
        return mobileService.saveMobile(mobile);
    }

    /**
     * Handles HTTP DELETE requests to delete mobile.
     * @param id bound request payload or parameter
     * @return dynamic reactive Mono<Void> response payload
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMobile(@PathVariable Long id) {
        log.info("REST request to delete mobile: {}", id);
        return mobileService.deleteMobile(id);
    }

    /**
     * Handles HTTP GET requests to get actions.
     * @param id bound request payload or parameter
     * @return dynamic reactive Flux<MobileAction> response payload
     */
    @GetMapping("/{id}/actions")
    public Flux<MobileAction> getActions(@PathVariable Long id) {
        return mobileService.getMobileActions(id);
    }

    /**
     * Handles HTTP POST requests to save actions.
     * @param id bound request payload or parameter
     * @param actions bound request payload or parameter
     * @return dynamic reactive Flux<MobileAction> response payload
     */
    @PostMapping("/{id}/actions")
    public Flux<MobileAction> saveActions(@PathVariable Long id, @RequestBody java.util.List<MobileAction> actions) {
        return mobileService.saveMobileActions(id, actions);
    }
}
