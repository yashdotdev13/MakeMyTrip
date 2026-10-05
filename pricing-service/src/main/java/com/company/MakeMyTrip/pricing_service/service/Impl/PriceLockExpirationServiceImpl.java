package com.company.MakeMyTrip.pricing_service.service.Impl;

import com.company.MakeMyTrip.pricing_service.entity.PriceLock;
import com.company.MakeMyTrip.pricing_service.enums.PriceLockStatus;
import com.company.MakeMyTrip.pricing_service.repository.PriceLockRepository;
import com.company.MakeMyTrip.pricing_service.service.PriceLockExpirationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceLockExpirationServiceImpl implements PriceLockExpirationService {

    private final PriceLockRepository priceLockRepository;

    @Override
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void expireLocks() {

        LocalDateTime now = LocalDateTime.now();
        List<PriceLock> expiredLocks = priceLockRepository
                .findByStatusAndValidTillBefore(PriceLockStatus.ACTIVE, now);

        if (expiredLocks.isEmpty()) {
            return;
        }
        for (PriceLock lock : expiredLocks) {
            lock.setStatus(PriceLockStatus.EXPIRED);

            log.info("Price lock expired: lockId={}, referenceId={}, validTill={}",
                    lock.getId(), lock.getReferenceId(), lock.getValidTill());
        }
        priceLockRepository.saveAll(expiredLocks);
        log.info("Expired {} price locks", expiredLocks.size());
    }
}