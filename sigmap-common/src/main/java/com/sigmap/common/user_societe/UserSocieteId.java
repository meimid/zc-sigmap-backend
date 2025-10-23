package com.sigmap.common.user_societe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSocieteId implements Serializable {
    private String userId;
    private Integer societeId;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSocieteId that = (UserSocieteId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(societeId, that.societeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, societeId);
    }
}

