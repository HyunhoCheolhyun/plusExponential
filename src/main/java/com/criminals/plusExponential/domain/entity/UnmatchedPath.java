package com.criminals.plusExponential.domain.entity;

import com.criminals.plusExponential.application.dto.UnmatchedPathDto;
import com.criminals.plusExponential.domain.embeddable.Coordinate;
import com.criminals.plusExponential.domain.embeddable.Fare;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Component;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UnmatchedPath extends BaseTimeEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @Column
    private Coordinate initPoint;

    @Embedded
    @Column
    private Coordinate destinationPoint;


    @Embedded
    @Column
    private Fare fare;

    @Column
    private int distance;

    @Column
    private int duration;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;




    public UnmatchedPathDto toDto() {
        UnmatchedPathDto dto = new UnmatchedPathDto();

        dto.setUser(this.getUser());
        dto.setInitPoint(this.getInitPoint());
        dto.setDestinationPoint(this.getDestinationPoint());
        dto.setFare(this.getFare());
        dto.setDistance(this.getDistance());
        dto.setDuration(this.getDuration());

        return dto;
    }

}
