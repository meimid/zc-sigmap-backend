package com.sigmap.sigmap_params.email_trace;

import com.sigmap.common.email_trace.EmailTrace;
import com.sigmap.common.email_trace.EmailTraceService;
import com.sigmap.common.email_trace.request.EmailTraceQuery;
import com.sigmap.common.menu_item.MenuItem;
import com.sigmap.common.menu_item.mappers.MenuItemMapper;
import com.sigmap.common.menu_item.request.MenuItemQuery;
import com.sigmap.common.menu_item.response.MenuItemResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/email-trace")
public class EmailTraceController {

    private final EmailTraceService emailTraceService;

    @GetMapping
    public ResponseEntity<Page<EmailTrace>> findAll(EmailTraceQuery query, Pageable pageable) {
        log.debug("API for get All EmailTrace .... ");


        Page<EmailTrace> emailTraces = emailTraceService.findAll(query.toSpecification(), pageable);
        return ResponseEntity.ok(emailTraces);
    }
}
