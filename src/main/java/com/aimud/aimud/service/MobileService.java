package com.aimud.aimud.service;

import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.repository.MobileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class MobileService {

    private final MobileRepository mobileRepository;
    private final StatService statService;

    public MobileService(MobileRepository mobileRepository, StatService statService) {
        this.mobileRepository = mobileRepository;
        this.statService = statService;
    }

    @Cacheable(value = "mobiles")
    public Flux<Mobile> getAllMobiles() {
        log.info("Fetching all mobiles");
        return mobileRepository.findAll().cache();
    }

    @Cacheable(value = "mobile", key = "#id")
    public Mono<Mobile> getMobile(Long id) {
        log.info("Fetching mobile with id: {}", id);
        return mobileRepository.findById(id).cache();
    }

    @CacheEvict(value = {"mobiles", "mobile"}, allEntries = true)
    public Mono<Mobile> saveMobile(Mobile mobile) {
        log.info("Saving mobile: {} (id: {})", mobile.getName(), mobile.getId());
        return mobileRepository.save(mobile);
    }

    @CacheEvict(value = {"mobiles", "mobile"}, allEntries = true)
    public Mono<Void> deleteMobile(Long id) {
        log.info("Deleting mobile with id: {}", id);
        return mobileRepository.deleteById(id);
    }
}
