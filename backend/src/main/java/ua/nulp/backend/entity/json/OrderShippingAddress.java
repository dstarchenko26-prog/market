package ua.nulp.backend.entity.json;

import lombok.Data;
import ua.nulp.backend.entity.enums.DeliveryProvider;
import java.io.Serializable;

@Data
public class OrderShippingAddress implements Serializable {
    // Географія
    private String region;
    private String district;
    private String city;
    private String cityId;

    // Відділення
    private String department;
    private String departmentId;

    // Кур'єр
    private String street;
    private String house;
    private String apartment;
    private String streetId;
    private boolean isCourier;

    // Службові
    private DeliveryProvider provider;
}
