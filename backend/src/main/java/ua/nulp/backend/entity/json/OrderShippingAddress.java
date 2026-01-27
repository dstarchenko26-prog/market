package ua.nulp.backend.entity.json;

import lombok.Data;
import ua.nulp.backend.entity.enums.DeliveryProvider;
import java.io.Serializable;

@Data
public class OrderShippingAddress implements Serializable {
    // Географія
    private String city;
    private String cityRef;

    // Відділення
    private String department;
    private String departmentRef;

    // Кур'єр
    private String street;
    private String house;
    private String apartment;

    // Службові
    private DeliveryProvider provider;
}
