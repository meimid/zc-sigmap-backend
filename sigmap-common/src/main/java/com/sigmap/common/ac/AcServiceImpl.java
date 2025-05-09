package com.sigmap.common.ac;

import com.sigmap.common.com.exception.DuplicateComIdsException;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.ac.mapper.AcMapper;
import com.sigmap.common.ac.request.AcRequest;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.ComRepository;
import com.sigmap.common.com.exception.ComNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AcServiceImpl implements AcService{
    private final AcRepository repository;
    private final ComRepository comRepository;

    public AcServiceImpl(AcRepository repository, ComRepository comRepository) {
        this.repository = repository;
        this.comRepository = comRepository;
    }

    @Override
    public List<Ac> findAll() {
        Specification<Ac> ac = AcSpecifications.deleted(false);
        return repository.findAll(ac);
    }

    @Override
    public Page<Ac> findAll(Specification<Ac> ac, Pageable pageable) {
        return repository.findAll(ac, pageable);
    }

    @Override
    public Page<Ac> findAllDeleted(Specification<Ac> ac, Pageable pageable) {
        ac = AcSpecifications.deleted(true);
        return repository.findAll(ac, pageable);
    }

    @Override
    public Page<Ac> findAllDeleted(Pageable pageable) {
        Specification<Ac> ac = AcSpecifications.deleted(true);
        return repository.findAll(ac, pageable);
    }

    @Override
    public Optional<Ac> findDeletedById(Integer id) {
        Specification<Ac> specification = AcSpecifications.id(id).and(AcSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public Optional<Ac> findOne(Specification<Ac> specification) {

        return repository.findOne(specification);
    }

    @Override
    public Ac findById(Integer id) throws AcNotFound {
        return repository.findById(id).orElseThrow(() -> new AcNotFound("",List.of(id)));

    }

    @Override
    public Ac save(Ac ac) {
        return repository.save(ac);
    }

    @Override
    public Ac create(AcRequest request) throws ComNotFound {
        Ac ac = AcMapper.INSTANCE.map(request);
        if (request.getComIds() != null && !request.getComIds().isEmpty()) {
            List<Integer> comIds = request.getComIds();

            int size = getSizeList(comIds);

            // Check if all Com IDs exist
            var coms = comRepository.findAllById(comIds);
            if (coms.size() !=size) {
                List<Integer> existingComIds = coms.stream()
                        .map(Com::getId)
                        .toList();
                List<Integer> nonExistentComIds = comIds.stream()
                        .filter(comId -> !existingComIds.contains(comId))
                        .toList();
                throw new ComNotFound("", nonExistentComIds);
            }
            ac.setComs(coms);

        }
        return repository.save(ac);
    }


    @Override
    public Ac update(Integer id, AcRequest request) throws AcNotFound, ComNotFound, DuplicateComIdsException {
        Ac ac = repository.findById(id).orElseThrow(() -> new AcNotFound("", List.of(id)));
        AcMapper.INSTANCE.updateAcFromRequest(request, ac);

        if (request.getComIds() != null && !request.getComIds().isEmpty()) {
            // Manually check for duplicate Com IDs
            List<Integer> comIds = request.getComIds();
            int size = getSizeList(comIds);

            // Check if all Com IDs exist
            var coms = comRepository.findAllById(comIds);
            if (coms.size() != size) {
                // Identify existent and non-existent comIds
                List<Integer> existingComIds = coms.stream()
                        .map(Com::getId)
                        .toList();
                List<Integer> nonExistentComIds = comIds.stream()
                        .filter(comId -> !existingComIds.contains(comId))
                        .toList();
                throw new ComNotFound("", nonExistentComIds);
            }
            ac.setComs(coms);
        }

        return repository.save(ac);
    }

    private static int getSizeList(List<Integer> listIds) throws ComNotFound, DuplicateComIdsException {
        int size = listIds.size();
        List<Integer> duplicateIds = new ArrayList<>();

        // null check for comIds elements
        for (int i = 0; i < size; i++) {
            if (listIds.get(i) == null ) {
                throw new ComNotFound("" , List.of(listIds.get(i)));
            }
            for (int j = i + 1; j < size; j++) {
                if (listIds.get(i).equals(listIds.get(j))) {
                    duplicateIds.add(listIds.get(j));
                }
            }
            if (!duplicateIds.isEmpty()) {
                throw new DuplicateComIdsException("", duplicateIds);
            }
        }
        return size;
    }

    @Override
    public void delete(Integer id) {
        repository.softDelete(id);
    }
}
