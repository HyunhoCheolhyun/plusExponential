package com.criminals.plusExponential.application;

import com.criminals.plusExponential.application.dto.UnmatchedPathDto;
import com.criminals.plusExponential.common.exception.customex.DecideOrderError;
import com.criminals.plusExponential.common.exception.customex.ErrorCode;
import com.criminals.plusExponential.domain.entity.MatchedPath;
import com.criminals.plusExponential.infrastructure.KakaoMobilityClient;
import com.criminals.plusExponential.infrastructure.persistence.MatchedPathRepository;
import com.criminals.plusExponential.infrastructure.persistence.UnmatchedPathRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.rmi.UnexpectedException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MatchedPathService extends PathService{

    private final MatchedPathRepository matchedPathRepository;
    private final PrivateMatchedPathService privateMatchedPathService;

    public MatchedPathService(KakaoMobilityClient km, UnmatchedPathRepository unmatchedPathRepository, MatchedPathRepository matchedPathRepository, PrivateMatchedPathService privateMatchedPathService) {
        super(km, unmatchedPathRepository);
        this.matchedPathRepository = matchedPathRepository;
        this.privateMatchedPathService = privateMatchedPathService;
    }

    private Map<Integer, Summary> decideOrder(UnmatchedPathDto newRequest, UnmatchedPathDto partner) {
        Map<Integer, Summary> retMap = new HashMap<>();

        Summary summary0 = getSummary(newRequest.getInitPoint(), partner.getInitPoint(), newRequest.getDestinationPoint(), partner.getDestinationPoint());
        Summary summary1 = getSummary(newRequest.getInitPoint(), partner.getInitPoint(), partner.getDestinationPoint(), newRequest.getDestinationPoint());
        Summary summary2 = getSummary(partner.getInitPoint(), newRequest.getInitPoint(), partner.getDestinationPoint(), newRequest.getDestinationPoint());
        Summary summary3 = getSummary(partner.getInitPoint(), newRequest.getInitPoint(), newRequest.getDestinationPoint(), partner.getDestinationPoint());

        retMap.put(0, summary0);
        retMap.put(1, summary1);
        retMap.put(2, summary2);
        retMap.put(3, summary3);

        int min = Integer.MAX_VALUE;

        for (int i = 0; i < 4; i++) {

            Summary currentSummary = retMap.get(i);
            int currentFareSum = currentSummary.fare.getTotal();

            if (currentFareSum < min) {
                min = currentFareSum;
                continue;
            }

            retMap.remove(i);

        }

        return retMap;

    }

    private void sendMessageToPrivateMatchedPath(MatchedPath matchedPath, UnmatchedPathDto newRequest, UnmatchedPathDto partner) {
        privateMatchedPathService.createPrivateMatchedPath(matchedPath, newRequest, partner);
    }

    @Transactional
    public void createMatchedPath(UnmatchedPathDto newRequest, UnmatchedPathDto partner) {

        Map<Integer, Summary> typeMap = decideOrder(newRequest, partner);

        try {
            for (int i = 0; i < 4; i++) {

                if (typeMap.containsKey(i)) {
                    MatchedPath matchedPath = new MatchedPath(i, newRequest, partner);
                    Summary summary = typeMap.get(i);
                    matchedPath.setDuration(summary.duration);
                    matchedPath.setDistance(summary.distance);
                    matchedPath.setFare(summary.fare);

                    matchedPathRepository.save(matchedPath);
                    sendMessageToPrivateMatchedPath(matchedPath, newRequest, partner);
                    return;

                }

            }

            throw new DecideOrderError(ErrorCode.DecideOrderError);

        } catch (DataAccessException e) {


            log.error("DB 저장 중 예외 발생: {}", e.getMessage(), e);


            throw e;
        }


    }


}
