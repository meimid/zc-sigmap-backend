package com.sigmap.common.user_societe.response;

import com.sigmap.common.societe.response.SocieteResponseDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SocieteUsersListUsersResponse {
    private SocieteResponseDto societe;
    private List<UserResponse> users;
}
