package ua.nulp.backend.entity.json;

import lombok.Data;
import java.io.Serializable;

@Data
public class OrderContactInfo implements Serializable {
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
}