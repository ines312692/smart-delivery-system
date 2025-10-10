package com.delivery.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Double latitude;
    private Double longitude;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
