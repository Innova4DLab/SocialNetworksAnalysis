package nodefriends;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by Thagus on 07/10/16.
 */
public class Link {
    //NumeroNodoOrigen, NumeroNodoDestino
    int numeroNodoOrigen;
    int numeroNodoDestino;

    public Link(int numeroNodoOrigen, int numeroNodoDestino) {
        this.numeroNodoOrigen = numeroNodoOrigen;
        this.numeroNodoDestino = numeroNodoDestino;
    }

    @Override
    public int hashCode(){
        return new HashCodeBuilder(17, 31)
                .append(numeroNodoOrigen)
                .append(numeroNodoDestino)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj){
        if (!(obj instanceof Link))
            return false;
        if (obj == this)
            return true;

        Link rhs = (Link) obj;
        return new EqualsBuilder()
                .append(numeroNodoOrigen, rhs.numeroNodoOrigen)
                .append(numeroNodoDestino, rhs.numeroNodoDestino)
                .isEquals();
    }
}
