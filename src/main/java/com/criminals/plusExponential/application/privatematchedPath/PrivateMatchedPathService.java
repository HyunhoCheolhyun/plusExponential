package com.criminals.plusExponential.application.privatematchedPath;

import com.criminals.plusExponential.application.PathService;
import com.criminals.plusExponential.application.dto.UnmatchedPathDto;
import com.criminals.plusExponential.domain.embeddable.Fare;
import com.criminals.plusExponential.domain.entity.MatchedPath;
import com.criminals.plusExponential.domain.entity.PrivateMatchedPath;
import com.criminals.plusExponential.infrastructure.persistence.MatchedPathRepository;
import com.criminals.plusExponential.infrastructure.persistence.PrivateMatchedPathRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrivateMatchedPathService {

    private final PathService pathService;
    private final PrivateMatchedPathRepository privateMatchedPathRepository;
    private final MatchedPathRepository matchedPathRepository;

    //테스트용으로 붙인 어노테이션

    public void receiveMessage(MatchedPath matchedPath, UnmatchedPathDto newRequest, UnmatchedPathDto partner) {
        createPrivateMatchedPath(matchedPath, newRequest, partner);


    }

    @Transactional
    public void createPrivateMatchedPath(MatchedPath matchedPath, UnmatchedPathDto newRequest, UnmatchedPathDto partner) {

        PrivateMatchedPath[] privateMatchedPaths = initFields(matchedPath, newRequest, partner);
        PrivateMatchedPath a = privateMatchedPaths[0];
        PrivateMatchedPath b = privateMatchedPaths[1];

        a.setMatchedPath(matchedPath);
        b.setMatchedPath(matchedPath);

        matchedPath.getPrivateMatchedPaths().add(a);
        matchedPath.getPrivateMatchedPaths().add(b);


        System.out.println("type = " + matchedPath.getType());
        System.out.println("a출발지 : " + a.getInitPoint() + "a 1경유지: " +  a.getFirstWayPoint() + "a 2경유지: " +  a.getSecondWayPoint() + " a도착지: " + a.getDestinationPoint());
        System.out.println("b출발지 : " + b.getInitPoint() + "b 1경유지: " +  b.getFirstWayPoint() + "b 2경유지: " +  b.getSecondWayPoint() + " b도착지: " + b.getDestinationPoint());

        privateMatchedPathRepository.save(a);
        privateMatchedPathRepository.save(b);


    }

    public PrivateMatchedPath[] initFields(MatchedPath matchedPath, UnmatchedPathDto newRequest, UnmatchedPathDto partner) {
        PrivateMatchedPath aPrivateMatchedPath = new PrivateMatchedPath();
        PrivateMatchedPath bPrivateMatchedPath = new PrivateMatchedPath();

        aPrivateMatchedPath.setFare(new Fare());
        bPrivateMatchedPath.setFare(new Fare());


        setPrivateMatchedPathToll(matchedPath, newRequest, partner, aPrivateMatchedPath, bPrivateMatchedPath);
        setPrivateMatchedPathPoints(matchedPath, newRequest, partner, aPrivateMatchedPath, bPrivateMatchedPath);
        setPrivateMatchedPathDistance(aPrivateMatchedPath, bPrivateMatchedPath);
        setPrivateMatchedPathFareTaxi(matchedPath, aPrivateMatchedPath, bPrivateMatchedPath);
        setPrivateMatchedPathDuration(matchedPath, aPrivateMatchedPath, bPrivateMatchedPath);


        setPrivateMatchedPathSavedAmount(matchedPath, aPrivateMatchedPath, bPrivateMatchedPath, newRequest, partner);

        aPrivateMatchedPath.setMatchedPath(matchedPath);
        bPrivateMatchedPath.setMatchedPath(matchedPath);

        aPrivateMatchedPath.setUser(newRequest.getUser());
        bPrivateMatchedPath.setUser(partner.getUser());

        PrivateMatchedPath[] result = new PrivateMatchedPath[2];

        result[0] = aPrivateMatchedPath;
        result[1] = bPrivateMatchedPath;

        return result;

    }

    private void setPrivateMatchedPathSavedAmount(MatchedPath matchedPath, PrivateMatchedPath a, PrivateMatchedPath b, UnmatchedPathDto newRequest, UnmatchedPathDto partner) {

        int savedAmountA = (newRequest.getFare().getTaxi() + newRequest.getFare().getToll())  - ( a.getFare().getTaxi() + a.getFare().getToll());
        int savedAmountB = (partner.getFare().getTaxi() + partner.getFare().getToll()) - (b.getFare().getTaxi() + b.getFare().getToll());


        a.setSavedAmount(savedAmountA);
        b.setSavedAmount(savedAmountB);
    }

    private void setPrivateMatchedPathDuration(MatchedPath matchedPath, PrivateMatchedPath a, PrivateMatchedPath b) {

        switch (matchedPath.getType()) {
            case 0:
                a.setDuration(pathService.getSummary(matchedPath.getInitPoint(), matchedPath.getFirstWayPoint(), matchedPath.getSecondWayPoint()).duration);
                b.setDuration(pathService.getSummary(matchedPath.getFirstWayPoint(), matchedPath.getSecondWayPoint(), matchedPath.getDestinationPoint()).duration);
                break;
            case 1:
                a.setDuration(matchedPath.getDuration());
                b.setDuration(pathService.getSummary(matchedPath.getFirstWayPoint(), matchedPath.getSecondWayPoint()).duration);
                break;
            case 2:
                a.setDuration(pathService.getSummary(matchedPath.getFirstWayPoint(), matchedPath.getSecondWayPoint(), matchedPath.getDestinationPoint()).duration);
                b.setDuration(pathService.getSummary(matchedPath.getInitPoint(), matchedPath.getFirstWayPoint(), matchedPath.getSecondWayPoint()).duration);
                break;
            case 3:
                a.setDuration(pathService.getSummary(matchedPath.getFirstWayPoint(), matchedPath.getSecondWayPoint()).duration);
                b.setDuration(matchedPath.getDuration());
                break;
        }
    }

    private void setPrivateMatchedPathDistance(PrivateMatchedPath a, PrivateMatchedPath b) {

        setPrivateMatchedPathDistanceDetail(a);
        setPrivateMatchedPathDistanceDetail(b);
    }

    private void setPrivateMatchedPathDistanceDetail(PrivateMatchedPath pm) {
        if (pm.getFirstWayPoint() == null) {
            pm.setDistance(
                    pathService.getSummary(pm.getInitPoint(), pm.getDestinationPoint()).distance
            );
        } else if (pm.getSecondWayPoint() == null) {
            int d1 = pathService.getSummary(pm.getInitPoint(), pm.getFirstWayPoint()).distance;
            int d2 = pathService.getSummary(pm.getFirstWayPoint(), pm.getDestinationPoint()).distance;
            pm.setDistance(d1 + d2);
        } else {
            pm.setDistance(
                    pathService.getSummary(
                            pm.getInitPoint(),
                            pm.getFirstWayPoint(),
                            pm.getSecondWayPoint(),
                            pm.getDestinationPoint()
                    ).distance
            );
        }
    }

    private void setPrivateMatchedPathFareTaxi(MatchedPath matchedPath, PrivateMatchedPath a, PrivateMatchedPath b) {

        int totalTaxiFare = matchedPath.getFare().getTaxi();
        int totalDistance = matchedPath.getDistance();
        int coRideDistance = pathService.getSummary(matchedPath.getFirstWayPoint(), matchedPath.getSecondWayPoint()).distance;
        System.out.println("a distance = " + a.getDistance() );
        System.out.println("b distance = " + b.getDistance());
        System.out.println("coRideDistance = " + coRideDistance);
        int aSoloDistance = a.getDistance() - coRideDistance;
        int bSoloDistance = b.getDistance() - coRideDistance;

        long partASolo  = (long) totalTaxiFare * aSoloDistance   / totalDistance;
        long partAShared = (long) totalTaxiFare * coRideDistance / totalDistance / 2;

        int aTaxiFare = (int) (partASolo + partAShared);
        Double ra = ((double) totalTaxiFare * (double) aSoloDistance / (double) totalDistance) + ((double) totalTaxiFare * (double) coRideDistance / (double) totalDistance / 2);
        ra -= aTaxiFare;


        long partBSolo  = (long) totalTaxiFare * bSoloDistance  / totalDistance;
        long partBShared = (long) totalTaxiFare * coRideDistance / totalDistance / 2;
        int  bTaxiFare  = (int) (partBSolo + partBShared);
        Double rb = ((double) totalTaxiFare * (double) bSoloDistance / (double) totalDistance) + ((double) totalTaxiFare * (double) coRideDistance / (double) totalDistance / 2);
        rb -= bTaxiFare;

        System.out.println("---------------------------------------");

        System.out.println("totalDistance = " + totalDistance);
        System.out.println("totalTaxiFare = " + totalTaxiFare);
        System.out.println("a TaxiFare = " + aTaxiFare);
        System.out.println("b TaxiFare = " + bTaxiFare);


        if (aTaxiFare + bTaxiFare < totalTaxiFare) {
            int difference = totalTaxiFare - (aTaxiFare + bTaxiFare);

            if (ra > rb) {
                a.getFare().setTaxi(aTaxiFare + difference);
                b.getFare().setTaxi(bTaxiFare);
            } else {
                a.getFare().setTaxi(aTaxiFare);
                b.getFare().setTaxi(bTaxiFare + difference);
            }


        }
    }





    public void setPrivateMatchedPathToll(MatchedPath matchedPath, UnmatchedPathDto newReq, UnmatchedPathDto partner, PrivateMatchedPath a, PrivateMatchedPath b) {
        int matchedToll = matchedPath.getFare().getToll();
        int aToll = newReq.getFare().getToll();
        int bToll = partner.getFare().getToll();

        int unmatchedSum = aToll + bToll;
        int delta = matchedToll - unmatchedSum;

        if (delta > 0) {            // 합승 톨이 더 클 때 균등 분배 감소
            aToll += delta / 2;
            bToll += delta / 2;
        } else if (delta < 0) {     // 합승 톨이 더 작을 때 균등 분배 증가
            aToll += delta / 2;
            bToll += delta / 2;
        }

        a.getFare().setToll(aToll);
        b.getFare().setToll(bToll);
    }



    private void setPrivateMatchedPathPoints(MatchedPath matchedPath,
                                             UnmatchedPathDto newReq,
                                             UnmatchedPathDto partner,
                                             PrivateMatchedPath a,
                                             PrivateMatchedPath b) {
        a.setInitPoint(newReq.getInitPoint());
        b.setInitPoint(partner.getInitPoint());

        switch (matchedPath.getType()) {
            case 0 -> {
                a.setFirstWayPoint(partner.getInitPoint());
                a.setDestinationPoint(newReq.getDestinationPoint());
                b.setFirstWayPoint(newReq.getDestinationPoint());
                b.setDestinationPoint(partner.getDestinationPoint());
            }
            case 1 -> {
                a.setFirstWayPoint(partner.getInitPoint());
                a.setSecondWayPoint(partner.getDestinationPoint());
                a.setDestinationPoint(newReq.getDestinationPoint());
                b.setDestinationPoint(partner.getDestinationPoint());
            }
            case 2 -> {
                a.setFirstWayPoint(partner.getDestinationPoint());
                a.setDestinationPoint(newReq.getDestinationPoint());
                b.setFirstWayPoint(newReq.getInitPoint());
                b.setDestinationPoint(partner.getDestinationPoint());
            }
            case 3 -> {
                a.setDestinationPoint(newReq.getDestinationPoint());
                b.setFirstWayPoint(newReq.getInitPoint());
                b.setSecondWayPoint(newReq.getDestinationPoint());
                b.setDestinationPoint(partner.getDestinationPoint());
            }
            default -> throw new IllegalArgumentException("unknown matchedPath type");
        }
    }
}
