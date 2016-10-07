package nodefriends;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by Thagus on 07/10/16.
 */
public class Amigo{
    //UID Facebook, NombreUsuario, Nivel, NumeroNodo
    String uidFacebook;
    String nombreUsuario;
    int nivel;
    int numeroNodo;

    public Amigo(String uidFacebook, String nombreUsuario, int nivel, int numeroNodo) {
        this.uidFacebook = uidFacebook;
        this.nombreUsuario = nombreUsuario;
        this.nivel = nivel;
        this.numeroNodo = numeroNodo;
    }

    @Override
    public int hashCode(){
        return new HashCodeBuilder(17, 31)
                .append(uidFacebook)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj){
        if (!(obj instanceof Amigo))
            return false;
        if (obj == this)
            return true;

        Amigo rhs = (Amigo) obj;
        return new EqualsBuilder()
                .append(uidFacebook, rhs.uidFacebook)
                .isEquals();
    }
}
