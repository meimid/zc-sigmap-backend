package com.sigmap.common.societe_address;

import com.sigmap.common.Participant.ParticipantId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SocieteAddressId implements Serializable {
    private Integer societeId;
    private Integer addressId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SocieteAddressId that = (SocieteAddressId) o;
        return Objects.equals(societeId, that.societeId) &&
                Objects.equals(addressId, that.addressId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(societeId, addressId);
    }
}