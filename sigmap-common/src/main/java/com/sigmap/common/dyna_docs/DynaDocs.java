package com.sigmap.common.dyna_docs;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "dyna_docs")
@IdClass(CompositeId.class)
public class DynaDocs {

  @Id
  // @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dyna_docs_id_seq")
  // @SequenceGenerator(
  //     name = "dyna_docs_id_seq",
  //     sequenceName = "dyna_docs_id_seq",
  //     allocationSize = 1)
  @Column(name = "dyna_docs_id")
  private Integer id;

  @Id
  @Column(name = "dyna_docs_ver")
  private Integer version;

  @Column(name = "name")
  private String name;

  @Column(name = "template_file_name")
  private String templateFileName;

  @Column(name = "guide_file_name")
  private String guideFileName;

  @ManyToOne
  @JoinColumn(name = "category_id")
  private RefParam typeMarche;

  @ManyToOne
  @JoinColumn(name = "mode_passation_id")
  private ModeDePassationSelection modePassation;

  @Column(name = "dyna_doc_order")
  private Integer dynaDocOrder;

  @Enumerated(EnumType.STRING)
  private ActiveStatus status;

  @Column(name = "is_static")
  private Boolean isStatic;

  @Column(name = "is_aao")
  private Boolean isAAO;
}
