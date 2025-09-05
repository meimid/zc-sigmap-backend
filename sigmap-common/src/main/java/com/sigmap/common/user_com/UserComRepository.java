package com.sigmap.common.user_com;

import com.sigmap.common.user_ac.UserAc;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserComRepository extends JpaRepository<UserCom, UserComId>,
        JpaSpecificationExecutor<UserCom>{


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE UserCom s SET s.isDeleted = true, s.deletedAt = CURRENT_TIMESTAMP, s.deletedBy = :#{principal.subject} WHERE s.comId = :comId AND s.userId = :userId")
    Integer softDelete(@Param("comId") Integer comId, @Param("userId") String userId);

    List<UserCom> findAllByComId(Integer acId);

    List<UserCom> findAllByComIdAndIsDeletedFalse(Integer comId);

    List<UserCom> findAllByUserIdAndIsDeletedFalse(String userId);
    List<UserCom> findByUserIdAndIsDeletedFalse(String userId);

    @Query("SELECT uc.com.id FROM UserCom uc WHERE uc.userId = :userId AND uc.isDeleted = false")
    List<Integer> findComIdsByUserIdAndIsDeletedFalse(@Param("userId") String userId);

    @Query("SELECT uc FROM UserCom uc WHERE uc.userId = :userId AND uc.com.id = :comId AND uc.isDeleted = false")
    Optional<UserCom> findByUserIdAndComId(@Param("userId") String userId, @Param("comId") Integer comId);


    @Query("SELECT uc FROM UserCom uc WHERE uc.comId = :comId AND uc.isDeleted = false AND uc.responsable = true")
    UserCom findResponsableByComId(@Param("comId") Integer comId);








}
