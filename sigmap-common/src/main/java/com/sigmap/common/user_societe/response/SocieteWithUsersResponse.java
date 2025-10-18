package com.sigmap.common.user_societe.response;


import com.sigmap.common.societe.response.SocieteResponseDto;
import lombok.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SocieteWithUsersResponse {

    private SocieteResponseDto societe;
    private List<UserSocieteResponseDto> content;

    private Pageable pageable;
    private boolean last;
    private long totalElements;
    private int totalPages;
    private int size;
    private int number;
    private Sort sort;
    private boolean first;
    private int numberOfElements;
    private boolean empty;
}
