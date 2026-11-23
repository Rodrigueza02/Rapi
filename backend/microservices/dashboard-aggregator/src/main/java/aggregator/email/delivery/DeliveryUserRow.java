package aggregator.email.delivery;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class DeliveryUserRow {

    @Id
    private UUID id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "email")
    private String email;

    @Column(name = "activo")
    private boolean activo;

    protected DeliveryUserRow() {}

    public UUID   getId()     { return id; }
    public String getNombre() { return nombre; }
    public String getEmail()  { return email; }
    public boolean isActivo() { return activo; }
}
