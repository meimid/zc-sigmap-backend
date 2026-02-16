package com.sigmap.common.reunion_dac_doc;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReunionDacDocServiceImpl implements ReunionDacDocService {
    private final ReunionDacDocRepository repository;

    @Override
    public Optional<ReunionDacDoc> findOne(Specification<ReunionDacDoc> specification) throws IOException {
        return repository.findOne(specification);
    }
}
